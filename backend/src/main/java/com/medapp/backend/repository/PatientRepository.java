package com.medapp.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.medapp.backend.model.Patient;

public interface PatientRepository extends MongoRepository<Patient , String> {

    List<Patient> findByNomContainingIgnoreCase(String nom);    
    List<Patient> findByPrenomContainingIgnoreCase(String prenom);
    Optional<Patient> findByNumeroSecuriteSociale(String numeroSecuriteSociale);
} 
