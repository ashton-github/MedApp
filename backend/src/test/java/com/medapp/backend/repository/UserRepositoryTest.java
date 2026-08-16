package com.medapp.backend.repository;

import com.medapp.backend.TestDataFactory;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;
import org.springframework.dao.DuplicateKeyException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
@DataMongoTest
@Testcontainers
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @Autowired
    private UserRepository userRepository;


    @AfterEach
    void nettoyageBase() {
        userRepository.deleteAll();
    }

    @Test
    void sauvegarde_puis_retrouve_utilisateur_par_email(){
        User user = TestDataFactory.unUtilisateur(Role.MEDECIN);
         userRepository.save(user);
         
         Optional<User> retrievedUser = userRepository.findByEmail(user.getEmail());
         assertTrue(retrievedUser.isPresent());
         assertEquals("Dupont", retrievedUser.get().getNom());
    }

    @Test
    void sauvegarde_lanceExcetption_siEmailDejaExistant(){
        User user1 = TestDataFactory.unUtilisateur(Role.MEDECIN);
        User user2 = TestDataFactory.unUtilisateur(Role.MEDECIN);
        user2.setEmail(user1.getEmail());

        userRepository.save(user1);

        assertThrows(DuplicateKeyException.class, () -> userRepository.save(user2));
    }
    
}
