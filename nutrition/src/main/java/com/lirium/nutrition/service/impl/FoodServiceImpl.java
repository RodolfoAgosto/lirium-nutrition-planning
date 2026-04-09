package com.lirium.nutrition.service.impl;

import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;
import com.lirium.nutrition.exception.DuplicateFoodException;
import com.lirium.nutrition.exception.FoodInUseException;
import com.lirium.nutrition.exception.InvalidTagException;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.mapper.FoodMapper;
import com.lirium.nutrition.model.entity.Food;
import com.lirium.nutrition.model.enums.FoodTag;
import com.lirium.nutrition.repository.FoodRepository;
import com.lirium.nutrition.service.FoodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;

    @Override
    public Set<FoodSummaryDTO> findAll() {

        return foodRepository.findAll()
                .stream()
                .map(FoodMapper::toSummary)
                .collect(Collectors.toSet());
    }

    @Override
    public FoodResponseDTO findById(Long id) {

        Food food = getFoodOrThrow(id);

        return FoodMapper.toResponse(food);
    }

    @Override
    @Transactional
    public FoodSummaryDTO create(FoodCreateRequestDTO dto) {

        log.info("Creating food name={}", dto.name());

        if (foodRepository.existsByName(dto.name())) {
            log.warn("Food creation failed - duplicate name={}", dto.name());
            throw new DuplicateFoodException("Food already exists: " + dto.name());
        }

        log.debug("Food create payload={}", dto);

        Food food = FoodMapper.toEntity(dto);
        foodRepository.save(food);

        log.info("Food created successfully name={}", dto.name());

        return FoodMapper.toSummary(food);

    }

    @Override
    @Transactional
    public FoodSummaryDTO update(Long id, FoodUpdateRequestDTO dto) {

        log.info("Updating food id={}", id);

        Food food = getFoodOrThrow(id);

        if (dto.name() != null &&
                !dto.name().equals(food.getName()) &&
                foodRepository.existsByName(dto.name())) {
            log.warn("Food update failed - duplicate name={} id={}", dto.name(), id);
            throw new DuplicateFoodException("Food already exists: " + dto.name());
        }

        log.debug("Food update payload id={} data={}", id, dto);

        food.changeName(dto.name());
        food.changeCalories(dto.caloriesPer100g());
        food.changeCarbs(dto.carbsPer100g());
        food.changeFat(dto.fatPer100g());
        food.changeProtein(dto.proteinPer100g());
        food.replaceTags(toFoodTags(dto.tags()));

        log.info("Food updated successfully id={}", id);

        return FoodMapper.toSummary(food);

    }

    @Override
    @Transactional
    public void deleteById(Long id) {

        log.info("Deleting food id={}", id);

        Food food = getFoodOrThrow(id);
        try {
            foodRepository.delete(food);
            foodRepository.flush();

            log.info("Food deleted successfully id={}", id);
        } catch (DataIntegrityViolationException e) {

            log.error("Food deletion failed - food in use id={}", id);
            throw new FoodInUseException("Food is in use and cannot be deleted", id);

        }
    }

    private Food getFoodOrThrow(Long id) {

        return foodRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Food not found id={}", id);
                    return new ResourceNotFoundException("Food", id);
                });

    }

    private Set<FoodTag> toFoodTags(Set<String> tags) {
        if (tags == null || tags.isEmpty()) return Set.of();
        return tags.stream()
                .map(tag -> {
                    try {
                        return FoodTag.valueOf(tag.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid food tag received tag={}", tag);
                        throw new InvalidTagException("Invalid tag: " + tag);
                    }
                })
                .collect(Collectors.toSet());
    }

    @Override
    public Food findEntityById(Long id) {

        return foodRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Food entity not found id={}", id);
                    return new ResourceNotFoundException("Food", id);
                });

    }

}