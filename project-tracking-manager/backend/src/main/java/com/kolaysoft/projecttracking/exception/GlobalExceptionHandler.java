package com.kolaysoft.projecttracking.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class
            );

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleProjectNotFound(
            ProjectNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "PROJECT_NOT_FOUND",
                exception.getMessage(),
                request,
                Collections.emptyMap()
        );
    }

    @ExceptionHandler(WeeklyReportNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleWeeklyReportNotFound(
            WeeklyReportNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "WEEKLY_REPORT_NOT_FOUND",
                exception.getMessage(),
                request,
                Collections.emptyMap()
        );
    }

    @ExceptionHandler(WorkItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleWorkItemNotFound(
            WorkItemNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "WORK_ITEM_NOT_FOUND",
                exception.getMessage(),
                request,
                Collections.emptyMap()
        );
    }

    @ExceptionHandler(DecisionLogNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDecisionLogNotFound(
            DecisionLogNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "DECISION_LOG_NOT_FOUND",
                exception.getMessage(),
                request,
                Collections.emptyMap()
        );
    }

    @ExceptionHandler(ActionItemNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleActionItemNotFound(
            ActionItemNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "ACTION_ITEM_NOT_FOUND",
                exception.getMessage(),
                request,
                Collections.emptyMap()
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(
            UserNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiErrorResponse errorResponse =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "USER_NOT_FOUND",
                        exception.getMessage(),
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(
            BusinessRuleException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "BUSINESS_RULE_ERROR",
                exception.getMessage(),
                request,
                Collections.emptyMap()
        );
    }

    @ExceptionHandler(
            AuthenticationCredentialsNotFoundException.class
    )
    public ResponseEntity<ApiErrorResponse> handleAuthenticationRequired(
            AuthenticationCredentialsNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                exception.getMessage(),
                request,
                Collections.emptyMap()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        String message = exception.getMessage();

        if (message == null
                || message.isBlank()
                || "Access Denied".equalsIgnoreCase(message)) {

            message =
                    "Bu işlem için yetkiniz bulunmamaktadır.";
        }

        ApiErrorResponse errorResponse =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "FORBIDDEN",
                        message,
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        fieldErrors.put(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Gönderilen alanlardan biri veya birkaçı geçersiz.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        exception.getConstraintViolations()
                .forEach(violation ->
                        fieldErrors.put(
                                violation
                                        .getPropertyPath()
                                        .toString(),
                                violation.getMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "CONSTRAINT_VIOLATION",
                "Gönderilen parametrelerden biri veya birkaçı geçersiz.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        String errorMessage =
                "Gönderilen parametre değeri geçersiz.";

        Class<?> requiredType =
                exception.getRequiredType();

        if (requiredType != null
                && requiredType.isEnum()) {

            String allowedValues =
                    Arrays.stream(
                                    requiredType.getEnumConstants()
                            )
                            .map(Object::toString)
                            .collect(
                                    Collectors.joining(", ")
                            );

            errorMessage =
                    "'" + exception.getValue()
                            + "' geçersiz bir değerdir. "
                            + "İzin verilen değerler: "
                            + allowedValues;
        }

        Map<String, String> fieldErrors =
                new LinkedHashMap<>();

        fieldErrors.put(
                exception.getName(),
                errorMessage
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "TYPE_MISMATCH",
                "Gönderilen istek parametresi geçersiz.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        log.warn(
                "Okunamayan request body. Path: {}",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST_BODY",
                "Request body okunamadı. JSON formatını ve enum değerlerini kontrol ediniz.",
                request,
                Collections.emptyMap()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        log.error(
                "Veri bütünlüğü hatası. Path: {}",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_ERROR",
                "İşlem veri bütünlüğü kuralları nedeniyle gerçekleştirilemedi.",
                request,
                Collections.emptyMap()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Beklenmeyen hata oluştu. Path: {}",
                request.getRequestURI(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Beklenmeyen bir hata oluştu.",
                request,
                Collections.emptyMap()
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String errorCode,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        ApiErrorResponse response =
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        errorCode,
                        message,
                        request.getRequestURI(),
                        fieldErrors
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}