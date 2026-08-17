package com.lirium.nutrition.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn("Validation error path={} message={}", request.getRequestURI(), message);

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidTagException.class)
    public ResponseEntity<ApiError> handleInvalidTag(
            InvalidTagException ex,
            HttpServletRequest request) {

        log.warn("Invalid tag path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Tag",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(FoodInUseException.class)
    public ResponseEntity<ApiError> handleFoodInUse(
            FoodInUseException ex,
            HttpServletRequest request) {

        log.warn("Food in use path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Food In Use",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error); // 409
    }

    @ExceptionHandler(DuplicateFoodException.class)
    public ResponseEntity<ApiError> handleDuplicateFood(
            DuplicateFoodException ex,
            HttpServletRequest request) {

        log.warn("Duplicate food path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Duplicate Food",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DuplicateTemplateException.class)
    public ResponseEntity<ApiError> handleDuplicateTemplate(
            DuplicateTemplateException ex,
            HttpServletRequest request) {

        log.warn("Duplicate template path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Duplicate Template",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailExists(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Email already exists path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Email already exists",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(DNIAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleDNIExists(
            DNIAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("DNI already exists path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "DNI already exists",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler({
            UnauthorizedOperationException.class,
            EmailNotValidatedException.class,
            AccountDisabledException.class
    })
    public ResponseEntity<ApiError> handleUnauthorized(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Unauthorized operation path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "Unauthorized operation",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler({
            InvalidGoalException.class,
            InvalidMealStructureException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Bad request path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error path={}", request.getRequestURI(), ex);

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "Unexpected error occurred",
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                401,
                "Unauthorized",
                "Invalid email or password",
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler({
            AccessDeniedException.class,
            AuthorizationDeniedException.class
    })
    public ResponseEntity<ApiError> handleAccessDenied(
            AuthorizationDeniedException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                403,
                "Forbidden",
                "Access denied",
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                401,
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(InvalidEnumValueException.class)
    public ResponseEntity<ApiError> handleInvalidEnum(
            InvalidEnumValueException ex,
            HttpServletRequest request
    ) {

        ApiError error = new ApiError(
                        400,
                   "Bad Request",
                        ex.getMessage(),
                        request.getRequestURI(),
                        LocalDateTime.now()
                );
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleHandlerMethodValidation(
            HandlerMethodValidationException ex,
            HttpServletRequest request) {

        String message = ex.getAllValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Validation failed for request parameters");

        log.warn("Handler method validation failed path={} message={}", request.getRequestURI(), message);

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({ IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<ApiError> handleDomainValidationException(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("Domain validation error path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Validation failed");

        log.warn("Constraint violation error path={} message={}", request.getRequestURI(), message);

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ApiError> handleUnprocessableEntity(
            UnprocessableEntityException ex,
            HttpServletRequest request) {

        log.warn("Unprocessable entity error path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Unprocessable Entity",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(PlanConflictException.class)
    public ResponseEntity<ApiError> handlePlanConflict(
            PlanConflictException ex,
            HttpServletRequest request) {

        log.warn("Plan state conflict path={} message={}", request.getRequestURI(), ex.getMessage());

        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String message = "Malformed JSON payload or invalid data format";

        if (ex.getCause() instanceof InvalidFormatException invalidFormatEx) {
            String fieldName = invalidFormatEx.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));

            if (!fieldName.isEmpty()) {
                message = String.format("Invalid value '%s' for field '%s'", invalidFormatEx.getValue(), fieldName);
            }
        } else if (ex.getCause() instanceof MismatchedInputException mismatchedInputEx) {
            String fieldName = mismatchedInputEx.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));

            if (!fieldName.isEmpty()) {
                message = String.format("Invalid structure or value for field '%s'", fieldName);
            }
        }

        log.warn("Malformed JSON payload path={} message={}", request.getRequestURI(), message);

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON",
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format("Parameter '%s' has an invalid value '%s'", ex.getName(), ex.getValue());
        log.warn("Type mismatch path={} message={}", request.getRequestURI(), message);

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Parameter",
                message,
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}