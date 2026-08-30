package com.lirium.nutrition.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.lirium.nutrition.dto.request.RestrictionCatalogUpdateDTO;
import com.lirium.nutrition.dto.request.RestrictionCreateRequestDTO;
import com.lirium.nutrition.dto.response.RestrictionResponseDTO;
import com.lirium.nutrition.dto.response.RestrictionSummaryDTO;
import com.lirium.nutrition.exception.ResourceNotFoundException;
import com.lirium.nutrition.exception.RestrictionNotFoundException;
import com.lirium.nutrition.mapper.RestrictionMapper;
import com.lirium.nutrition.model.entity.Restriction;
import com.lirium.nutrition.model.enums.RestrictionCategory;
import com.lirium.nutrition.repository.RestrictionRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RestrictionServiceImplTest {

  @Mock private RestrictionRepository restrictionRepository;
  @Mock private RestrictionMapper restrictionMapper;
  @InjectMocks private RestrictionServiceImpl restrictionService;

  @Test
  void shouldReturnAllRestrictions() {

    // Given
    Restriction restriction1 = mock(Restriction.class);
    Restriction restriction2 = mock(Restriction.class);

    RestrictionSummaryDTO dto1 = mock(RestrictionSummaryDTO.class);
    RestrictionSummaryDTO dto2 = mock(RestrictionSummaryDTO.class);

    when(restrictionRepository.findAll()).thenReturn(List.of(restriction1, restriction2));

    when(restrictionMapper.toSummaryDTO(restriction1)).thenReturn(dto1);

    when(restrictionMapper.toSummaryDTO(restriction2)).thenReturn(dto2);

    // When
    Set<RestrictionSummaryDTO> result = restrictionService.findAll();

    // Then
    assertEquals(2, result.size());
    assertTrue(result.contains(dto1));
    assertTrue(result.contains(dto2));

    verify(restrictionRepository).findAll();
    verify(restrictionMapper).toSummaryDTO(restriction1);
    verify(restrictionMapper).toSummaryDTO(restriction2);
    verify(restrictionMapper, times(2)).toSummaryDTO(any());
  }

  @Test
  void shouldReturnRestrictionById() {

    // Given
    Long id = 1L;

    Restriction restriction = mock(Restriction.class);
    RestrictionResponseDTO responseDTO = mock(RestrictionResponseDTO.class);

    when(restrictionRepository.findById(id)).thenReturn(Optional.of(restriction));

    when(restrictionMapper.toResponseDTO(restriction)).thenReturn(responseDTO);

    // When
    RestrictionResponseDTO result = restrictionService.findById(id);

    // Then
    assertEquals(responseDTO, result);

    verify(restrictionRepository).findById(id);
    verify(restrictionMapper).toResponseDTO(restriction);
  }

  @Test
  void shouldThrowWhenRestrictionNotFound() {

    // Given
    Long id = 1L;

    when(restrictionRepository.findById(id)).thenReturn(Optional.empty());

    // When + Then
    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> restrictionService.findById(id));

    // Assert del mensaje (importante para negocio)
    assertTrue(ex.getMessage().contains("Restriction"));

    verify(restrictionRepository).findById(id);
    verifyNoInteractions(restrictionMapper);
  }

  @Test
  void shouldCreateRestriction() {

    // Given
    RestrictionCreateRequestDTO request =
        new RestrictionCreateRequestDTO(
            "GLUTEN", "Gluten intolerance", "INTOLERANCES", RestrictionCategory.INTOLERANCES);

    Restriction savedRestriction = mock(Restriction.class);
    RestrictionSummaryDTO summaryDTO = mock(RestrictionSummaryDTO.class);

    when(restrictionRepository.save(any(Restriction.class))).thenReturn(savedRestriction);

    when(restrictionMapper.toSummaryDTO(savedRestriction)).thenReturn(summaryDTO);

    // When
    RestrictionSummaryDTO result = restrictionService.create(request);

    // Then
    assertEquals(summaryDTO, result);

    verify(restrictionRepository).save(any(Restriction.class));
    verify(restrictionMapper).toSummaryDTO(savedRestriction);
  }

  @Test
  void shouldUpdateRestriction() {

    // Given
    Long id = 1L;

    Restriction existingRestriction =
        Restriction.builder()
            .id(id)
            .code("LACTOSE")
            .name("Lactose")
            .description("Avoid lactose")
            .category(RestrictionCategory.INTOLERANCES)
            .build();

    RestrictionCatalogUpdateDTO request =
        new RestrictionCatalogUpdateDTO(
            "LACTOSE",
            "Lactose Intolerance",
            "Avoid dairy products",
            RestrictionCategory.INTOLERANCES);

    RestrictionSummaryDTO responseDTO =
        new RestrictionSummaryDTO(
            id, "LACTOSE", "Lactose Intolerance", RestrictionCategory.INTOLERANCES);

    when(restrictionRepository.findById(id)).thenReturn(Optional.of(existingRestriction));

    when(restrictionMapper.toSummaryDTO(existingRestriction)).thenReturn(responseDTO);

    // When
    RestrictionSummaryDTO result = restrictionService.update(id, request);

    // Then
    assertNotNull(result);
    assertEquals(responseDTO, result);
    assertEquals("Lactose Intolerance", existingRestriction.getName());
    assertEquals("Avoid dairy products", existingRestriction.getDescription());

    verify(restrictionRepository).findById(id);
    verify(restrictionMapper).toSummaryDTO(existingRestriction);
  }

  @Test
  void shouldThrowWhenUpdatingNonExistingRestriction() {

    // Given
    Long id = 1L;

    RestrictionCatalogUpdateDTO request =
        new RestrictionCatalogUpdateDTO(
            "LACTOSE",
            "Lactose Intolerance",
            "Avoid dairy products",
            RestrictionCategory.INTOLERANCES);

    when(restrictionRepository.findById(id)).thenReturn(Optional.empty());

    // When + Then
    RestrictionNotFoundException ex =
        assertThrows(
            RestrictionNotFoundException.class, () -> restrictionService.update(id, request));

    assertTrue(ex.getMessage().contains("Restriction not found with id: " + id));

    verify(restrictionRepository).findById(id);
    verifyNoMoreInteractions(restrictionRepository);
    verifyNoInteractions(restrictionMapper);
  }

  @Test
  void shouldThrowWhenDeletingNonExistingRestriction() {

    // Given
    Long id = 1L;

    when(restrictionRepository.findById(id)).thenReturn(Optional.empty());

    // When + Then
    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> restrictionService.deleteById(id));

    assertTrue(ex.getMessage().contains("Restriction"));

    verify(restrictionRepository).findById(id);

    verify(restrictionRepository, never()).delete(any());
  }

  @Test
  void shouldDeleteRestrictionSuccessfully() {

    Restriction restriction = mock(Restriction.class);

    when(restrictionRepository.findById(1L)).thenReturn(Optional.of(restriction));

    restrictionService.deleteById(1L);

    verify(restrictionRepository).delete(restriction);
  }

  @Test
  void shouldCreateRestrictionWhenValid() {

    // Given
    RestrictionCreateRequestDTO request =
        new RestrictionCreateRequestDTO(
            "GLUTEN", "Gluten intolerance", "INTOLERANCES", RestrictionCategory.INTOLERANCES);

    Restriction savedRestriction = mock(Restriction.class);
    RestrictionSummaryDTO summaryDTO = mock(RestrictionSummaryDTO.class);

    when(restrictionRepository.save(any(Restriction.class))).thenReturn(savedRestriction);

    when(restrictionMapper.toSummaryDTO(savedRestriction)).thenReturn(summaryDTO);

    // When
    RestrictionSummaryDTO result = restrictionService.create(request);

    // Then
    assertEquals(summaryDTO, result);

    verify(restrictionRepository).save(any(Restriction.class));
    verify(restrictionMapper).toSummaryDTO(savedRestriction);
  }
}
