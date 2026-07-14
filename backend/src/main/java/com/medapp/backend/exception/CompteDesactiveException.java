package com.medapp.backend.exception;

public class CompteDesactiveException extends RuntimeException {

    public CompteDesactiveException(){
        super("Ce compte est desactivé. Veuillez contacter l'administrateur pour plus d'informations.");
    }
    
}
