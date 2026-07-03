package com.lirium.nutrition.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.lirium.nutrition.dto.response.PatientProfileHistoryResponseDTO;
import com.lirium.nutrition.dto.response.PatientProfileHistorySummaryDTO;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.PatientProfileHistory;
import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PatientProfileHistoryMapperTest {

    @Test
    void toResponseDTO_shouldMapCorrectly() {
        // Arrange
        User user = new User();
        PatientProfile profile = new PatientProfile(user);

        Restriction r1 = Restriction.builder()
                .code("SUGAR")
                .name("SUGAR")
                .category(RestrictionCategory.DIETARY)
                .description("No sugar allowed")
                .id(1L)
                .build();

        Restriction r2 = Restriction.builder()
                .code("SALT")
                .name("SALT")
                .category(RestrictionCategory.DIETARY)
                .description("Low salt diet")
                .id(2L)
                .build();

        profile.addRestriction(r1);
        profile.addRestriction(r2);

        Weight weight = new Weight(70000);
        Height height = new Height(180);

        profile.updateNutritionProfile(
                height,
                weight,
                ActivityLevel.ACTIVE,
                GoalType.WEIGHT_LOSS
        );

        PatientProfileHistory history = new PatientProfileHistory(profile);

        // Act
        PatientProfileHistoryResponseDTO dto =
                PatientProfileHistoryMapper.toResponseDTO(history);

        // Assert
        assertNotNull(dto);

        assertEquals(70000, dto.weight());
        assertEquals(Integer.valueOf("180"), dto.height());

        assertEquals(Set.of("SUGAR", "SALT"), dto.restrictions());

        assertEquals(history.getVisitDate(), dto.visitDate());
        assertEquals(profile.getPrimaryGoal(), dto.primaryGoal());
    }

    @Test
    void toResponseDTO_shouldHandleEmptyRestrictions() {
        User user = new User();
        PatientProfile profile = new PatientProfile(user);

        profile.updateNutritionProfile(
                new Height(180),
                new Weight(70000),
                ActivityLevel.ACTIVE,
                GoalType.WEIGHT_MAINTENANCE
        );

        PatientProfileHistory history = new PatientProfileHistory(profile);

        PatientProfileHistoryResponseDTO dto =
                PatientProfileHistoryMapper.toResponseDTO(history);

        assertNotNull(dto);
        assertTrue(dto.restrictions().isEmpty());
    }

    @Test
    void toSummaryDTO_shouldMapCorrectly() {
        User user = new User();
        PatientProfile profile = new PatientProfile(user);

        profile.updateNutritionProfile(
                new Height(175),
                new Weight(80000),
                ActivityLevel.SEDENTARY,
                GoalType.MUSCLE_GAIN
        );

        PatientProfileHistory history = new PatientProfileHistory(profile);

        PatientProfileHistorySummaryDTO dto =
                PatientProfileHistoryMapper.toSummaryDTO(history);

        assertNotNull(dto);
        assertEquals(history.getId(), dto.id());
        assertEquals(history.getPatientProfile().getId(), dto.patientProfileId());
        assertEquals(history.getVisitDate(), dto.visitDate());
        assertEquals(80000, dto.weight());
    }
}