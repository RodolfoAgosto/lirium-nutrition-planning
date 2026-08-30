package com.lirium.nutrition.dto.response;

import java.time.LocalDate;

public record PatientProfileHistorySummaryDTO(
    Long id, Long patientProfileId, LocalDate visitDate, Integer weight) {}
