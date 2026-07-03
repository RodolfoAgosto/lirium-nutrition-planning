package com.lirium.nutrition.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.lirium.nutrition.dto.request.PatientProfileCreateRequestDTO;
import com.lirium.nutrition.dto.request.PatientProfileUpdateRequestDTO;
import com.lirium.nutrition.dto.response.PatientProfileResponseDTO;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.ActivityLevel;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import com.lirium.nutrition.model.enums.Sex;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import java.util.Set;
import java.util.List;

import org.junit.jupiter.api.Test;

class PatientProfileMapperTest {

    @Test
    void shouldMapToEntityCorrectly() {

        // given
        User user = new User();
        user.setId(1L);

        Restriction r1 = Restriction.builder().id(1L).name("SALT").build();
        Restriction r2 = Restriction.builder().id(2L).name("SUGAR").build();

        PatientProfileCreateRequestDTO dto = new PatientProfileCreateRequestDTO(
                1L,
                Sex.MALE,
                ActivityLevel.MODERATE,
                70000,
                180,
                "notes",
                Set.of(1L, 2L),
                List.of(),
                GoalType.WEIGHT_LOSS
        );

        Set<Restriction> restrictions = Set.of(r1, r2);

        // when
        PatientProfile profile =
                PatientProfileMapper.toEntity(dto, user, restrictions);

        // then
        assertNotNull(profile);
        assertEquals(user, profile.getUser());
        assertEquals(70000, profile.getWeight().grams());
        assertEquals(180, profile.getHeight().cm());
        assertEquals("notes", profile.getMedicalNotes());
        assertEquals(GoalType.WEIGHT_LOSS, profile.getPrimaryGoal());

        assertEquals(
                Set.of("SALT", "SUGAR"),
                profile.getRestrictions()
                        .stream()
                        .map(Restriction::getName)
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    @Test
    void shouldMapUpdateEntityCorrectly() {

        // given
        User user = new User();

        PatientProfile profile = new PatientProfile(user);

        PatientProfileUpdateRequestDTO dto = new PatientProfileUpdateRequestDTO(
                Sex.FEMALE,
                ActivityLevel.ACTIVE,
                65000,
                170,
                "updated notes",
                Set.of(1L, 2L),
                List.of(PhysiologicalCondition.LACTATION),
                GoalType.MUSCLE_GAIN
        );

        Restriction r1 = Restriction.builder().id(1L).name("SALT").build();
        Restriction r2 = Restriction.builder().id(2L).name("SUGAR").build();

        Set<Restriction> restrictions = Set.of(r1, r2);

        // when
        PatientProfileMapper.updateEntity(profile, dto, restrictions);

        // then
        assertEquals(Sex.FEMALE, profile.getSex());
        assertEquals(ActivityLevel.ACTIVE, profile.getActivityLevel());
        assertEquals(65000, profile.getWeight().grams());
        assertEquals(170, profile.getHeight().cm());
        assertEquals("updated notes", profile.getMedicalNotes());
        assertEquals(GoalType.MUSCLE_GAIN, profile.getPrimaryGoal());

        assertEquals(2, profile.getRestrictions().size());
    }

    @Test
    void shouldMapToResponseCorrectly() throws Exception {

        // given
        User user = new User();
        user.setId(10L);

        Restriction r1 = Restriction.builder().id(1L).name("SALT").build();
        Restriction r2 = Restriction.builder().id(2L).name("SUGAR").build();

        PatientProfile profile = new PatientProfile(user);

        profile.update(
                Sex.MALE,
                ActivityLevel.MODERATE,
                Weight.of(70000),
                Height.of(180),
                "notes",
                Set.of(r1, r2),
                List.of(),
                GoalType.LACTATION_HEALTH
        );

        setId(profile, 99L);

        // when
        PatientProfileResponseDTO dto =
                PatientProfileMapper.toResponse(profile);

        // then
        assertEquals(99L, dto.id());
        assertEquals(10L, dto.userId());
        assertEquals(70000, dto.weight());
        assertEquals(180, dto.height());
        assertEquals("notes", dto.medicalNotes());
        assertEquals(GoalType.LACTATION_HEALTH, dto.primaryGoal());

        assertEquals(Set.of("SALT", "SUGAR"), dto.restrictions());
    }

    static void setId(Object entity, Long id) throws Exception {
        Field field = entity.getClass().getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
    }
}