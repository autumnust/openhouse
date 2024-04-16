package com.linkedin.openhouse.housetables.services.toggle;

import com.linkedin.openhouse.housetables.model.TableToggleRule;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class RegexSimpleRuleEngine implements TableRuleEngine {
  @Override
  public boolean eval(String databaseId, String tableId, Iterable<TableToggleRule> rules) {
    // base version of the rule engine just iterate through all existing rules
    // and return positive when hitting the first match
    for (TableToggleRule rule : rules) {
      if (regexMatch(rule.getDatabasePattern(), databaseId)
          && regexMatch(rule.getTablePattern(), tableId)) {
        return true;
      }
    }
    return false;
  }

  /** Regex-based matching */
  private boolean regexMatch(String rulePattern, String entityId) {
    Pattern pattern = Pattern.compile(rulePattern);
    Matcher matcher = pattern.matcher(entityId);

    return matcher.matches();
  }
}
