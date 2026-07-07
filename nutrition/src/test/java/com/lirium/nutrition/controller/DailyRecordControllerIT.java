package com.lirium.nutrition.controller;

import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.Role;
import com.lirium.nutrition.testdata.DailyRecordTestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DailyRecordControllerIT  extends AbstractIntegrationTest{

    // setup

    private String adminToken;
    private String nutritionistToken;
    private String patientToken;
    private Long patientId;
    private Long otherPatientId;
    private PatientProfile patientProfile;
    private PatientProfile otherPatientProfile;
    private UserDetails admin;

    @Autowired
    private DailyRecordTestDataFactory dailyRecordTestDataFactory;

    @BeforeEach
    void setup() {

        admin = userRepository.save(new User(
                "admin@test.com",
                passwordEncoder.encode("1234"),
                "Admin", "Test", Role.ADMIN));

        User nutritionist = userRepository.save(new User(
                "nutri@test.com",
                passwordEncoder.encode("1234"),
                "Nutri", "Test", Role.NUTRITIONIST));

        User patient = userRepository.save(new User(
                "patient@test.com",
                passwordEncoder.encode("1234"),
                "Patient", "Test", Role.PATIENT));

        User otherPatient = userRepository.save(new User(
                "other@test.com",
                passwordEncoder.encode("1234"),
                "Other", "Test", Role.PATIENT));

        patientId = patient.getId();
        otherPatientId = otherPatient.getId();

        patientProfile = patient.getPatientProfile();
        otherPatientProfile = otherPatient.getPatientProfile();

        adminToken = "Bearer " + jwtService.generateToken(admin);
        nutritionistToken = "Bearer " + jwtService.generateToken(nutritionist);
        patientToken = "Bearer " + jwtService.generateToken(patient);

    }

    // GET /today/{patientId}
    //    shouldReturnTodayDailyRecordWhenNutritionistRequestsPatientRecord()
    //    shouldReturnTodayDailyRecordWhenPatientRequestsOwnTodayRecord()
    //    shouldReturnForbiddenWhenPatientRequestsAnotherPatientsTodayRecord()
    //    shouldReturnUnauthorizedWhenUserIsNotAuthenticated()
    //    shouldReturnNotFoundWhenPatientDoesNotExist()
    @Test
    @DisplayName("should return today's daily record when admin requests patient record")
    void shouldReturnTodayDailyRecordWhenAdminRequestsPatientRecord() throws Exception {

        dailyRecordTestDataFactory.createTodayDailyRecord(patientProfile);
        mockMvc.perform(
                        get("/api/daily-records/today/" + patientProfile.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").exists());
    }

    @Test
    @DisplayName("should return today's daily record when nutritionist requests patient record")
    void shouldReturnTodayDailyRecordWhenNutritionistRequestsPatientRecord()
            throws Exception {
        dailyRecordTestDataFactory.createTodayDailyRecord(patientProfile);
        mockMvc.perform(get("/api/daily-records/today/" + patientProfile.getId())
                                .header("Authorization", nutritionistToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return today's daily record when patient requests own record")
    void shouldReturnTodayDailyRecordWhenPatientRequestsOwnRecord() throws Exception {

        dailyRecordTestDataFactory.createTodayDailyRecord(patientProfile);

        mockMvc.perform(
                        get("/api/daily-records/today/" + patientProfile.getId())
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return forbidden when patient requests another patient's today record")
    void shouldReturnForbiddenWhenPatientRequestsAnotherPatientsTodayRecord()
            throws Exception {
        mockMvc.perform(
                        get("/api/daily-records/today/" + otherPatientId)
                                .header("Authorization", patientToken)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should return unauthorized when user is not authenticated")
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated()
            throws Exception {

        mockMvc.perform(
                        get("/api/daily-records/today/" + patientId)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should return not found when patient does not exist")
    void shouldReturnNotFoundWhenPatientDoesNotExist() throws Exception {
        mockMvc.perform(
                        get("/api/daily-records/today/99999")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

}

    // GET /{id}
//    shouldReturnDailyRecordWhenAdminRequestsById()
//
//    shouldReturnDailyRecordWhenNutritionistRequestsById()
//
//    shouldReturnDailyRecordWhenOwnerPatientRequestsById()
//
//    shouldReturnForbiddenWhenPatientRequestsAnotherPatientsRecordById()
//
//    shouldReturnUnauthorizedWhenUserIsNotAuthenticated()
//
//    shouldReturnNotFoundWhenDailyRecordDoesNotExist()

    // GET /patient/{patientId}
//    shouldReturnPatientDailyRecordsWhenAdminRequests()
//
//    shouldReturnPatientDailyRecordsWhenNutritionistRequests()
//
//    shouldReturnPatientDailyRecordsWhenOwnerPatientRequests()
//
//    shouldReturnForbiddenWhenPatientRequestsAnotherPatientsDailyRecords()
//
//    shouldReturnUnauthorizedWhenUserIsNotAuthenticated()
//
//    shouldReturnEmptyListWhenPatientHasNoDailyRecords()


    // Tests de Daily Records con Ownership
//    @Test
//    @DisplayName("PATIENT puede ver sus registros diarios")
//    void shouldReturnDailyRecordsWhenPatientRequestsOwnRecords1() throws Exception {
//        mockMvc.perform(get("/api/daily-records/patient/" + patientId)
//                        .header("Authorization", patientToken))
//                .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("PATIENT no puede ver registros de otro paciente")
//    void shouldReturnForbiddenWhenPatientRequestsAnotherPatientsRecords() throws Exception {
//        mockMvc.perform(get("/api/daily-records/patient/" + otherPatientId)
//                        .header("Authorization", patientToken))
//                .andExpect(status().isForbidden());
//    }
//
//    @Test
//    @DisplayName("NUTRITIONIST puede ver adherencia de cualquier paciente")
//    void shouldReturnAdherenceReportWhenNutritionistRequestsAnyPatient() throws Exception {
//        mockMvc.perform(get("/api/daily-records/patient/"
//                        + patientId + "/adherence")
//                        .param("from", "2026-05-01")
//                        .param("to", "2026-05-09")
//                        .header("Authorization", nutritionistToken))
//                .andExpect(status().isOk());
//    }

    // GET /today/{patientId}

    // GET /{id}

    // GET /patient/{patientId}
//}

