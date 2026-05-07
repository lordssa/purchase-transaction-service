package com.wex.purchase.entrypoint.controller.error;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;

import com.wex.purchase.application.exception.DuplicateCorrelationException;
import com.wex.purchase.application.exception.InvalidCorrelationIdFormatException;
import com.wex.purchase.application.exception.MissingCorrelationIdException;
import com.wex.purchase.application.exception.PurchaseTransactionNotFoundException;
import com.wex.purchase.application.exception.CurrencyConversionUnavailableException;
import com.wex.purchase.application.exception.ExternalServiceUnavailableException;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);
    private static final String REQUEST_ID_FIELD = "X-Request-Id";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<FieldValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(RestExceptionHandler::toFieldValidationError)
                .toList();

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        List<FieldValidationError> errors = extractJacksonFieldErrors(ex);

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Request body is invalid",
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DuplicateCorrelationException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateCorrelation(
            DuplicateCorrelationException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MissingCorrelationIdException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingCorrelation(
            MissingCorrelationIdException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of(
                        new FieldValidationError(
                                REQUEST_ID_FIELD,
                                "Required header must be an UUID"
                        )
                )
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequestHeader(
            MissingRequestHeaderException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Required header is missing",
                request.getRequestURI(),
                List.of(new FieldValidationError(ex.getHeaderName(), "RequestHeader is Required"))
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingQueryParam(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Required query parameter is missing",
                request.getRequestURI(),
                List.of(new FieldValidationError(ex.getParameterName(), "Field is Required"))
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<FieldValidationError> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> new FieldValidationError(
                        v.getPropertyPath() != null ? v.getPropertyPath().toString() : "parameter",
                        v.getMessage()
                ))
                .toList();

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Request parameter is invalid",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String field = ex.getName() != null ? ex.getName() : "parameter";
        String message = "Invalid value";
        if (ex.getRequiredType() != null && java.util.UUID.class.equals(ex.getRequiredType())) {
            message = "must be a valid UUID";
        }

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Request parameter is invalid",
                request.getRequestURI(),
                List.of(new FieldValidationError(field, message))
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(InvalidCorrelationIdFormatException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCorrelationFormat(
            InvalidCorrelationIdFormatException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of(
                        new FieldValidationError(REQUEST_ID_FIELD, ex.getMessage())
                )
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(PurchaseTransactionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            PurchaseTransactionNotFoundException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CurrencyConversionUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleConversionUnavailable(
            CurrencyConversionUnavailableException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                422,
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(422).body(response);
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalServiceUnavailable(
            ExternalServiceUnavailableException ex,
            HttpServletRequest request
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception at path={}", request.getRequestURI(), ex);

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Something went wrong, please try again later or contact support",
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private static List<FieldValidationError> extractJacksonFieldErrors(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause == null) {
            return List.of(new FieldValidationError("body", "Malformed JSON"));
        }

        String simpleName = cause.getClass().getSimpleName();
        boolean isInvalidFormat = "InvalidFormatException".equals(simpleName);
        boolean isMismatchedInput = "MismatchedInputException".equals(simpleName);

        if (isInvalidFormat || isMismatchedInput) {
            String field = jsonPathFromJacksonException(cause);
            Class<?> targetType = targetTypeFromJacksonException(cause);
            String expected = expectedTypeLabel(targetType);
            return List.of(new FieldValidationError(field, "must be a " + expected));
        }

        return List.of(new FieldValidationError("body", "Malformed JSON"));
    }

    private static String jsonPathFromJacksonException(Throwable ex) {
        List<?> path = invokeListGetter(ex, "getPath");
        if (path == null || path.isEmpty()) {
            return "body";
        }
        return path
                .stream()
                .map(RestExceptionHandler::jsonPathSegment)
                .collect(Collectors.joining("."));
    }

    private static String jsonPathSegment(Object ref) {
        if (ref == null) {
            return "body";
        }
        String fieldName = invokeStringGetter(ref, "getFieldName");
        if (fieldName != null && !fieldName.isBlank()) {
            return fieldName;
        }
        Integer index = invokeIntegerGetter(ref, "getIndex");
        if (index != null && index >= 0) {
            return "[" + index + "]";
        }
        return "body";
    }

    private static Class<?> targetTypeFromJacksonException(Throwable ex) {
        Object type = invokeGetter(ex, "getTargetType");
        return type instanceof Class<?> clazz ? clazz : null;
    }

    private static String expectedTypeLabel(Class<?> type) {
        if (type == null) {
            return "valid value";
        }
        if (Number.class.isAssignableFrom(type)) {
            return "number";
        }
        if (type == java.math.BigDecimal.class) {
            return "number";
        }
        if (type == java.time.LocalDate.class) {
            return "date (yyyy-MM-dd)";
        }
        if (type == String.class) {
            return "string";
        }
        return "valid value";
    }

    private static Object invokeGetter(Object target, String methodName) {
        try {
            return target.getClass().getMethod(methodName).invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static List<?> invokeListGetter(Object target, String methodName) {
        Object value = invokeGetter(target, methodName);
        return value instanceof List<?> list ? list : null;
    }

    private static String invokeStringGetter(Object target, String methodName) {
        Object value = invokeGetter(target, methodName);
        return value instanceof String s ? s : null;
    }

    private static Integer invokeIntegerGetter(Object target, String methodName) {
        Object value = invokeGetter(target, methodName);
        return value instanceof Integer i ? i : null;
    }

    private static FieldValidationError toFieldValidationError(FieldError error) {
        String message = error.getDefaultMessage();
        if (message == null || message.isBlank()) {
            message = "Invalid value";
        }
        return new FieldValidationError(error.getField(), message);
    }
}

