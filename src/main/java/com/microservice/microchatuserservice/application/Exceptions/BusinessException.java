package com.microservice.microchatuserservice.application.Exceptions;

public abstract class BusinessException extends RuntimeException{

    protected BusinessException(String message){
        super(message);
    }
}
