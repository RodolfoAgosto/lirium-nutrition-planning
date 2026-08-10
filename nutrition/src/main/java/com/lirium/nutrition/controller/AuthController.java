package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.LoginRequestDTO;
import com.lirium.nutrition.dto.request.RefreshRequestDTO;
import com.lirium.nutrition.dto.response.AuthResponseDTO;
import com.lirium.nutrition.infrastructure.security.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user authentication and JWT token management.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Log in user",
            description = "Authenticates a user using their credentials (email and password) and returns a valid JWT token along with basic profile information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful. The JWT token is generated and returned.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request structure or missing mandatory fields.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials (incorrect email or password).",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Account is disabled or suspended by an administrator.",
                    content = @Content
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Refresh JWT authentication token",
            description = "Generates a new JWT access token using a valid, non-expired refresh token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token successfully refreshed. Returns new JWT access token and refresh token.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request structure or missing refresh token field.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized. The refresh token is invalid, expired, or revoked.",
                    content = @Content
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO request) {
        return ResponseEntity.ok(authService.refresh(request.refreshToken()));
    }

}
