package com.lirium.nutrition.repository;

import com.lirium.nutrition.dto.response.PatientSummaryDTO;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Sex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    Optional<PatientProfile> findByUser(User user);

    List<PatientProfile> findBySex(Sex sex);

    List<PatientProfile> findByPrimaryGoal(GoalType goalType);

    void deleteByUser(User user);

    @Query("""
             SELECT new com.lirium.nutrition.dto.response.PatientSummaryDTO(
               p.id,
               u.firstName,
               u.lastName,
               u.email,
               u.dni
             )
             FROM PatientProfile p
             JOIN p.user u
             WHERE (:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')))
             AND (:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))
             AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
             AND (:dni IS NULL OR u.dni LIKE CONCAT('%', :dni, '%'))
           """)
    List<PatientSummaryDTO> searchPatients(
            String firstName,
            String lastName,
            String email,
            String dni
    );


    @Query("""
        SELECT p
        FROM PatientProfile p
        JOIN FETCH p.user
        WHERE p.id = :id
    """)
    Optional<PatientProfile> findByIdWithUser(Long id);

    Optional<PatientProfile> findByUserId(Long userId);

}