package com.medapp.backend.exception;

public class NumeroSecuriteSocialeDejaExistantException  extends RuntimeException {
    public NumeroSecuriteSocialeDejaExistantException(String numeroSecuriteSociale){
        super("Le numéro de sécurité sociale est déjà utilisé : " + numeroSecuriteSociale);
    }
    
}
