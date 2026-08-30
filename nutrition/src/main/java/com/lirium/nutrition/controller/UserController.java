package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.CreatePatientRequestDTO;
import com.lirium.nutrition.dto.request.CreateUserRequestDTO;
import com.lirium.nutrition.dto.request.UserUpdateRequestDTO;
import com.lirium.nutrition.dto.response.UserResponseDTO;
import com.lirium.nutrition.exception.ApiError;
import com.lirium.nutrition.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(
    name = "Users",
    description = "Endpoints for user management, registration, and account updates.")
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @Operation(
      operationId = "registerUser",
      summary = "Register a new user",
      description =
          "Public endpoint for patient self-registration. Creates a new account in the system.")
  @SecurityRequirements
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "User successfully registered.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request payload or validation failure.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict. Email or DNI already registered.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)))
      })
  @PostMapping
  public ResponseEntity<UserResponseDTO> registerUser(
      @Valid @RequestBody CreateUserRequestDTO request) {

    log.info("Registering new user");
    log.debug("User register payload={}", request.toString());
    UserResponseDTO response = userService.registerUser(request);
    log.info("User registered successfully");
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      operationId = "registerPatient",
      summary = "Register a new patient",
      description =
          "Creates a new user account with patient profile details. Restricted to ADMIN and NUTRITIONIST roles.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Patient successfully registered.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request payload or validation constraint failure.",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Full authentication is required to access this resource.",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden. User lacks the required role (ADMIN or NUTRITIONIST).",
            content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict. A patient with the provided email already exists.",
            content = @Content)
      })
  @PostMapping("/patient")
  public ResponseEntity<UserResponseDTO> registerPatient(
      @Valid @RequestBody CreatePatientRequestDTO request) {

    log.info("Registering new patient user");
    log.debug("Patient register payload={}", request.toString());
    UserResponseDTO response = userService.registerPatient(request);
    log.info("Patient user registered successfully");
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      operationId = "getUserById",
      summary = "Find user by ID",
      description = "Retrieves user details based on their unique database ID.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication token is missing or invalid.",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with the provided ID.",
            content = @Content)
      })
  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.findById(id));
  }

  @Operation(
      operationId = "getUserByEmail",
      summary = "Find user by email",
      description = "Retrieves user details using their registered email address.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User found successfully.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Missing or malformed email parameter.",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication token is missing or invalid.",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with the provided email.",
            content = @Content)
      })
  @GetMapping("/email")
  public ResponseEntity<UserResponseDTO> findByEmail(@RequestParam String email) {
    return ResponseEntity.ok(userService.findByEmail(email));
  }

  @Operation(
      operationId = "getAllUsers",
      summary = "Retrieve all users",
      description =
          "Returns a list of all registered users. Restricted to ADMIN and NUTRITIONIST roles.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of users.",
            content =
                @Content(
                    mediaType = "application/json",
                    array =
                        @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication token is missing or invalid.",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description =
                "Forbidden. Lacks required permissions (requires ADMIN or NUTRITIONIST role).",
            content = @Content)
      })
  @GetMapping
  public ResponseEntity<List<UserResponseDTO>> findAll() {
    return ResponseEntity.ok(userService.findAll());
  }

  @Operation(
      operationId = "updateUserBasicInfo",
      summary = "Update user basic information",
      description = "Updates editable basic profile fields for a specific user.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User information updated successfully.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request payload or validation constraint failure.",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication token is missing or invalid.",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with the provided ID.",
            content = @Content)
      })
  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDTO> updateBasicInfo(
      @PathVariable Long id, @Valid @RequestBody UserUpdateRequestDTO request) {

    log.info("Updating user id={}", id);
    log.debug("User update payload={}", request.toString());
    UserResponseDTO response = userService.updateBasicInfo(id, request);
    log.info("User updated successfully id={}", id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "setUserEnabled",
      summary = "Enable or disable user account",
      description = "Administrative endpoint to change a user's active/enabled status.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User enabled state successfully updated.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication token is missing or invalid.",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden. Requires 'user.enable' authority.",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with the provided ID.",
            content = @Content)
      })
  @PatchMapping("/{id}/enabled")
  @PreAuthorize("hasAuthority('user.enable')")
  public ResponseEntity<UserResponseDTO> setEnabled(
      @PathVariable Long id, @RequestParam boolean enabled) {

    log.info("Setting enabled={} for user id={}", enabled, id);

    UserResponseDTO response = userService.setEnabled(id, enabled);

    log.info("User enabled state updated id={} enabled={}", id, enabled);
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "validateUserEmail",
      summary = "Validate user email",
      description = "Marks a user's email address as verified.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User email successfully validated.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication token is missing or invalid.",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description =
                "Forbidden. User can only validate their own email unless they have 'user.write' authority.",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with the provided ID.",
            content = @Content),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict. User email is already validated.",
            content = @Content)
      })
  @PatchMapping("/{id}/validate-email")
  @PreAuthorize("hasAnyRole('ADMIN') or @patientSecurity.isOwner(#id, authentication)")
  public ResponseEntity<UserResponseDTO> validateEmail(@PathVariable @P("id") Long id) {

    log.info("Validating email for user id={}", id);
    log.info(">>> ENTERED validateEmail, id={}", id);

    UserResponseDTO response = userService.validateEmail(id);

    log.info("Email validated for user id={}", id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      operationId = "deleteUser",
      summary = "Disable user by ID",
      description = "Deactivates a user account in the system (Soft delete).")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "User successfully disabled. No content returned."),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request. User account is already disabled.",
            content = @Content),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized. Authentication token is missing or invalid.",
            content = @Content),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden. Requires 'user.delete' authority.",
            content = @Content),
        @ApiResponse(
            responseCode = "404",
            description = "User not found with the provided ID.",
            content = @Content)
      })
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('user.delete')")
  public ResponseEntity<Void> delete(@PathVariable Long id) {

    log.info("Disabling user id={}", id);

    userService.deleteById(id);

    log.info("User disabled successfully id={}", id);
    return ResponseEntity.noContent().build();
  }
}
