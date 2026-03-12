package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.response.PhysiologicalConditionDTO;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public interface CatalogService {

    public List<PhysiologicalConditionDTO> getPhysiologicalConditions();

}