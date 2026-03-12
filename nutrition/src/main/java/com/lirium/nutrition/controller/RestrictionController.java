package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.service.RestrictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/restrictions")
@RequiredArgsConstructor
public class RestrictionController {

    private final RestrictionService restrictionService;

    @GetMapping
    public ResponseEntity<Set<RestrictionSummaryDTO>> findAll() {
        Set<RestrictionSummaryDTO> restrictions = restrictionService.findAll();
        return ResponseEntity.ok(restrictions);
    }

    @PostMapping
    public ResponseEntity<RestrictionSummaryDTO> create(@Valid @RequestBody RestrictionCreateRequestDTO request) {

        RestrictionSummaryDTO response = restrictionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<RestrictionResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(restrictionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestrictionSummaryDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RestrictionCatalogUpdateDTO request) {

        return ResponseEntity.ok(restrictionService.update(id, request));
    }

}