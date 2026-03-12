package com.lirium.nutrition.mapper;

        import com.lirium.nutrition.dto.request.*;
        import com.lirium.nutrition.dto.response.*;
        import com.lirium.nutrition.model.entity.Restriction;
        import org.mapstruct.*;

        import java.util.List;
        import java.util.Set;

@Mapper(componentModel = "spring")
public interface RestrictionMapper {

    // === ENTITY → DTO ===

    RestrictionResponseDTO toResponseDTO(Restriction restriction);

    RestrictionSummaryDTO toSummaryDTO(Restriction restriction);

    // === DTO → ENTITY ===

    Restriction toEntity(RestrictionCreateRequestDTO dto);

    // === UPDATE ===
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDTO(RestrictionUpdateDTO dto, @MappingTarget Restriction entity);

    // === Set<DTO> - Set<ENTITY>
    Set<Restriction> toEntitySet(Set<RestrictionUpdateDTO> dtoSet);

    Set<RestrictionSummaryDTO> toDTOSet(Set<Restriction> restrictions);

}