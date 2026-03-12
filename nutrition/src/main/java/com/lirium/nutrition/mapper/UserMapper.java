package com.lirium.nutrition.mapper;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.model.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // ---------- ENTITY → DTO ----------

    UserResponseDTO toResponseDTO(User user);

    UserSummaryDTO toSummaryDTO(User user);

    // ---------- CREATE DTO → ENTITY ----------

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "emailValidated", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "patientProfile", ignore = true)
    User toEntity(CreateUserRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "emailValidated", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "patientProfile", ignore = true)
    User toEntity(CreatePatientRequestDTO dto);


    // ---------- UPDATE DTO → ENTITY ----------

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "dni", ignore = true)
    @Mapping(target = "emailValidated", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "patientProfile", ignore = true)
    void updateUserFromDTO(UserUpdateRequestDTO dto, @MappingTarget User user);

}