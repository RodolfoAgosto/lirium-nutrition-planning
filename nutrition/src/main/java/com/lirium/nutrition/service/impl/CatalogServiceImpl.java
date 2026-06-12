package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.PhysiologicalConditionDTO;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import com.lirium.nutrition.service.CatalogService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class CatalogServiceImpl implements CatalogService {

    @Override
    public List<PhysiologicalConditionDTO> getPhysiologicalConditions() {

        return Arrays.stream(PhysiologicalCondition.values())
                .map(condition ->
                        new PhysiologicalConditionDTO(
                                condition.name(),
                                condition.getLabel()
                        ))
                .toList();
    }
}