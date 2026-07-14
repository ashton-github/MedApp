package com.medapp.backend.exception;



//if password is too weak then throw this exception
public class MotDePasseInvalideException extends RuntimeException {
    public MotDePasseInvalideException(String message) {
        super(message);
    }
    
}
