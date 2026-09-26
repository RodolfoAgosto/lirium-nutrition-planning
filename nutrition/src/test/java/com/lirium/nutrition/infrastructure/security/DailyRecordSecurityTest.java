package com.lirium.nutrition.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.lirium.nutrition.model.entity.User;
import com.lirium.nutrition.repository.DailyRecordRepository;
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
class DailyRecordSecurityTest {

  private static final long USER_ID = 7L;

  @Mock private DailyRecordRepository dailyRecordRepository;

  @InjectMocks private DailyRecordSecurity security;

  @Test
  @DisplayName("The owner of the daily record can access its meal records")
  void shouldAllowOwnerOfMealRecord() {
    when(dailyRecordRepository.existsByMeals_IdAndPatient_User_Id(10L, USER_ID)).thenReturn(true);

    assertThat(security.isMealRecordOwner(10L, authenticated())).isTrue();
  }

  @Test
  @DisplayName("Another patient can't access the meal record")
  void shouldRejectMealRecordOfAnotherPatient() {
    when(dailyRecordRepository.existsByMeals_IdAndPatient_User_Id(10L, USER_ID)).thenReturn(false);

    assertThat(security.isMealRecordOwner(10L, authenticated())).isFalse();
  }

  @Test
  @DisplayName("Meal record access is denied without an id or an authenticated user")
  void shouldRejectMealRecordWithoutIdOrAuthentication() {
    assertThat(security.isMealRecordOwner(null, authenticated())).isFalse();
    assertThat(security.isMealRecordOwner(10L, null)).isFalse();
    assertThat(security.isMealRecordOwner(10L, notAuthenticated())).isFalse();

    verifyNoInteractions(dailyRecordRepository);
  }

  @Test
  @DisplayName("The owner can access the daily record")
  void shouldAllowOwnerOfDailyRecord() {
    when(dailyRecordRepository.existsByIdAndPatient_User_Id(20L, USER_ID)).thenReturn(true);

    assertThat(security.isDailyRecordOwner(20L, authenticated())).isTrue();
  }

  @Test
  @DisplayName("Another patient can't access the daily record")
  void shouldRejectDailyRecordOfAnotherPatient() {
    when(dailyRecordRepository.existsByIdAndPatient_User_Id(20L, USER_ID)).thenReturn(false);

    assertThat(security.isDailyRecordOwner(20L, authenticated())).isFalse();
  }

  @Test
  @DisplayName("Daily record access is denied without an id or an authenticated user")
  void shouldRejectDailyRecordWithoutIdOrAuthentication() {
    assertThat(security.isDailyRecordOwner(null, authenticated())).isFalse();
    assertThat(security.isDailyRecordOwner(20L, null)).isFalse();
    assertThat(security.isDailyRecordOwner(20L, notAuthenticated())).isFalse();

    verifyNoInteractions(dailyRecordRepository);
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
