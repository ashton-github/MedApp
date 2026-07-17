package com.medapp.backend.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EmailDejaUtiliseException.class)
    public ResponseEntity<Map<String , String>> handleEmailDejaUtilise(EmailDejaUtiliseException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message" , ex.getMessage()));
    }

    @ExceptionHandler(MotDePasseInvalideException.class)
    public ResponseEntity<Map<String , String>> handleMotDePasseInvalide(MotDePasseInvalideException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message" , ex.getMessage()));
    }


    @ExceptionHandler(IdentifiantsInvalidesException.class)
    public ResponseEntity<Map<String , String>> handleIdentifiantsInvalides(IdentifiantsInvalidesException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message" , ex.getMessage()));
    }

    @ExceptionHandler(CompteDesactiveException.class)
    public ResponseEntity<Map<String , String>> handleCompteDesactivve(CompteDesactiveException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message" , ex.getMessage()));
    }
    


}
