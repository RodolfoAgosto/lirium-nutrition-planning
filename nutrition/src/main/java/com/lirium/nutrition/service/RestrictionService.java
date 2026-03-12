package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.request.RestrictionCatalogUpdateDTO;
import com.lirium.nutrition.dto.request.RestrictionCreateRequestDTO;
import com.lirium.nutrition.dto.response.RestrictionResponseDTO;
import com.lirium.nutrition.dto.response.RestrictionSummaryDTO;
import com.lirium.nutrition.dto.request.RestrictionUpdateDTO;
import com.lirium.nutrition.model.entity.Restriction;

import java.util.Set;

public interface RestrictionService {

    Set<RestrictionSummaryDTO> findAll();

    RestrictionResponseDTO findById(Long id);

    RestrictionSummaryDTO create(RestrictionCreateRequestDTO dto);

    RestrictionSummaryDTO update(Long id, RestrictionCatalogUpdateDTO dto);

    void deleteById(Long id);

}

