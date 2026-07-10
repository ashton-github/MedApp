package com.medapp.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de fumée : vérifie que le contexte Spring démarre correctement
 * (connexion MongoDB, sécurité, contrôleurs, etc.).
 *
 * C'est le premier test à faire passer avant d'écrire la moindre
 * fonctionnalité métier — bon exercice pour valider l'environnement Docker.
 */
@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Si cette méthode ne lève pas d'exception, le contexte Spring
        // (et la connexion à MongoDB) est correctement configuré.
    }
}
