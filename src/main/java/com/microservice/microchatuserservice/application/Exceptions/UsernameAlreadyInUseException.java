package com.microservice.microchatuserservice.application.Exceptions;

public class UsernameAlreadyInUseException extends BusinessException {

    public UsernameAlreadyInUseException(String message) {
        super(message);
    }
}
