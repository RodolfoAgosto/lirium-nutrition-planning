package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.CreatePatientRequestDTO;
import com.lirium.nutrition.dto.request.CreateUserRequestDTO;
import com.lirium.nutrition.dto.request.UserUpdateRequestDTO;
import com.lirium.nutrition.dto.response.UserResponseDTO;
import com.lirium.nutrition.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody CreateUserRequestDTO request) {

        log.info("Registering new user");
        if (log.isDebugEnabled()) {
            log.debug("User register payload={}", request.toString());
        }
        UserResponseDTO response = userService.registerUser(request);
        log.info("User registered successfully");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }

    @PostMapping("/patient")
    public ResponseEntity<UserResponseDTO> registerPatient(
            @Valid @RequestBody CreatePatientRequestDTO request) {

        log.info("Registering new patient user");
        if (log.isDebugEnabled()) {
            log.debug("Patient register payload={}", request.toString());
        }
        UserResponseDTO response = userService.registerPatient(request);
        log.info("Patient user registered successfully");
        return ResponseEntity.ok(response);

    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponseDTO> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateBasicInfo(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDTO request) {

        log.info("Updating user id={}", id);
        if (log.isDebugEnabled()) {
            log.debug("User update payload={}", request.toString());
        }
        UserResponseDTO response = userService.updateBasicInfo(id, request);
        log.info("User updated successfully id={}", id);
        return ResponseEntity.ok(response);

    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserResponseDTO> setEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {

        log.info("Setting enabled={} for user id={}", enabled, id);

        UserResponseDTO response = userService.setEnabled(id, enabled);

        log.info("User enabled state updated id={} enabled={}", id, enabled);
        return ResponseEntity.ok(response);

    }

    @PatchMapping("/{id}/validate-email")
    public ResponseEntity<UserResponseDTO> validateEmail(@PathVariable Long id) {

        log.info("Validating email for user id={}", id);

        UserResponseDTO response = userService.validateEmail(id);

        log.info("Email validated for user id={}", id);
        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        log.info("Deleting user id={}", id);

        userService.deleteById(id);

        log.info("User deleted successfully id={}", id);
        return ResponseEntity.noContent().build();

    }

}