package com.microservice.microchatuserservice.application.exceptions;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException() {
        super("Invalid token");
    }
}
