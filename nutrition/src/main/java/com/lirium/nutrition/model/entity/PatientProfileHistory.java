package com.lirium.nutrition.model.entity;

import com.lirium.nutrition.model.enums.GoalType;
import com.lirium.nutrition.model.enums.PhysiologicalCondition;
import com.lirium.nutrition.model.valueobject.Height;
import com.lirium.nutrition.model.valueobject.Weight;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Historical snapshot of a patient's profile at a given visit date. Stores weight, height, medical
 * notes, dietary restrictions, physiological conditions, and primary nutrition goal at that moment.
 * Linked to a specific PatientProfile and acts as an audit/history record.
 */
@Entity
@Table(name = "patient_profile_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class PatientProfileHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "patient_profile_id", nullable = false)
  private PatientProfile patientProfile;

  @Column(name = "visit_date", nullable = false)
  private LocalDate visitDate;

  @Embedded private Weight weight;

  @Embedded private Height height;

  @Column(name = "medical_notes", columnDefinition = "TEXT")
  private String medicalNotes;

  @ManyToMany
  @JoinTable(
      name = "patient_profile_history_restrictions",
      joinColumns = @JoinColumn(name = "history_id"),
      inverseJoinColumns = @JoinColumn(name = "restriction_id"))
  private Set<Restriction> restrictions = new HashSet<>();

  @ElementCollection(targetClass = PhysiologicalCondition.class)
  @Enumerated(EnumType.STRING)
  @CollectionTable(
      name = "patient_profile_history_conditions",
      joinColumns = @JoinColumn(name = "patient_profile_history_id"))
  @Column(name = "physiological_condition")
  private Set<PhysiologicalCondition> physiologicalConditions = new HashSet<>();

  @Enumerated(EnumType.STRING)
  @Column(name = "primary_goal")
  private GoalType primaryGoal;

  public PatientProfileHistory(PatientProfile patientProfile) {
    this.patientProfile = Objects.requireNonNull(patientProfile);
    this.visitDate = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires"));
    this.weight = patientProfile.getWeight();
    this.height = patientProfile.getHeight();
    this.medicalNotes = patientProfile.getMedicalNotes();
    this.primaryGoal = patientProfile.getPrimaryGoal();
    this.restrictions = new HashSet<>(patientProfile.getRestrictions());
    this.physiologicalConditions = new HashSet<>(patientProfile.getPhysiologicalConditions());
  }
}
