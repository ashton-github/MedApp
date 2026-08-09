package com.medapp.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.medapp.backend.model.Medicament;
import com.medapp.backend.model.Ordonnance;
import com.medapp.backend.model.StatutOrdonnance;

import org.testcontainers.junit.jupiter.Container;

@DataMongoTest
@Testcontainers
@ActiveProfiles("test")
public class OrdonnanceRepositoryTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");


    @Autowired
    private OrdonnanceRepository ordonnanceRepository;

    @AfterEach
    void nettoyageBase(){
        ordonnanceRepository.deleteAll();
    }

    @Test
    void sauvegardeEtRetrouveParPatientId(){
        String patientId = "patient-123";

        Ordonnance ordonnance = new Ordonnance(
            patientId , "medecin-123" , LocalDate.now() , LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane" , "100mg" , "3x/jour" , "5 jours")) ,
            StatutOrdonnance.ACTIVE , null
        );

        ordonnanceRepository.save(ordonnance);

        List<Ordonnance> result = ordonnanceRepository.findByPatientId(patientId);

        assertEquals(1, result.size());

        assertTrue(result.get(0).getPatientId().equals(patientId));
    }
    
}
