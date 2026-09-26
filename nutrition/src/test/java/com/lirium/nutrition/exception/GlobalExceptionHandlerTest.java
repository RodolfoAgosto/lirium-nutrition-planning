package com.lirium.nutrition.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.lirium.nutrition.model.enums.ActivityLevel;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  private HttpServletRequest request() {

    HttpServletRequest request = mock(HttpServletRequest.class);

    when(request.getRequestURI()).thenReturn("/api/test");

    return request;
  }

  @Test
  void shouldHandleValidationError() {

    var target = new Object();

    var bindingResult = new BeanPropertyBindingResult(target, "target");

    bindingResult.addError(new FieldError("target", "name", "must not be blank"));

    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

    when(exception.getBindingResult()).thenReturn(bindingResult);

    ResponseEntity<ApiError> response = handler.handleValidation(exception, request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    assertThat(response.getBody().message()).contains("name");
  }

  @Test
  void shouldHandleInvalidTag() {

    ResponseEntity<ApiError> response =
        handler.handleInvalidTag(new InvalidTagException("invalid"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldHandleFoodInUse() {

    ResponseEntity<ApiError> response =
        handler.handleFoodInUse(new FoodInUseException("used", 1L), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void shouldHandleDuplicateFood() {

    ResponseEntity<ApiError> response =
        handler.handleDuplicateFood(new DuplicateFoodException("duplicate"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void shouldHandleDuplicateTemplate() {

    ResponseEntity<ApiError> response =
        handler.handleDuplicateTemplate(new DuplicateTemplateException("duplicate"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void shouldHandleNotFound() {

    NotFoundException exception = new NotFoundException("Resource not found") {};

    ResponseEntity<ApiError> response = handler.handleNotFound(exception, request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void shouldHandleEmailAlreadyExists() {

    ResponseEntity<ApiError> response =
        handler.handleEmailExists(new EmailAlreadyExistsException("exists"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void shouldHandleUnauthorizedOperation() {

    ResponseEntity<ApiError> response =
        handler.handleUnauthorized(new UnauthorizedOperationException("no access"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldHandleEmailNotValidated() {

    ResponseEntity<ApiError> response =
        handler.handleUnauthorized(new EmailNotValidatedException("not validated"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldHandleAccountDisabled() {

    ResponseEntity<ApiError> response =
        handler.handleUnauthorized(new AccountDisabledException(1L), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldHandleInvalidGoal() {

    ResponseEntity<ApiError> response =
        handler.handleBadRequest(new InvalidGoalException("invalid"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldHandleInvalidMealStructure() {

    ResponseEntity<ApiError> response =
        handler.handleBadRequest(new InvalidMealStructureException("invalid"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldHandleGenericException() {

    ResponseEntity<ApiError> response =
        handler.handleGeneric(new RuntimeException("error"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(response.getBody().message())
        .isEqualTo("An unexpected error occurred. Please contact support.");
  }

  @Test
  void shouldHandleBadCredentials() {

    ResponseEntity<ApiError> response =
        handler.handleBadCredentials(new BadCredentialsException("bad"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldHandleAccessDenied() {

    AuthorizationResult result = mock(AuthorizationResult.class);

    AuthorizationDeniedException exception = new AuthorizationDeniedException("denied", result);

    ResponseEntity<ApiError> response = handler.handleAccessDenied(exception, request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldHandleInvalidRefreshToken() {

    ResponseEntity<ApiError> response =
        handler.handleInvalidRefreshToken(new InvalidRefreshTokenException("expired"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldHandleInvalidEnumValue() {

    ResponseEntity<ApiError> response =
        handler.handleInvalidEnum(new InvalidEnumValueException("invalid enum"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  // ------------------------------------------------ status mapping (no branches)

  @Test
  void shouldHandleDniAlreadyExists() {
    ResponseEntity<ApiError> response =
        handler.handleDNIExists(new DniAlreadyExistsException("12345678"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }

  @Test
  void shouldHandleRestrictionAlreadyExists() {
    ResponseEntity<ApiError> response =
        handler.handleRestrictionAlreadyExists(
            new RestrictionAlreadyExistsException("GLUTEN_FREE already exists"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().message()).isEqualTo("GLUTEN_FREE already exists");
  }

  @Test
  void shouldHandlePlanConflict() {
    ResponseEntity<ApiError> response =
        handler.handlePlanConflict(
            new PlanConflictException("Patient already has a draft plan"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().message()).isEqualTo("Patient already has a draft plan");
  }

  @Test
  void shouldHandleUnprocessableEntity() {
    ResponseEntity<ApiError> response =
        handler.handleUnprocessableEntity(
            new UnprocessableEntityException("Patient profile is incomplete"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @Test
  void shouldHandleUnauthorized() {
    ResponseEntity<ApiError> response =
        handler.handleUnauthorized(new UnauthorizedException("Invalid token"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldHandleDomainValidationAsBadRequest() {
    ResponseEntity<ApiError> response =
        handler.handleDomainValidationException(
            new IllegalArgumentException("Name cannot be blank"), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message()).isEqualTo("Name cannot be blank");
  }

  // ------------------------------------------------ validation messages

  @Test
  void shouldUseFallbackMessageWhenThereAreNoFieldErrors() {
    MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
    when(exception.getBindingResult())
        .thenReturn(new BeanPropertyBindingResult(new Object(), "target"));

    ResponseEntity<ApiError> response = handler.handleValidation(exception, request());

    assertThat(response.getBody().message()).isEqualTo("Validation error");
  }

  @Test
  void shouldUseFirstConstraintViolationMessage() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("must be positive");

    ResponseEntity<ApiError> response =
        handler.handleConstraintViolationException(
            new ConstraintViolationException(Set.of(violation)), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message()).isEqualTo("must be positive");
  }

  @Test
  void shouldUseFallbackMessageWhenThereAreNoConstraintViolations() {
    ResponseEntity<ApiError> response =
        handler.handleConstraintViolationException(
            new ConstraintViolationException(Set.of()), request());

    assertThat(response.getBody().message()).isEqualTo("Validation failed");
  }

  @Test
  void shouldUseFirstHandlerMethodValidationMessage() {
    MessageSourceResolvable error = mock(MessageSourceResolvable.class);
    when(error.getDefaultMessage()).thenReturn("from must be before to");
    ParameterValidationResult result = mock(ParameterValidationResult.class);
    when(result.getResolvableErrors()).thenReturn(List.of(error));
    HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
    when(exception.getAllValidationResults()).thenReturn(List.of(result));

    ResponseEntity<ApiError> response = handler.handleHandlerMethodValidation(exception, request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message()).isEqualTo("from must be before to");
  }

  @Test
  void shouldUseFallbackMessageWhenHandlerMethodValidationHasNoErrors() {
    HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
    when(exception.getAllValidationResults()).thenReturn(List.of());

    ResponseEntity<ApiError> response = handler.handleHandlerMethodValidation(exception, request());

    assertThat(response.getBody().message()).isEqualTo("Validation failed for request parameters");
  }

  // ------------------------------------------------ malformed JSON

  @Test
  void shouldNameTheFieldWithAnInvalidValue() {
    InvalidFormatException cause =
        InvalidFormatException.from(null, "bad value", "SOMETIMES", ActivityLevel.class);
    cause.prependPath(new Object(), "activityLevel");

    ResponseEntity<ApiError> response =
        handler.handleHttpMessageNotReadable(notReadable(cause), request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message())
        .isEqualTo("Invalid value 'SOMETIMES' for field 'activityLevel'");
  }

  @Test
  void shouldNameTheFieldWithAnInvalidStructure() {
    MismatchedInputException cause =
        MismatchedInputException.from(null, List.class, "expected an array");
    cause.prependPath(new Object(), "restrictionIds");

    ResponseEntity<ApiError> response =
        handler.handleHttpMessageNotReadable(notReadable(cause), request());

    assertThat(response.getBody().message())
        .isEqualTo("Invalid structure or value for field 'restrictionIds'");
  }

  @Test
  void shouldUseGenericMessageWhenTheInvalidFieldIsUnknown() {
    InvalidFormatException cause =
        InvalidFormatException.from(null, "bad value", "x", Integer.class);

    ResponseEntity<ApiError> response =
        handler.handleHttpMessageNotReadable(notReadable(cause), request());

    assertThat(response.getBody().message())
        .isEqualTo("Malformed JSON payload or invalid data format");
  }

  @Test
  void shouldUseGenericMessageWhenTheStructureFieldIsUnknown() {
    MismatchedInputException cause = MismatchedInputException.from(null, List.class, "bad");

    ResponseEntity<ApiError> response =
        handler.handleHttpMessageNotReadable(notReadable(cause), request());

    assertThat(response.getBody().message())
        .isEqualTo("Malformed JSON payload or invalid data format");
  }

  @Test
  void shouldUseGenericMessageForUnparseableJson() {
    ResponseEntity<ApiError> response =
        handler.handleHttpMessageNotReadable(notReadable(null), request());

    assertThat(response.getBody().message())
        .isEqualTo("Malformed JSON payload or invalid data format");
  }

  // ------------------------------------------------ query parameter type mismatch

  @Test
  void shouldExplainExpectedFormatForInvalidDates() {
    MethodArgumentTypeMismatchException exception = typeMismatch("from", "25/09/2026");
    doReturn(LocalDate.class).when(exception).getRequiredType();

    ResponseEntity<ApiError> response = handler.handleTypeMismatch(exception, request());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message())
        .isEqualTo(
            "Parameter 'from' has an invalid date value '25/09/2026'. Expected format: YYYY-MM-DD");
  }

  @Test
  void shouldReportInvalidValueForOtherParameterTypes() {
    MethodArgumentTypeMismatchException exception = typeMismatch("id", "abc");
    doReturn(Long.class).when(exception).getRequiredType();

    ResponseEntity<ApiError> response = handler.handleTypeMismatch(exception, request());

    assertThat(response.getBody().message()).isEqualTo("Parameter 'id' has an invalid value 'abc'");
  }

  @Test
  void shouldReportInvalidValueWhenRequiredTypeIsUnknown() {
    MethodArgumentTypeMismatchException exception = typeMismatch("id", "abc");

    ResponseEntity<ApiError> response = handler.handleTypeMismatch(exception, request());

    assertThat(response.getBody().message()).isEqualTo("Parameter 'id' has an invalid value 'abc'");
  }

  private static HttpMessageNotReadableException notReadable(Throwable cause) {
    return new HttpMessageNotReadableException("Unreadable", cause, mock(HttpInputMessage.class));
  }

  private static MethodArgumentTypeMismatchException typeMismatch(String name, Object value) {
    MethodArgumentTypeMismatchException exception = mock(MethodArgumentTypeMismatchException.class);
    when(exception.getName()).thenReturn(name);
    when(exception.getValue()).thenReturn(value);
    return exception;
  }
}
