package com.medapp.backend.service;

import com.medapp.backend.config.SecurityConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.medapp.backend.exception.DonneesInvalidesException;
import com.medapp.backend.exception.NumeroSecuriteSocialeDejaExistantException;
import com.medapp.backend.exception.PatientIntrouvableException;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.Role;
import com.medapp.backend.repository.PatientRepository;

@Service
public class PatientService {

    private final SecurityConfig securityConfig;
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository, SecurityConfig securityConfig){
        this.patientRepository = patientRepository;
        this.securityConfig = securityConfig;
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

    public Patient obtenirPatient(String id){
        return patientRepository.findById(id).orElseThrow(() -> new PatientIntrouvableException(id));
    }

    public Patient modifierPatient(String id , Patient patientModifie) {
        Patient patientExistant = patientRepository.findById(id).orElseThrow(() -> new PatientIntrouvableException(id));

        patientExistant.setNom(patientModifie.getNom());
        patientExistant.setDateNaissance(patientModifie.getDateNaissance());
        patientExistant.setSexe(patientModifie.getSexe());
        patientExistant.setTelephone(patientModifie.getTelephone());
        patientExistant.setAdresse(patientModifie.getAdresse());
        patientExistant.setNumeroSecuriteSociale(patientModifie.getNumeroSecuriteSociale());
        patientExistant.setAntecedents(patientModifie.getAntecedents());
        patientExistant.setMedecinReferent(patientModifie.getMedecinReferent());
        patientExistant.setDateMiseAJour(LocalDateTime.now());
        
        return patientRepository.save(patientExistant);
        
    }

    public void supprimerPatient(String id) {
        patientRepository.findById(id)
                .orElseThrow(() -> new PatientIntrouvableException(id));
        patientRepository.deleteById(id);
    }

    public Patient appliquerMasquageSelonRole(Patient patient, Role role){

        if(role == Role.SECRETAIRE){
            String numero = patient.getNumeroSecuriteSociale();
            if(numero != null && numero.length() >= 3){
                String masque = "X".repeat(numero.length() - 3) + numero.substring(numero.length() - 3);
                patient.setNumeroSecuriteSociale(masque);
            }
        }
        return patient;
    }
}
