package com.medapp.backend.controller;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint de vérification utilisé par le frontend au démarrage
 * pour confirmer que la stack (API + MongoDB) est opérationnelle.
 *
 * Premier point d'entrée suggéré pour s'exercer au TDD :
 * écrire le test de ce controller AVANT toute modification.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private final MongoTemplate mongoTemplate;

    public HealthController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "medapp-backend");
        body.put("status", "UP");
        body.put("mongodb", checkMongo());
        return body;
    }

    private String checkMongo() {
        try {
            mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            return "UP";
        } catch (Exception e) {
            return "DOWN";
        }
    }
}
