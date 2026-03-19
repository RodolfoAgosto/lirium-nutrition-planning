package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.NutritionPlanTemplateCreateRequestDTO;
import com.lirium.nutrition.dto.request.NutritionPlanTemplateUpdateRequestDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateResponseDTO;
import com.lirium.nutrition.dto.response.NutritionPlanTemplateSummaryDTO;
import com.lirium.nutrition.exception.DuplicateTemplateException;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.NutritionPlanTemplateMapper;
import com.lirium.nutrition.model.entity.NutritionPlanTemplate;
import com.lirium.nutrition.repository.NutritionPlanTemplateRepository;
import com.lirium.nutrition.service.NutritionPlanTemplateService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionPlanTemplateServiceImpl implements NutritionPlanTemplateService {

    private final NutritionPlanTemplateRepository repository;

    @Override
    public List<NutritionPlanTemplateSummaryDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(NutritionPlanTemplateMapper::toSummary)
                .toList();
    }

    @Override
    public NutritionPlanTemplateResponseDTO getById(Long id) {
        return NutritionPlanTemplateMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public NutritionPlanTemplateResponseDTO create(
            NutritionPlanTemplateCreateRequestDTO dto) {

        if (repository.existsByName(dto.name())) {
            throw new DuplicateTemplateException("Template already exists: " + dto.name());
        }

        NutritionPlanTemplate saved = repository.save(
                NutritionPlanTemplateMapper.toEntity(dto)
        );

        return NutritionPlanTemplateMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NutritionPlanTemplateResponseDTO update(
            Long id, NutritionPlanTemplateUpdateRequestDTO dto) {

        NutritionPlanTemplate template = getOrThrow(id);

        if (dto.name() != null
                && !dto.name().equals(template.getName())
                && repository.existsByName(dto.name())) {
            throw new DuplicateTemplateException("Template already exists: " + dto.name());
        }

        template.update(dto.name(), dto.description(),
                dto.targetGoal(), dto.excludedTags());

        if (dto.proteinPercentage() != null) {
            template.updateMacros(dto.proteinPercentage(),
                    dto.carbPercentage(), dto.fatPercentage());
        }

        return NutritionPlanTemplateMapper.toResponse(template);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private NutritionPlanTemplate getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));
    }
}