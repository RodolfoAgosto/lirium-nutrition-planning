package com.lirium.nutrition.service;

import com.lirium.nutrition.dto.response.AdherenceReportDTO;

import java.time.LocalDate;

public interface AdherenceReportService {

    public AdherenceReportDTO getAdherence(Long patientId, LocalDate from, LocalDate to);

}

