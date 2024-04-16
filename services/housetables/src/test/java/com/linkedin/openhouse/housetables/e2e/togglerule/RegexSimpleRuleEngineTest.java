package com.linkedin.openhouse.housetables.e2e.togglerule;

import com.google.common.collect.ImmutableList;
import com.linkedin.openhouse.housetables.model.TableToggleRule;
import com.linkedin.openhouse.housetables.services.toggle.RegexSimpleRuleEngine;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RegexSimpleRuleEngineTest {

  @Test
  public void testRegexRuleEngine() {
    RegexSimpleRuleEngine ruleEngine = new RegexSimpleRuleEngine();
    List<TableToggleRule> toggleRuleList =
        ImmutableList.of(
            TableToggleRule.builder().databasePattern("db").tablePattern("table").build(),
            TableToggleRule.builder().databasePattern("nyc.*").tablePattern("table").build(),
            TableToggleRule.builder().databasePattern("^a.*|^b.*").tablePattern(".*").build());

    Assertions.assertTrue(ruleEngine.eval("db", "table", toggleRuleList));
    Assertions.assertTrue(ruleEngine.eval("nycaa", "table", toggleRuleList));
    Assertions.assertFalse(ruleEngine.eval("nyc123", "able", toggleRuleList));
    Assertions.assertTrue(ruleEngine.eval("a5", "table", toggleRuleList));
    Assertions.assertTrue(ruleEngine.eval("b4", "table", toggleRuleList));
    Assertions.assertFalse(ruleEngine.eval("ccc", "table", toggleRuleList));
  }
}
