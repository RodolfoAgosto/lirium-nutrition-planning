package com.lirium.nutrition.controller;

import com.lirium.nutrition.infrastructure.config.DemoResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("demo")
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Demo", description = "Administrative operations for managing the demo environment")
public class DemoController {

  private final DemoResetService demoResetService;

  @Operation(
      summary = "Reset demo data",
      description =
          "Restores the demo database to its initial state by clearing the demo data "
              + "and executing the predefined demo data seed.")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Demo data successfully reset"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "User does not have ADMIN role")
  })
  @PreAuthorize("hasRole('ADMIN')")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/reset")
  public ResponseEntity<Void> resetDemoData() {

    demoResetService.resetDemoState();

    return ResponseEntity.noContent().build();
  }
}
