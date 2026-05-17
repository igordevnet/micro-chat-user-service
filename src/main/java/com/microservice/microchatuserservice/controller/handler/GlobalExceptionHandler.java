package com.microservice.microchatuserservice.controller.handler;

import com.microservice.microchatuserservice.application.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDate;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<StandardError> handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.NOT_FOUND.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameAlreadyInUseException.class)
    public ResponseEntity<StandardError> handleUsernameAlreadyInUseException(UsernameAlreadyInUseException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ResponseEntity<StandardError> handleEmailAlreadyInUseException(EmailAlreadyInUseException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.CONFLICT.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<StandardError> handleInvalidCredentialsException(InvalidCredentialsException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<StandardError> handleInvalidTokenException(InvalidTokenException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidVerifyCodeException.class)
    public ResponseEntity<StandardError> handleInvalidVerifyCodeException(InvalidVerifyCodeException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidCookieException.class)
    public  ResponseEntity<StandardError> handleInvalidCookieException(InvalidCookieException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public  ResponseEntity<StandardError> handleEmailNotVerifiedException(EmailNotVerifiedException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(FailedToSendEmailException.class)
    public  ResponseEntity<StandardError> handleFailedToSendEmailException(FailedToSendEmailException e, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(e.getMessage())
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidationErrors(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce("", (acc, error) -> acc + error + "; ");

        var response = StandardError.builder()
                .error("Error validating: " + errorMessage)
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }
}
