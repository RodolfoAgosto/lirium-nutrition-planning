package com.lirium.nutrition.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class GlobalExceptionHandlerTest {


    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();


    private HttpServletRequest request() {

        HttpServletRequest request =
                mock(HttpServletRequest.class);

        when(request.getRequestURI())
                .thenReturn("/api/test");

        return request;
    }


    @Test
    void shouldHandleValidationError() {

        var target = new Object();

        var bindingResult =
                new BeanPropertyBindingResult(target, "target");

        bindingResult.addError(
                new FieldError(
                        "target",
                        "name",
                        "must not be blank"
                )
        );

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);


        ResponseEntity<ApiError> response =
                handler.handleValidation(
                        exception,
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody().message())
                .contains("name");
    }


    @Test
    void shouldHandleInvalidTag() {

        ResponseEntity<ApiError> response =
                handler.handleInvalidTag(
                        new InvalidTagException("invalid"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void shouldHandleFoodInUse() {

        ResponseEntity<ApiError> response =
                handler.handleFoodInUse(
                        new FoodInUseException("used", 1L),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }


    @Test
    void shouldHandleDuplicateFood() {

        ResponseEntity<ApiError> response =
                handler.handleDuplicateFood(
                        new DuplicateFoodException("duplicate"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }


    @Test
    void shouldHandleDuplicateTemplate() {

        ResponseEntity<ApiError> response =
                handler.handleDuplicateTemplate(
                        new DuplicateTemplateException("duplicate"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }


    @Test
    void shouldHandleResourceNotFound() {

        ResponseEntity<ApiError> response =
                handler.handleNotFound(
                        new ResourceNotFoundException("missing", 1L),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    void shouldHandleEmailAlreadyExists() {

        ResponseEntity<ApiError> response =
                handler.handleEmailExists(
                        new EmailAlreadyExistsException("exists"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }


    @Test
    void shouldHandleUnauthorizedOperation() {

        ResponseEntity<ApiError> response =
                handler.handleUnauthorized(
                        new UnauthorizedOperationException("no access"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    void shouldHandleEmailNotValidated() {

        ResponseEntity<ApiError> response =
                handler.handleUnauthorized(
                        new EmailNotValidatedException("not validated"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    void shouldHandleAccountDisabled() {

        ResponseEntity<ApiError> response =
                handler.handleUnauthorized(
                        new AccountDisabledException(1L),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    void shouldHandleInvalidGoal() {

        ResponseEntity<ApiError> response =
                handler.handleBadRequest(
                        new InvalidGoalException("invalid"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void shouldHandleInvalidMealStructure() {

        ResponseEntity<ApiError> response =
                handler.handleBadRequest(
                        new InvalidMealStructureException("invalid"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    void shouldHandleGenericException() {

        ResponseEntity<ApiError> response =
                handler.handleGeneric(
                        new RuntimeException("error"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(response.getBody().message())
                .isEqualTo("Unexpected error occurred");
    }


    @Test
    void shouldHandleBadCredentials() {

        ResponseEntity<ApiError> response =
                handler.handleBadCredentials(
                        new BadCredentialsException("bad"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    void shouldHandleAccessDenied() {

        AuthorizationResult result =
                mock(AuthorizationResult.class);

        AuthorizationDeniedException exception =
                new AuthorizationDeniedException(
                        "denied",
                        result
                );

        ResponseEntity<ApiError> response =
                handler.handleAccessDenied(
                        exception,
                        request()
                );

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    void shouldHandleInvalidRefreshToken() {

        ResponseEntity<ApiError> response =
                handler.handleInvalidRefreshToken(
                        new InvalidRefreshTokenException("expired"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }


    @Test
    void shouldHandleInvalidEnumValue() {

        ResponseEntity<ApiError> response =
                handler.handleInvalidEnum(
                        new InvalidEnumValueException("invalid enum"),
                        request()
                );


        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

}