package com.medapp.backend.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.medapp.backend.model.Ordonnance;
import com.medapp.backend.model.StatutOrdonnance;

@Repository
public interface OrdonnanceRepository extends MongoRepository<Ordonnance , String> {

    List<Ordonnance> findByPatientId(String patientId);
    List<Ordonnance> findByPatientIdAndStatut(String patientId, StatutOrdonnance statut);
}
