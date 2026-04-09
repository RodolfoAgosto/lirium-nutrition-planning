package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.RestrictionResponseDTO;
import com.lirium.nutrition.dto.response.RestrictionSummaryDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.RestrictionMapper;
import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import com.lirium.nutrition.repository.RestrictionRepository;
import com.lirium.nutrition.service.RestrictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RestrictionServiceImpl implements RestrictionService {

    private final RestrictionRepository restrictionRepository;
    private final RestrictionMapper restrictionMapper;

    @Override
    public Set<RestrictionSummaryDTO> findAll() {

        return restrictionRepository.findAll()
                .stream()
                .map(restrictionMapper::toSummaryDTO)
                .collect(Collectors.toSet());
    }

    @Override
    public RestrictionResponseDTO findById(Long id) {

        Restriction restriction = getRestrictionOrThrow(id);
        return restrictionMapper.toResponseDTO(restriction);

    }

    @Override
    @Transactional
    public RestrictionSummaryDTO create(RestrictionCreateRequestDTO dto) {

        log.info("Creating restriction code={} name={}", dto.code(), dto.name());

        Restriction restriction = new Restriction();

        restriction.setCode(dto.code());
        restriction.setName(dto.name());
        restriction.setDescription(dto.description());
        restriction.setCategory(RestrictionCategory.valueOf(dto.category()));

        Restriction saved = restrictionRepository.save(restriction);

        log.info("Restriction created successfully id={} code={}",
                saved.getId(), saved.getCode());

        return restrictionMapper.toSummaryDTO(saved);

    }

    @Override
    @Transactional
    public RestrictionSummaryDTO update(Long id, RestrictionCatalogUpdateDTO dto) {

        log.info("Updating restriction id={}", id);

        Restriction restriction = restrictionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Restriction not found id={}", id);
                    return new ResourceNotFoundException("Restriction", id);
                });

        if (log.isDebugEnabled()) {
            log.debug("Update payload id={} code={} category={}",
                    id, dto.code(), dto.category());
        }

        restriction.setCode(dto.code());
        restriction.setName(dto.name());
        restriction.setDescription(dto.description());
        restriction.setCategory(RestrictionCategory.valueOf(dto.category()));

        restrictionRepository.save(restriction);

        log.info("Restriction updated successfully id={}", id);

        return restrictionMapper.toSummaryDTO(restriction);

    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        Restriction restriction = restrictionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restriction", id));

        restrictionRepository.delete(restriction);
    }

    private Restriction getRestrictionOrThrow(Long id) {
        return restrictionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restriction", id));
    }

}