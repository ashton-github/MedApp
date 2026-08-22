package com.medapp.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;

import com.medapp.backend.model.Patient;

public interface PatientRepository extends MongoRepository<Patient , String> {

    List<Patient> findByNomContainingIgnoreCase(String nom);    
    List<Patient> findByPrenomContainingIgnoreCase(String prenom);
    Optional<Patient> findByNumeroSecuriteSociale(String numeroSecuriteSociale);

    Optional<Patient> findByIdAndMedecinReferent(String id , String medecinReferent);
    Page<Patient> findByMedecinReferent(String medecinReferent, Pageable pageable);
    List<Patient> findByMedecinReferentAndNomContainingIgnoreCase(String medecinReferent, String nom);
    List<Patient> findByMedecinReferentAndPrenomContainingIgnoreCase(String medecinReferent, String prenom);
} 
