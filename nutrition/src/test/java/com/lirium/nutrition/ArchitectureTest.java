package com.lirium.nutrition;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

public class ArchitectureTest {

  // =========================
  // Architecture Rules
  // =========================

  @ArchTest
          .consideringOnlyDependenciesInLayers()
          .whereLayer("Controller")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Service")
          .mayOnlyBeAccessedByLayers("Controller")
          .whereLayer("Repository")
          .mayOnlyBeAccessedByLayers("Service", "Infrastructure");

  // =========================
  // Structure Rules
  // ========================
  @ArchTest
  static final ArchRule controllersShouldBeInControllerPackage =
      classes()

  @ArchTest
  static final ArchRule controllersShouldHaveControllerSuffix =
      classes()

  // =========================
  // Dependency Rules
  // =========================
  @ArchTest
  static final ArchRule securityBeansShouldNotDependOnServiceLayer =
      noClasses()

  // =========================
  // Convention Rules
  // =========================
  @ArchTest
  static final ArchRule serviceImplementationsShouldBeInImplPackage =
      classes()

  @ArchTest
  static final ArchRule serviceImplementationsShouldImplementService =
      classes()

  // =========================
  // Type Rules
  // =========================
  @ArchTest
  static final ArchRule repositoriesShouldBeInterfaces =
