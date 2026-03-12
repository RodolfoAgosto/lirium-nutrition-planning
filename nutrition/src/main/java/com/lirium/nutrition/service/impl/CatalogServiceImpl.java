package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.PhysiologicalConditionDTO;
import com.lirium.nutrition.exception.CatalogException;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import com.lirium.nutrition.service.CatalogService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CatalogServiceImpl implements CatalogService {

    @Override
    public List<PhysiologicalConditionDTO> getPhysiologicalConditions() {

        try {

            List<PhysiologicalConditionDTO> conditions = Arrays.stream(PhysiologicalCondition.values())
                    .map(condition -> new PhysiologicalConditionDTO(
                            condition.name(),
                            condition.getLabel()
                    ))
                    .collect(Collectors.toList());

            if (conditions.isEmpty()) {
                throw new CatalogException("No physiological conditions available");
            }

            return conditions;

        } catch (Exception ex) {
            throw new CatalogException("Error retrieving physiological conditions catalog", ex);
        }
    }
}