package com.microservice.microchatuserservice.application.exceptions;

public class UsernameAlreadyInUseException extends BusinessException {

    public UsernameAlreadyInUseException(String message) {
        super(message);
    }
}
