package com.microservice.microchatuserservice.controller.handler;

import com.microservice.microchatuserservice.application.Exceptions.EmailAlreadyInUseException;
import com.microservice.microchatuserservice.application.Exceptions.UserNotFoundException;
import com.microservice.microchatuserservice.application.Exceptions.UsernameAlreadyInUseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
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
}
