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
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NutritionPlanTemplateServiceImpl implements NutritionPlanTemplateService {

    private final NutritionPlanTemplateRepository repository;

    @Override
    public List<NutritionPlanTemplateSummaryDTO> getAll() {

        log.info("Fetching all nutrition plan templates");

        List<NutritionPlanTemplateSummaryDTO> result = repository.findAll()
                .stream()
                .map(NutritionPlanTemplateMapper::toSummary)
                .toList();

        log.info("Templates fetched count={}", result.size());

        return result;

    }

    @Override
    public NutritionPlanTemplateResponseDTO getById(Long id) {
        return NutritionPlanTemplateMapper.toResponse(getOrThrow(id));
    }

    @Override
    @Transactional
    public NutritionPlanTemplateResponseDTO create(NutritionPlanTemplateCreateRequestDTO dto) {

        log.info("Creating template name={}", dto.name());

        if (repository.existsByName(dto.name())) {
            log.warn("Template creation failed - duplicate name={}", dto.name());
            throw new DuplicateTemplateException("Template already exists: " + dto.name());
        }

        NutritionPlanTemplate saved = repository.save(
                NutritionPlanTemplateMapper.toEntity(dto)
        );

        log.info("Template created successfully id={} name={}", saved.getId(), saved.getName());

        return NutritionPlanTemplateMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NutritionPlanTemplateResponseDTO update(Long id, NutritionPlanTemplateUpdateRequestDTO dto) {

        log.info("Updating template id={}", id);

        NutritionPlanTemplate template = getOrThrow(id);

        if (dto.name() != null
                && !dto.name().equals(template.getName())
                && repository.existsByName(dto.name())) {
            log.warn("Template update failed - duplicate name={} id={}", dto.name(), id);
            throw new DuplicateTemplateException("Template already exists: " + dto.name());
        }

        log.debug("Update payload id={} name={} goal={} macrosUpdated={}", id, dto.name(), dto.targetGoal(), dto.proteinPercentage() != null);

        template.update(dto.name(), dto.description(), dto.targetGoal(), dto.excludedTags());

        if (dto.proteinPercentage() != null) {
            template.updateMacros(dto.proteinPercentage(),
                    dto.carbPercentage(), dto.fatPercentage());
        }

        log.info("Template updated successfully id={}", id);

        return NutritionPlanTemplateMapper.toResponse(template);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting template id={}", id);
        repository.delete(getOrThrow(id));
        log.info("Template deleted successfully id={}", id);
    }

    private NutritionPlanTemplate getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));
    }
}