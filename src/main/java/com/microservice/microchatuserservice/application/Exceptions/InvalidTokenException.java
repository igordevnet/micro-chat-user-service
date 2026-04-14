package com.microservice.microchatuserservice.application.Exceptions;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException() {
        super("Invalid token");
    }
}
