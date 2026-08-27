package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.FoodPortionAddRequestDTO;
import com.lirium.nutrition.dto.request.PlanFoodPortionUpdateQuantityRequestDTO;
import com.lirium.nutrition.dto.request.PlanMealCreateRequestDTO;
import com.lirium.nutrition.dto.response.PlanMealResponseDTO;
import com.lirium.nutrition.dto.response.PlanMealSummaryDTO;
import com.lirium.nutrition.service.PlanMealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/plan-meals")
@RequiredArgsConstructor
@Validated
@Tag(name = "Plan Meals", description = "Endpoints for managing meal structures and portions within nutrition plan days")
@SecurityRequirement(name = "bearerAuth")
public class PlanMealController {

    private final PlanMealService service;

    @Operation(
            summary = "Get plan meal by ID",
            description = "Retrieves full details of a specific plan meal. Accessible by ADMIN, NUTRITIONIST, or the owner PATIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan meal retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlanMealResponseDTO.class)
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @planMealSecurity.isOwner(#id, authentication)")
    @GetMapping("/{id}")
    public ResponseEntity<PlanMealResponseDTO> getById(@PathVariable @P("id") @Positive Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(
            summary = "Get plan meals by plan day ID",
            description = "Retrieves all planned meals assigned to a specific plan day. Accessible by ADMIN, NUTRITIONIST, or the owner PATIENT."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan meals retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PlanMealSummaryDTO.class))
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN','NUTRITIONIST') or @planMealSecurity.isPlanDayOwner(#planDayId, authentication)")
    @GetMapping("/day/{planDayId}")
    public List<PlanMealSummaryDTO> getByPlanDay(@PathVariable("planDayId") @Positive Long planDayId) {
        return service.getByPlanDay(planDayId);
    }

    @Operation(
            summary = "Create a new plan meal",
            description = "Creates a new planned meal within a daily plan."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Plan meal created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlanMealResponseDTO.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<PlanMealResponseDTO> create(@Valid @RequestBody PlanMealCreateRequestDTO dto){

        log.info("Creating plan meal for dailyPlanDayId={}", dto.dailyPlanId());
        log.debug("PlanMeal create payload={}", dto);
        PlanMealResponseDTO response = service.create(dto);
        log.info("Plan meal created successfully for dailyPlanDayId={}", dto.dailyPlanId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @Operation(
            summary = "Delete a plan meal",
            description = "Deletes an existing plan meal by its ID. Restricted to ADMIN and NUTRITIONIST users."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Plan meal deleted successfully"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST')")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long id) {

        log.info("Deleting plan meal id={}", id);
        service.delete(id);
        log.info("Plan meal deleted successfully id={}", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Add a food portion to a plan meal",
            description = "Adds a new food portion to an existing planned meal."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Food portion added successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlanMealResponseDTO.class)
                    )
            )
    })
    @PostMapping("/{mealId}/portions")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<PlanMealResponseDTO> addPortion(@PathVariable @Positive Long mealId,
                                          @Valid @RequestBody FoodPortionAddRequestDTO dto) {
        log.info("Adding food portion to plan mealId={} with foodId={}", mealId, dto.foodId());
        log.debug("Portion payload={}", dto);
        PlanMealResponseDTO response = service.addPortion(mealId, dto);
        log.info("Food portion added successfully to plan mealId={}", mealId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Remove a food portion from a plan meal",
            description = "Removes a specific food portion from a planned meal."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Portion removed successfully and the updated meal is returned",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlanMealResponseDTO.class)
                    )
            )
    })
    @DeleteMapping("/{mealId}/portions/{portionId}")
    public ResponseEntity<PlanMealResponseDTO> removePortion(@PathVariable @Positive Long mealId,
                                             @PathVariable @Positive Long portionId) {
        log.info("Removing portionId={} from plan mealId={}", portionId, mealId);
        PlanMealResponseDTO response = service.removePortion(mealId, portionId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update food portion quantity",
            description = "Updates the quantity of a specific food portion within a plan meal. Restricted to ADMIN and NUTRITIONIST users."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Portion quantity updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlanMealResponseDTO.class)
                    )
            )
    })
    @PatchMapping("/{mealId}/portions/{portionId}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'NUTRITIONIST')")
    public ResponseEntity<PlanMealResponseDTO> updateQuantity(
            @PathVariable @Positive Long mealId,
            @PathVariable @Positive Long portionId,
            @Valid @RequestBody PlanFoodPortionUpdateQuantityRequestDTO dto) {
        log.info("Updating quantity for portionId={} in plan mealId={}", portionId, mealId);
        log.debug("Update quantity payload={}", dto);
        PlanMealResponseDTO response = service.updateQuantity(mealId, portionId, dto);
        log.info("Quantity updated successfully for portionId={} in plan mealId={}", portionId, mealId);
        return ResponseEntity.ok(response);
    }

}