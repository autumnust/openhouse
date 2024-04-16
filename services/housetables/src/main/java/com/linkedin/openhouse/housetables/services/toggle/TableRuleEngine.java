package com.linkedin.openhouse.housetables.services.toggle;

import com.linkedin.openhouse.housetables.model.TableToggleRule;

public interface TableRuleEngine {
  boolean eval(String databaseId, String tableId, Iterable<TableToggleRule> rules);
}
