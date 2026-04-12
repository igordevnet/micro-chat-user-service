package com.microservice.microchatuserservice.application.Exceptions;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
