package com.medapp.backend.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class GlobalExceptionHandlerTest {

    @Test
    void handleExceptionInattendue_retourne500_avecMessageGenerique() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        Exception exceptionSimulee = new RuntimeException("Détail interne sensible qui ne doit pas fuiter");

        ResponseEntity<Map<String, String>> reponse = handler.handleExceptionInattendue(exceptionSimulee);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, reponse.getStatusCode());
        assertEquals("Une erreur inattendue est survenue.", reponse.getBody().get("message"));
    }
}