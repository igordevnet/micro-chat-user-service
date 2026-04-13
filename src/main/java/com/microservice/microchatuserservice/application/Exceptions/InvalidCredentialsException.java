package com.microservice.microchatuserservice.application.Exceptions;

public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
