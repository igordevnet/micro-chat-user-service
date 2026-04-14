package com.microservice.microchatuserservice.application.Exceptions;

public class InvalidVerifyCodeException extends BusinessException {
    public InvalidVerifyCodeException(String message) {
        super(message);
    }
}
