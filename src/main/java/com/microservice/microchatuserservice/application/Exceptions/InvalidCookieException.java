package com.microservice.microchatuserservice.application.Exceptions;

public class InvalidCookieException extends BusinessException {
    public InvalidCookieException() {
        super("Unauthorized");
    }
}
