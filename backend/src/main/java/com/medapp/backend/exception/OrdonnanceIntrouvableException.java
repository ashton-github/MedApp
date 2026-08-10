package com.medapp.backend.exception;

public class OrdonnanceIntrouvableException extends RuntimeException{

    public OrdonnanceIntrouvableException(String id){
        super("Ordonnance introuvable avec l'id : " + id);
    }
    
}
