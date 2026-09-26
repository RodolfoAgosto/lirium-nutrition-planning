package com.lirium.nutrition.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.PlanFoodPortionRepository;
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
class PlanFoodPortionSecurityTest {

  private static final long USER_ID = 7L;

  @Mock private PlanFoodPortionRepository portionRepository;
  @Mock private PlanMealRepository mealRepository;

  @InjectMocks private PlanFoodPortionSecurity security;

  @Test
  @DisplayName("The patient who owns the plan can access its food portions")
  void shouldAllowOwnerOfPortion() {
    when(portionRepository.existsByIdAndUserId(30L, USER_ID)).thenReturn(true);

    assertThat(security.isPortionOwner(30L, authenticated())).isTrue();
  }

  @Test
  @DisplayName("Another patient can't access the food portion")
  void shouldRejectPortionOfAnotherPatient() {
    when(portionRepository.existsByIdAndUserId(30L, USER_ID)).thenReturn(false);

    assertThat(security.isPortionOwner(30L, authenticated())).isFalse();
  }

  @Test
  @DisplayName("Portion access is denied without an id or an authenticated user")
  void shouldRejectPortionWithoutIdOrAuthentication() {
    assertThat(security.isPortionOwner(null, authenticated())).isFalse();
    assertThat(security.isPortionOwner(30L, null)).isFalse();
    assertThat(security.isPortionOwner(30L, notAuthenticated())).isFalse();

    verifyNoInteractions(portionRepository);
  }

  @Test
  @DisplayName("The patient who owns the plan can access the meal")
  void shouldAllowOwnerOfMeal() {
    when(mealRepository.existsByIdAndUserId(10L, USER_ID)).thenReturn(true);

    assertThat(security.isMealOwner(10L, authenticated())).isTrue();
  }

  @Test
  @DisplayName("Another patient can't access the meal")
  void shouldRejectMealOfAnotherPatient() {
    when(mealRepository.existsByIdAndUserId(10L, USER_ID)).thenReturn(false);

    assertThat(security.isMealOwner(10L, authenticated())).isFalse();
  }

  @Test
  @DisplayName("Meal access is denied without an id or an authenticated user")
  void shouldRejectMealWithoutIdOrAuthentication() {
    assertThat(security.isMealOwner(null, authenticated())).isFalse();
    assertThat(security.isMealOwner(10L, null)).isFalse();
    assertThat(security.isMealOwner(10L, notAuthenticated())).isFalse();

    verifyNoInteractions(mealRepository);
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
