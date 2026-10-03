package com.lirium.nutrition;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(
    packages = "com.lirium.nutrition",
    importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

  // =========================
  // Architecture Rules
  // =========================

  @ArchTest
  static final ArchRule layeredArchitecture =
      layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .layer("Controller")
          .definedBy("..controller..")
          .layer("Service")
          .definedBy("..service..")
          .layer("Repository")
          .definedBy("..repository..")
          .layer("Infrastructure")
          .definedBy("..infrastructure..")
          .whereLayer("Controller")
          .mayNotBeAccessedByAnyLayer()
          .whereLayer("Service")
          .mayOnlyBeAccessedByLayers("Controller", "Infrastructure")
          .whereLayer("Repository")
          .mayOnlyBeAccessedByLayers("Service", "Infrastructure");

  // =========================
  // Structure Rules
  // ========================
  @ArchTest
  static final ArchRule controllersShouldBeInControllerPackage =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("..controller..");

  @ArchTest
  static final ArchRule controllersShouldHaveControllerSuffix =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .haveSimpleNameEndingWith("Controller");

  // =========================
  // Dependency Rules
  // =========================
  @ArchTest
  static final ArchRule securityBeansShouldNotDependOnServiceLayer =
      noClasses()
          .that()
          .resideInAPackage("..infrastructure.security..")
          .and()
          .haveSimpleNameEndingWith("Security")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..service..");

  @ArchTest
  static final ArchRule modelShouldNotDependOnOtherLayers =
      noClasses()
          .that()
          .resideInAPackage("com.lirium.nutrition.model..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "com.lirium.nutrition.controller..",
              "com.lirium.nutrition.service..",
              "com.lirium.nutrition.repository..",
              "com.lirium.nutrition.dto..",
              "com.lirium.nutrition.mapper..",
              "com.lirium.nutrition.infrastructure..")
          .because("the domain model is the core and must not depend on outer layers");

  // =========================
  // Convention Rules
  // =========================
  @ArchTest
  static final ArchRule serviceImplementationsShouldBeInImplPackage =
      classes()
          .that()
          .resideInAnyPackage("..service..")
          .and()
          .haveSimpleNameEndingWith("ServiceImpl")
          .should()
          .resideInAnyPackage("..service.impl..");

  @ArchTest
  static final ArchRule serviceImplementationsShouldImplementService =
      classes()
          .that()
          .haveSimpleNameEndingWith("ServiceImpl")
          .should()
          .implement(JavaClass.Predicates.simpleNameEndingWith("Service"));

  // =========================
  // Type Rules
  // =========================
  @ArchTest
  static final ArchRule repositoriesShouldBeInterfaces =
      classes().that().resideInAPackage("..repository..").should().beInterfaces();
}
