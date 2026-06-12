package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.response.PhysiologicalConditionDTO;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @InjectMocks
    private CatalogServiceImpl catalogService;

    @Test
    void shouldReturnPhysiologicalConditions() {

        // When
        List<PhysiologicalConditionDTO> result =
                catalogService.getPhysiologicalConditions();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());

        assertEquals(
                PhysiologicalCondition.values().length,
                result.size()
        );

        PhysiologicalCondition first =
                PhysiologicalCondition.values()[0];

        assertEquals(first.name(), result.getFirst().code());
        assertEquals(first.getLabel(), result.getFirst().label());
    }

}