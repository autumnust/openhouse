package com.linkedin.openhouse.tables.api.validator.impl;

import static com.linkedin.openhouse.common.schema.IcebergSchemaHelper.*;
import static org.apache.iceberg.types.Types.NestedField.*;

import com.linkedin.openhouse.common.api.spec.TableUri;
import com.linkedin.openhouse.tables.api.spec.v0.request.components.Policies;
import com.linkedin.openhouse.tables.api.spec.v0.request.components.Retention;
import com.linkedin.openhouse.tables.api.spec.v0.request.components.RetentionColumnPattern;
import com.linkedin.openhouse.tables.api.spec.v0.request.components.TimePartitionSpec;
import java.lang.reflect.Field;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Types;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PoliciesSpecValidatorTest {

  PoliciesSpecValidator validator;

  private Schema dummySchema;

  private Schema nestedSchema;

  @BeforeEach
  public void setup() {
    this.validator = new PoliciesSpecValidator();
    this.dummySchema =
        new Schema(
            required(1, "id", Types.StringType.get()), required(2, "aa", Types.StringType.get()));

    // A nested version wrapping over dummySchema
    this.nestedSchema =
        new Schema(
            required(3, "top1", Types.StructType.of(dummySchema.columns())),
            required(4, "top2", Types.IntegerType.get()));
  }

  @Test
  void testValidatePatternPositive() {

    // With pattern
    RetentionColumnPattern pattern =
        RetentionColumnPattern.builder().pattern("yyyy-mm-dd-hh").columnName("aa").build();
    Retention retention1 =
        Retention.builder()
            .columnPattern(pattern)
            .count(1)
            .granularity(TimePartitionSpec.Granularity.DAY)
            .build();

    Assertions.assertTrue(
        validator.validatePatternIfPresent(
            retention1, TableUri.builder().build(), getSchemaJsonFromSchema(dummySchema)));

    // Without Pattern
    Retention retention2 =
        Retention.builder()
            .columnPattern(null)
            .count(10)
            .granularity(TimePartitionSpec.Granularity.DAY)
            .build();
    Assertions.assertTrue(
        validator.validatePatternIfPresent(
            retention2, TableUri.builder().build(), getSchemaJsonFromSchema(dummySchema)));

    // Able to find nested columns in the column existence check
    pattern =
        RetentionColumnPattern.builder().pattern("yyyy-mm-dd-hh").columnName("top1.aa").build();
    Retention retention3 =
        Retention.builder()
            .columnPattern(pattern)
            .count(1)
            .granularity(TimePartitionSpec.Granularity.DAY)
            .build();
    Assertions.assertTrue(
        validator.validatePatternIfPresent(
            retention3, TableUri.builder().build(), getSchemaJsonFromSchema(nestedSchema)));
  }

  @Test
  void testValidatePatternNegative() {
    RetentionColumnPattern malformedPattern =
        RetentionColumnPattern.builder().pattern("random_pattern").columnName("aa").build();
    Retention testRetention =
        Retention.builder()
            .columnPattern(malformedPattern)
            .count(1)
            .granularity(TimePartitionSpec.Granularity.DAY)
            .build();
    Assertions.assertFalse(
        validator.validatePatternIfPresent(
            testRetention, TableUri.builder().build(), getSchemaJsonFromSchema(dummySchema)));
  }

  @Test
  void testValidate() {
    // Negative: declared retention column not exists
    RetentionColumnPattern pattern0 =
        RetentionColumnPattern.builder()
            .pattern("yyyy-mm-dd-hh")
            .columnName("bb")
            .build(); /* dummySchema doesn't have bb*/
    Retention retention0 =
        Retention.builder()
            .count(1)
            .granularity(TimePartitionSpec.Granularity.DAY)
            .columnPattern(pattern0)
            .build();
    Policies policies0 = Policies.builder().retention(retention0).build();
    Assertions.assertFalse(
        validator.validate(
            policies0, null, TableUri.builder().build(), getSchemaJsonFromSchema(dummySchema)));

    pattern0 =
        RetentionColumnPattern.builder()
            .pattern("yyyy-mm-dd-hh")
            .columnName("Aa") /* casing matters*/
            .build();
    retention0 =
        Retention.builder()
            .count(1)
            .granularity(TimePartitionSpec.Granularity.DAY)
            .columnPattern(pattern0)
            .build();
    policies0 = Policies.builder().retention(retention0).build();
    Assertions.assertFalse(
        validator.validate(
            policies0, null, TableUri.builder().build(), getSchemaJsonFromSchema(dummySchema)));

    pattern0 =
        RetentionColumnPattern.builder()
            .pattern("yyyy-mm-dd-hh")
            .columnName("top1.aaa") /* negative case for nested*/
            .build();
    retention0 =
        Retention.builder()
            .count(1)
            .granularity(TimePartitionSpec.Granularity.DAY)
            .columnPattern(pattern0)
            .build();
    policies0 = Policies.builder().retention(retention0).build();
    Assertions.assertFalse(
        validator.validate(
            policies0, null, TableUri.builder().build(), getSchemaJsonFromSchema(nestedSchema)));

    // Negative: Missing timepartitionspec AND pattern
    Retention retention1 =
        Retention.builder().count(1).granularity(TimePartitionSpec.Granularity.DAY).build();
    Policies policies1 = Policies.builder().retention(retention1).build();
    Assertions.assertFalse(
        validator.validate(
            policies1, null, TableUri.builder().build(), getSchemaJsonFromSchema(dummySchema)));

    // Positive: Only have pattern but no timepartitionSpec
    RetentionColumnPattern pattern =
        RetentionColumnPattern.builder().pattern("yyyy-mm-dd-hh").build();
    Retention retention2 = retention1.toBuilder().columnPattern(pattern).build();
    Policies policies2 = Policies.builder().retention(retention2).build();
    Assertions.assertTrue(
        validator.validate(
            policies2, null, TableUri.builder().build(), getSchemaJsonFromSchema(dummySchema)));

    // Negative: Having both timepartitionspec AND pattern
    Retention retention3 =
        Retention.builder()
            .count(1)
            .granularity(TimePartitionSpec.Granularity.DAY)
            .columnPattern(pattern)
            .build();
    Policies policies3 = Policies.builder().retention(retention3).build();
    Assertions.assertFalse(
        validator.validate(
            policies3,
            TimePartitionSpec.builder()
                .columnName("ts")
                .granularity(TimePartitionSpec.Granularity.DAY)
                .build(),
            TableUri.builder().build(),
            getSchemaJsonFromSchema(dummySchema)));

    // Negative: Having both timepartitionspec AND invalid-pattern
    RetentionColumnPattern malformedPattern =
        RetentionColumnPattern.builder().pattern("random_pattern").columnName("aa").build();
    Retention retention4 =
        Retention.builder()
            .count(1)
            .granularity(TimePartitionSpec.Granularity.DAY)
            .columnPattern(malformedPattern)
            .build();
    Policies policies4 = Policies.builder().retention(retention4).build();
    Assertions.assertFalse(
        validator.validate(
            policies4,
            TimePartitionSpec.builder()
                .columnName("ts")
                .granularity(TimePartitionSpec.Granularity.DAY)
                .build(),
            TableUri.builder().build(),
            getSchemaJsonFromSchema(dummySchema)));

    Field failedMsg =
        org.springframework.util.ReflectionUtils.findField(
            PoliciesSpecValidator.class, "failureMessage");
    Assertions.assertNotNull(failedMsg);
    org.springframework.util.ReflectionUtils.makeAccessible(failedMsg);
    Assertions.assertTrue(
        ((String) org.springframework.util.ReflectionUtils.getField(failedMsg, validator))
            .contains("You can only specify retention column pattern on non-timestampPartitioned"));

    // The granularity mismatch is covered in
    // com.linkedin.openhouse.tables.e2e.h2.TablesControllerTest.testCreateRequestFailsForWithGranularityDifferentFromTimePartitionSpec
    // with error message validation

  }
}
