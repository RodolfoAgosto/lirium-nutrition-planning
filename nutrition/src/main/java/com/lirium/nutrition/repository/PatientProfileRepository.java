package com.lirium.nutrition.repository;

import com.lirium.nutrition.dto.response.PatientSummaryDTO;
import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Sex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
             WHERE (CAST(:firstName AS string) IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', CAST(:firstName AS string), '%')))
             AND (CAST(:lastName AS string) IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', CAST(:lastName AS string), '%')))
             AND (CAST(:email AS string) IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%')))
             AND (CAST(:dni AS string) IS NULL OR u.dni LIKE CONCAT('%', CAST(:dni AS string), '%'))
           """)
    List<PatientSummaryDTO> searchPatients(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("dni") String dni
    );


    @Query("""
    SELECT p
    FROM PatientProfile p
    JOIN FETCH p.user
    WHERE p.user.id = :userId""")
    Optional<PatientProfile> findByUserIdFetchUser(@Param("userId") Long userId);

    Optional<PatientProfile> findByUserId(Long userId);

}