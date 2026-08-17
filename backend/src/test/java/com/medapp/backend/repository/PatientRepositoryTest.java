package com.medapp.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.medapp.backend.TestDataFactory;
import com.medapp.backend.model.Patient;

import org.testcontainers.junit.jupiter.Container;

@DataMongoTest
@Testcontainers
@ActiveProfiles("test")
public class PatientRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @Autowired
    private PatientRepository patientRepository;

    @AfterEach
    void nettoyageBase(){
        patientRepository.deleteAll();
    }

    @Test
    void recherche_retournePatient_dontLeNomContientLaRequete_insensibleALaCasse(){
        Patient patient = TestDataFactory.unPatient();


        patientRepository.save(patient);
        List<Patient> resultats = patientRepository.findByNomContainingIgnoreCase("dupo");

        assertEquals(1, resultats.size());
        assertEquals("Dupont", resultats.get(0).getNom());
    }

    @Test
    void recherche_retournePatient_dontLePrenomContientLaRequete_insensibleALaCasse() {
        
        Patient patient = TestDataFactory.unPatient();
        patientRepository.save(patient);

        List<Patient> resultats = patientRepository.findByPrenomContainingIgnoreCase("Mar");

        assertEquals(1, resultats.size());
        assertEquals("Marie", resultats.get(0).getPrenom());
    }
    
    
}
