package com.lirium.nutrition.repository;

import com.lirium.nutrition.model.entity.PatientProfile;
import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.Sex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    Optional<PatientProfile> findByUser(User user);

    List<PatientProfile> findBySex(Sex sex);

    List<PatientProfile> findByPrimaryGoal(GoalType goalType);

    void deleteByUser(User user);

}