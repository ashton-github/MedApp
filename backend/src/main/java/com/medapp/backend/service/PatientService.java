package com.medapp.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.medapp.backend.exception.DonneesInvalidesException;
import com.medapp.backend.exception.NumeroSecuriteSocialeDejaExistantException;
import com.medapp.backend.model.Patient;
import com.medapp.backend.repository.PatientRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository){
        this.patientRepository = patientRepository;
    }
    
    public Patient creerPatient(Patient patient){

        if(patient.getDateNaissance() != null && patient.getDateNaissance().isAfter(LocalDate.now())){
            throw new DonneesInvalidesException("La date de naissance ne peut pas etre dans le futur.");
        }
        if(patientRepository.findByNumeroSecuriteSociale(patient.getNumeroSecuriteSociale()).isPresent()){
            throw new NumeroSecuriteSocialeDejaExistantException(patient.getNumeroSecuriteSociale());
        }
        patient.setDateCreation(LocalDateTime.now());
        return patientRepository.save(patient);
    }

    public List<Patient> rechercherPatients(String requete) {
        List<Patient> parNom = patientRepository.findByNomContainingIgnoreCase(requete);
        List<Patient> parPrenom = patientRepository.findByPrenomContainingIgnoreCase(requete);

        Map<String , Patient> resultats = new LinkedHashMap<>();
        for(Patient patient:parNom){
            resultats.put(patient.getId() , patient);
        }
        for(Patient patient:parPrenom){
            resultats.put(patient.getId() , patient);
        }
        return new ArrayList<>(resultats.values());
    }
}
