package com.example.api.config;

import com.example.api.api.ErrorResponse;
import com.example.core.domain.DomainException;
import com.example.core.domain.GlobalErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        return ResponseEntity
                .status(ex.status())
                .body(new ErrorResponse(ex.errorCode().code(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";
        return ResponseEntity
                .badRequest()
                .body(new ErrorResponse(GlobalErrorCode.INVALID_INPUT.code(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return ResponseEntity
                .status(GlobalErrorCode.INTERNAL_ERROR.status())
                .body(new ErrorResponse(GlobalErrorCode.INTERNAL_ERROR.code(), GlobalErrorCode.INTERNAL_ERROR.message()));
    }
}
