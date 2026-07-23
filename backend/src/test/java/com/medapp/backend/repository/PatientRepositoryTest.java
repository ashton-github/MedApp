package com.medapp.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.medapp.backend.model.Patient;
import com.medapp.backend.model.Sexe;

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
        Patient patient = new Patient( "Dupont" , "Marie" , LocalDate.of(1990 , 5 , 12) , Sexe.F ,
                "12345678" , "12 rue de la Paix" , "1900512123456" ,
            List.of("Diabéte type 2") , null , LocalDateTime.now() , null );


        patientRepository.save(patient);
        List<Patient> resultats = patientRepository.findByNomContainingIgnoreCase("dupo");

        assertEquals(1, resultats.size());
        assertEquals("Dupont", resultats.get(0).getNom());
    }
    
    
}
