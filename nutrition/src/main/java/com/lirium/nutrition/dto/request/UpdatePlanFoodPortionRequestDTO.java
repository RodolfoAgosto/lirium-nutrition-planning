package com.lirium.nutrition.dto.request;

public record UpdatePlanFoodPortionRequestDTO(
     Long foodId,
     Double quantity
) { }
