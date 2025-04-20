package com.shop.ecom.shared.error.infrastructure.primary;



import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 1000)
class BeanValidationErrorsHandler {

  private static final String ERRORS = "errors";
  private static final Logger log = LoggerFactory.getLogger(BeanValidationErrorsHandler.class);


  /**
   * Handles validation exceptions thrown when request body fields annotated with validation constraints
   * (like @NotNull, @Size, etc.) fail during method argument binding.
   *
   * @param exception the MethodArgumentNotValidException thrown by Spring
   * @return ProblemDetail containing HTTP 400 status, validation error messages, and field names
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
    ProblemDetail problem = buildProblemDetail();
    problem.setProperty(ERRORS, buildErrors(exception));

    log.info(exception.getMessage(), exception);

    return problem;
  }

  /**
   * Builds a map of field names to validation error messages from MethodArgumentNotValidException.
   *
   * @param exception the exception containing binding result with field errors
   * @return unmodifiable map of field name to error message
   */
  private Map<String, String> buildErrors(MethodArgumentNotValidException exception) {
    return exception
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .collect(Collectors.toUnmodifiableMap(FieldError::getField, FieldError::getDefaultMessage));
  }

  /**
   * Handles validation exceptions thrown when request parameters, path variables, or other method-level
   * parameters fail constraint validation (e.g., @NotBlank on @RequestParam).
   *
   * @param exception the ConstraintViolationException thrown by the validator
   * @return ProblemDetail containing HTTP 400 status and constraint violation messages
   */
  @ExceptionHandler(ConstraintViolationException.class)
  ProblemDetail handleConstraintViolationException(ConstraintViolationException exception) {
    ProblemDetail problem = buildProblemDetail();
    problem.setProperty(ERRORS, buildErrors(exception));

    log.info(exception.getMessage(), exception);

    return problem;
  }

  /**
   * Builds a standard ProblemDetail object with HTTP 400 status and a descriptive error message.
   *
   * @return ProblemDetail object to be used as a base for validation error responses
   */
  private ProblemDetail buildProblemDetail() {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
      HttpStatus.BAD_REQUEST,
      "One or more fields were invalid. See 'errors' for details."
    );

    problem.setTitle("Bean validation error");
    return problem;
  }

  /**
   * Builds a map of property field names to error messages from ConstraintViolationException.
   *
   * @param exception the exception containing constraint violations
   * @return unmodifiable map of field name to violation message
   */
  private Map<String, String> buildErrors(ConstraintViolationException exception) {
    return exception
      .getConstraintViolations()
      .stream()
      .collect(Collectors.toUnmodifiableMap(toFieldName(), ConstraintViolation::getMessage));
  }

  /**
   * Extracts the final segment of the property path to get the field name
   * from a ConstraintViolation object.
   *
   * @return function that converts a ConstraintViolation into a field name string
   */
  private Function<ConstraintViolation<?>, String> toFieldName() {
    return error -> {
      String propertyPath = error.getPropertyPath().toString();

      return propertyPath.substring(propertyPath.lastIndexOf(".") + 1);
    };
  }
}
