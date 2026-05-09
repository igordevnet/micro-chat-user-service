package com.microservice.microchatuserservice.application.exceptions;

public class EmailAlreadyInUseException extends BusinessException{

    public EmailAlreadyInUseException(String message){
        super(message);
    }
}
