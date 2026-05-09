package com.microservice.microchatuserservice.application.exceptions;

public class InvalidVerifyCodeException extends BusinessException {
    public InvalidVerifyCodeException(String message) {
        super(message);
    }
}
