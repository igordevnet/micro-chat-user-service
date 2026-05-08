package com.microservice.microchatuserservice.application.exceptions;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
