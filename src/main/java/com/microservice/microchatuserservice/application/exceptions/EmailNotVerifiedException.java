package com.microservice.microchatuserservice.application.exceptions;

public class EmailNotVerifiedException extends BusinessException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
