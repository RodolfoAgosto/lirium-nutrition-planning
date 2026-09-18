package com.lirium.nutrition.infrastructure.config;

import com.lirium.nutrition.exception.ApiError;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
  @ApiResponse(
      responseCode = "401",
      description = "Unauthorized. Missing or invalid JWT token.",
      content = @Content(schema = @Schema(implementation = ApiError.class))),
  @ApiResponse(
      responseCode = "403",
      description = "Forbidden. User lacks the required role.",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
})
public @interface CommonAuthResponses {}
