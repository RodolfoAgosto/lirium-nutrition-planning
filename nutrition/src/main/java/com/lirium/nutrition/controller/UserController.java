package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.CreatePatientRequestDTO;
import com.lirium.nutrition.dto.request.CreateUserRequestDTO;
import com.lirium.nutrition.dto.request.UserUpdateRequestDTO;
import com.lirium.nutrition.dto.response.UserResponseDTO;
import com.lirium.nutrition.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

        UserResponseDTO response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/patient")
    public ResponseEntity<UserResponseDTO> registerPatient(
            @Valid @RequestBody CreatePatientRequestDTO request) {

        UserResponseDTO response = userService.registerPatient(request);
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

        return ResponseEntity.ok(userService.updateBasicInfo(id, request));
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserResponseDTO> setEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {

        return ResponseEntity.ok(userService.setEnabled(id, enabled));
    }

    @PatchMapping("/{id}/validate-email")
    public ResponseEntity<UserResponseDTO> validateEmail(@PathVariable Long id) {
        return ResponseEntity.ok(userService.validateEmail(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}