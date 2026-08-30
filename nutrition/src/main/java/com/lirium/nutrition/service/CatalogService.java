package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.response.PhysiologicalConditionDTO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface CatalogService {

  public List<PhysiologicalConditionDTO> getPhysiologicalConditions();
}
