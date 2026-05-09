package com.microservice.microchatuserservice.application.exceptions;

public class InvalidCookieException extends BusinessException {
    public InvalidCookieException() {
        super("Unauthorized");
    }
}
