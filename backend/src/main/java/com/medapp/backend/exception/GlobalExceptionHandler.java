package com.medapp.backend.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    public ResponseEntity<Map<String , String>> handleCompteDesactive(CompteDesactiveException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message" , ex.getMessage()));
    }



    @ExceptionHandler(NumeroSecuriteSocialeDejaExistantException.class)
    public ResponseEntity<Map<String , String>> handleNumeroSecuriteSocialeDejaExistant(NumeroSecuriteSocialeDejaExistantException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message" , ex.getMessage()));
    }

    @ExceptionHandler(DonneesInvalidesException.class)
    public ResponseEntity<Map<String , String >> handleDonneesInvalides(DonneesInvalidesException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message" , ex.getMessage()));
    }

    @ExceptionHandler(PatientIntrouvableException.class)
    public ResponseEntity<Map<String , String>> handlePatientIntrouvalbe(PatientIntrouvableException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message" , ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " : " + error.getDefaultMessage())
                .orElse("Données invalides.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Accès refusé : rôle insuffisant."));
    }

    @ExceptionHandler(OrdonnanceIntrouvableException.class)
    public ResponseEntity<Map<String , String >> handleOrdonnanceIntrouvable(OrdonnanceIntrouvableException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message" , ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleExceptionInattendue(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Une erreur inattendue est survenue."));
    }

    

    


}
