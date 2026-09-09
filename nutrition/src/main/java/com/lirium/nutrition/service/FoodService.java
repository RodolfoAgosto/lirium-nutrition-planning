package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.FoodCreateRequestDTO;
import com.lirium.nutrition.dto.request.FoodUpdateRequestDTO;
import com.lirium.nutrition.dto.response.FoodResponseDTO;
import com.lirium.nutrition.dto.response.FoodSummaryDTO;
import com.lirium.nutrition.model.entity.Food;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FoodService {

  Food findEntityById(Long id);

  Set<FoodSummaryDTO> findAll();

  Page<FoodSummaryDTO> findAll(Pageable pageable);

  FoodResponseDTO findById(Long id);

  FoodSummaryDTO create(FoodCreateRequestDTO dto);

  FoodSummaryDTO update(Long id, FoodUpdateRequestDTO dto);

  void deleteById(Long id);
}
