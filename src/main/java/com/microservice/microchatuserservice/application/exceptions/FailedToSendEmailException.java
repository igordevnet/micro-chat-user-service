package com.microservice.microchatuserservice.application.exceptions;

public class FailedToSendEmailException extends BusinessException {
    public FailedToSendEmailException(String message) {
        super(message);
    }
}
