package com.lirium.nutrition.controller;

import com.lirium.nutrition.dto.request.*;
import com.lirium.nutrition.dto.response.*;
import com.lirium.nutrition.service.RestrictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
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

        log.info("Creating restriction name={}", request.name());
        if (log.isDebugEnabled()) {
            log.debug("Restriction create payload={}", request.toString());
        }
        RestrictionSummaryDTO response = restrictionService.create(request);
        log.info("Restriction created successfully name={}", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/{id}")
    public ResponseEntity<RestrictionResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(restrictionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestrictionSummaryDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody RestrictionCatalogUpdateDTO request) {

        log.info("Updating restriction id={}", id);
        if (log.isDebugEnabled()) {
            log.debug("Restriction update payload={}", request.toString());
        }
        RestrictionSummaryDTO response = restrictionService.update(id, request);
        log.info("Restriction updated successfully id={}", id);
        return ResponseEntity.ok(response);

    }

}