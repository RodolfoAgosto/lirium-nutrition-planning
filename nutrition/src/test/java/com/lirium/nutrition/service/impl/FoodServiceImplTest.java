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
import com.lirium.nutrition.model.enums.FoodCategory;
import com.lirium.nutrition.model.enums.MealType;
import com.lirium.nutrition.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceImplTest {

    @Mock
    private FoodRepository foodRepository;
    @InjectMocks
    private FoodServiceImpl service;

    @Test
    void shouldReturnAllFood(){

        // Given
        List<Food> foodList = List.of(
                Food.of("Chicken Breast", 165, 31, 0, 4,
                        FoodCategory.PROTEIN,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Salmon", 208, 20, 0, 13,
                        FoodCategory.PROTEIN,
                        Set.of(MealType.LUNCH, MealType.DINNER)),

                Food.of("Tuna in Water", 116, 26, 0, 1,
                        FoodCategory.PROTEIN,
                        Set.of(MealType.LUNCH, MealType.DINNER))
                );
        when(foodRepository.findAll()).thenReturn(foodList);

        // When
        Set<FoodSummaryDTO> foodSummaryDTOList = service.findAll();

        //Then
        assertEquals(3, foodSummaryDTOList.size());


    }

    @Test
    void shouldUpdateFoodSuccessfully() {

        // Given
        Long id = 1L;

        Food food = Food.of(
                "Chicken Breast",
                165,
                31,
                0,
                4,
                FoodCategory.PROTEIN,
                Set.of(MealType.LUNCH)
        );

        FoodUpdateRequestDTO dto = new FoodUpdateRequestDTO(
                "Salmon",
                208,
                0,
                13,
                20,
                Set.of("FISH", "SOY")
        );

        when(foodRepository.findById(id))
                .thenReturn(Optional.of(food));

        when(foodRepository.existsByName("Salmon"))
                .thenReturn(false);

        // When
        FoodSummaryDTO result = service.update(id, dto);

        // Then
        assertEquals("Salmon", result.name());

        verify(foodRepository).findById(id);
        verify(foodRepository).existsByName("Salmon");
    }

    @Test
    void shouldDeleteFoodSuccessfully() {

        // Given
        Long id = 1L;

        Food food = Food.of(
                "Chicken Breast",
                165,
                31,
                0,
                4,
                FoodCategory.PROTEIN,
                Set.of(MealType.LUNCH)
        );

        when(foodRepository.findById(id))
                .thenReturn(Optional.of(food));

        // When
        service.deleteById(id);

        // Then
        verify(foodRepository).delete(food);
        verify(foodRepository).flush();
    }

    @Test
    void shouldReturnMappedFoodWhenIdExists() {

        // Given
        Long id = 37L;

        Food food = Food.of("Chicken Breast", 165, 31, 0, 4,
                 FoodCategory.PROTEIN,
                 Set.of(MealType.LUNCH, MealType.DINNER));
        when(foodRepository.findById(id)).thenReturn(Optional.of(food));

        // When
        FoodResponseDTO foodResponse = service.findById(37L);

        // Then
        assertAll(
                () -> assertEquals(food.getCaloriesPer100g(), foodResponse.caloriesPer100g()),
                () -> assertEquals(food.getProteinPer100g(), foodResponse.proteinPer100g()),
                () -> assertEquals(food.getName(), foodResponse.name())
        );
    }

    @Test
    void shouldThrowWhenCreatingDuplicateFood() {

        // Given
        FoodCreateRequestDTO request =
                new FoodCreateRequestDTO(
                        "Apple",
                        52,
                        1,
                        14,
                        0,
                        null,
                        Set.of(),
                        Set.of()
                );

        when(foodRepository.existsByName("Apple"))
                .thenReturn(true);

        // When + Then
        DuplicateFoodException ex = assertThrows(
                DuplicateFoodException.class,
                () -> service.create(request)
        );

        assertTrue(ex.getMessage().contains("Apple"));

        verify(foodRepository).existsByName("Apple");

        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void shouldThrowWhenUpdatingFoodWithDuplicateName() {

        // Given
        Long foodId = 1L;

        Food food = mock(Food.class);

        when(food.getName()).thenReturn("Apple");

        FoodUpdateRequestDTO request =
                new FoodUpdateRequestDTO(
                        "Banana",
                        89,
                        1,
                        23,
                        0,
                        null
                );

        when(foodRepository.findById(foodId))
                .thenReturn(Optional.of(food));

        when(foodRepository.existsByName("Banana"))
                .thenReturn(true);

        // When + Then
        DuplicateFoodException ex = assertThrows(
                DuplicateFoodException.class,
                () -> service.update(foodId, request)
        );

        assertTrue(ex.getMessage().contains("Banana"));

        verify(foodRepository).findById(foodId);
        verify(foodRepository).existsByName("Banana");

        verify(food, never()).changeName(any());
    }

    @Test
    void shouldThrowWhenUpdatingFoodWithInvalidTag() {

        // Given
        Long foodId = 1L;

        Food food = mock(Food.class);

        when(foodRepository.findById(foodId))
                .thenReturn(Optional.of(food));

        FoodUpdateRequestDTO request =
                new FoodUpdateRequestDTO(
                        "Apple",
                        52,
                        1,
                        14,
                        0,
                        Set.of("TAG_QUE_NO_EXISTE")
                );

        // When + Then
        InvalidTagException ex = assertThrows(
                InvalidTagException.class,
                () -> service.update(foodId, request)
        );

        assertTrue(
                ex.getMessage().contains("TAG_QUE_NO_EXISTE")
        );

        verify(foodRepository).findById(foodId);

        verify(food, never()).replaceTags(any());
    }

    @Test
    void shouldThrowFoodInUseExceptionWhenDeletingFoodInUse() {

        // Given
        Long foodId = 1L;

        Food food = mock(Food.class);

        when(foodRepository.findById(foodId))
                .thenReturn(Optional.of(food));

        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(foodRepository)
                .delete(food);

        // When + Then
        FoodInUseException ex = assertThrows(
                FoodInUseException.class,
                () -> service.deleteById(foodId)
        );

        assertTrue(
                ex.getMessage().contains("cannot be deleted")
        );

        verify(foodRepository).findById(foodId);
        verify(foodRepository).delete(food);

        verify(foodRepository, never()).flush();
    }

    @Test
    void shouldThrowWhenFoodNotFound() {

        // Given
        Long foodId = 1L;

        when(foodRepository.findById(foodId))
                .thenReturn(Optional.empty());

        // When + Then
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.findById(foodId)
        );

        assertTrue(ex.getMessage().contains("Food"));

        verify(foodRepository).findById(foodId);
    }

    @Test
    void shouldThrowWhenFoodEntityNotFound() {

        Long foodId = 1L;

        when(foodRepository.findById(foodId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.findEntityById(foodId)
        );

        verify(foodRepository).findById(foodId);
    }

    @Test
    void shouldUpdateFoodSuccessfullyWhenNameDoesNotChange() {

        Long id = 1L;

        Food food = mock(Food.class);

        when(food.getName()).thenReturn("Apple");

        when(foodRepository.findById(id))
                .thenReturn(Optional.of(food));

        FoodUpdateRequestDTO dto =
                new FoodUpdateRequestDTO(
                        "Apple",
                        100,
                        10,
                        20,
                        5,
                        Set.of()
                );

        service.update(id, dto);

        verify(foodRepository, never())
                .existsByName(any());
    }

}