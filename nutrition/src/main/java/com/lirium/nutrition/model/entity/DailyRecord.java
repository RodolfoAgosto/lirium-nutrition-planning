package com.lirium.nutrition.model.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class DailyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "daily_record_seq")
    @SequenceGenerator(name = "daily_record_seq", sequenceName = "daily_record_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patient;

    @Column(nullable = false)
    private LocalDate date;

    @OneToMany(mappedBy = "dailyRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealRecord> meals = new ArrayList<>();

    private DailyRecord(PatientProfile patient, LocalDate date) {
        this.patient = Objects.requireNonNull(patient);
        this.date = Objects.requireNonNull(date);
    }

    public static DailyRecord of(PatientProfile patient, LocalDate date) {
        return new DailyRecord(patient, date);
    }

    public void addMeal(MealRecord meal) {
        Objects.requireNonNull(meal);
        meals.add(meal);
    }

    public List<MealRecord> getMeals() {
        return Collections.unmodifiableList(meals);
    }
}