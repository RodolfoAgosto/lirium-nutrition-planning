package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.PatientProfileHistory;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.enums.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PatientProfileHistoryRepository extends JpaRepository<PatientProfileHistory, Long> {

    List<PatientProfileHistory> findByPatientProfileOrderByVisitDateDesc(PatientProfile patientProfile);

    List<PatientProfileHistory> findByPatientProfileAndVisitDateBetween(
            PatientProfile patientProfile,
            LocalDate start,
            LocalDate end
    );

    List<PatientProfileHistory> findByPatientProfileAndPrimaryGoal(
            PatientProfile patientProfile,
            GoalType primaryGoal
    );

}