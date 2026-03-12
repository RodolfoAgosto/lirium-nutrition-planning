package com.lirium.nutrition.dto.response;

public record PatientSummaryDTO(

        Long patientId,
        String firstName,
        String lastName,
        String email,
        String dni

) {}