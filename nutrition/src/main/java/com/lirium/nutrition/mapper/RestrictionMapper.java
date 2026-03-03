package com.lirium.nutrition.mapper;

        import com.lirium.nutrition.dto.request.*;
        import com.lirium.nutrition.dto.response.*;
        import com.lirium.nutrition.model.entity.Restriction;
        import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RestrictionMapper {

    // === ENTITY → DTO ===

    RestrictionResponseDTO toResponseDTO(Restriction restriction);

    RestrictionSummaryDTO toSummaryDTO(Restriction restriction);

    // === DTO → ENTITY ===

    Restriction toEntity(RestrictionCreateDTO dto);

    // === UPDATE ===
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(RestrictionUpdateDTO dto, @MappingTarget Restriction entity);
}