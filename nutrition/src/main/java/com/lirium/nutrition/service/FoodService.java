package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;

import java.util.Set;

public interface FoodService {

    Set<FoodSummaryDTO> findAll();

    FoodResponseDTO findById(Long id);

    FoodSummaryDTO create(FoodCreateRequestDTO dto);

    FoodSummaryDTO update(Long id, FoodUpdateRequestDTO dto);

    void deleteById(Long id);
}