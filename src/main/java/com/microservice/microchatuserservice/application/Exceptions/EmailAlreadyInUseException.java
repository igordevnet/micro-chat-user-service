package com.microservice.microchatuserservice.application.Exceptions;

public class EmailAlreadyInUseException extends BusinessException{

    public EmailAlreadyInUseException(String message){
        super(message);
    }
}
