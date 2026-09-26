package com.lirium.nutrition.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.DailyPlanRepository;
import com.lirium.nutrition.repository.PlanMealRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class PlanMealSecurityTest {

  private static final long USER_ID = 7L;

  @Mock private PlanMealRepository planMealRepository;
  @Mock private DailyPlanRepository dailyPlanRepository;

  @InjectMocks private PlanMealSecurity security;

  @Test
  @DisplayName("The patient who owns the plan can access its meals")
  void shouldAllowOwnerOfMeal() {
    when(planMealRepository.existsByIdAndUserId(10L, USER_ID)).thenReturn(true);

    assertThat(security.isOwner(10L, authenticated())).isTrue();
  }

  @Test
  @DisplayName("Another patient can't access the meal")
  void shouldRejectMealOfAnotherPatient() {
    when(planMealRepository.existsByIdAndUserId(10L, USER_ID)).thenReturn(false);

    assertThat(security.isOwner(10L, authenticated())).isFalse();
  }

  @Test
  @DisplayName("Meal access is denied without an id or an authenticated user")
  void shouldRejectMealWithoutIdOrAuthentication() {
    assertThat(security.isOwner(null, authenticated())).isFalse();
    assertThat(security.isOwner(10L, null)).isFalse();
    assertThat(security.isOwner(10L, notAuthenticated())).isFalse();

    verifyNoInteractions(planMealRepository);
  }

  @Test
  @DisplayName("The patient who owns the plan can access its days")
  void shouldAllowOwnerOfPlanDay() {
    when(dailyPlanRepository.existsByIdAndNutritionPlan_PatientProfile_User_Id(20L, USER_ID))
        .thenReturn(true);

    assertThat(security.isPlanDayOwner(20L, authenticated())).isTrue();
  }

  @Test
  @DisplayName("Another patient can't access the plan day")
  void shouldRejectPlanDayOfAnotherPatient() {
    when(dailyPlanRepository.existsByIdAndNutritionPlan_PatientProfile_User_Id(20L, USER_ID))
        .thenReturn(false);

    assertThat(security.isPlanDayOwner(20L, authenticated())).isFalse();
  }

  @Test
  @DisplayName("Plan day access is denied without an id or an authenticated user")
  void shouldRejectPlanDayWithoutIdOrAuthentication() {
    assertThat(security.isPlanDayOwner(null, authenticated())).isFalse();
    assertThat(security.isPlanDayOwner(20L, null)).isFalse();
    assertThat(security.isPlanDayOwner(20L, notAuthenticated())).isFalse();

    verifyNoInteractions(dailyPlanRepository);
  }

  private static Authentication authenticated() {
    return new UsernamePasswordAuthenticationToken(user(), null, List.of());
  }

  private static Authentication notAuthenticated() {
    return new UsernamePasswordAuthenticationToken(user(), null);
  }

  private static User user() {
    User user = new User();
    user.setId(USER_ID);
    return user;
  }
}
