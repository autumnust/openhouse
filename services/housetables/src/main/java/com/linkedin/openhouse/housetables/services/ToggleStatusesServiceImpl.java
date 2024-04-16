package com.linkedin.openhouse.housetables.services;

import com.linkedin.openhouse.common.api.spec.ToggleStatusEnum;
import com.linkedin.openhouse.housetables.api.spec.model.ToggleStatus;
import com.linkedin.openhouse.housetables.repository.impl.jdbc.ToggleStatusHtsJdbcRepository;
import com.linkedin.openhouse.housetables.services.toggle.TableRuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ToggleStatusesServiceImpl implements ToggleStatusesService {
  @Autowired ToggleStatusHtsJdbcRepository htsRepository;
  @Autowired TableRuleEngine tableRuleEngine;

  @Override
  public ToggleStatus getTableToggleStatus(String featureId, String databaseId, String tableId) {
    boolean evalResult =
        tableRuleEngine.eval(databaseId, tableId, htsRepository.findAllByFeature(featureId));
    return evalResult
        ? ToggleStatus.builder().status(ToggleStatusEnum.ACTIVE).build()
        : ToggleStatus.builder().status(ToggleStatusEnum.INACTIVE).build();
  }
}
