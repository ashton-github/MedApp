package com.medapp.backend.exception;

public class EmailDejaUtiliseException extends RuntimeException{
    
    public EmailDejaUtiliseException(String email){
        super("L'email est déjà utilise : " + email);
    }
}
