package com.medapp.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;

import com.medapp.backend.exception.DonneesInvalidesException;
import com.medapp.backend.exception.NumeroSecuriteSocialeDejaExistantException;
import com.medapp.backend.exception.PatientIntrouvableException;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.PatientRepository;
import com.medapp.backend.repository.UserRepository;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository , UserRepository userRepository){
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }
    
    public Patient creerPatient(Patient patient , String medecinId , Role role){

        if(patient.getDateNaissance() != null && patient.getDateNaissance().isAfter(LocalDate.now())){
            throw new DonneesInvalidesException("La date de naissance ne peut pas etre dans le futur.");
        }
        if(patientRepository.findByNumeroSecuriteSociale(patient.getNumeroSecuriteSociale()).isPresent()){
            throw new NumeroSecuriteSocialeDejaExistantException(patient.getNumeroSecuriteSociale());
        }
        if(role == Role.MEDECIN){
            patient.setMedecinReferent((medecinId));
        }else {
            validerMedecinReferent(patient.getMedecinReferent());
        }
        patient.setDateCreation(LocalDateTime.now());
        return patientRepository.save(patient);
    }

    public List<Patient> rechercherPatients(String requete, String medecinId, Role role) {
        List<Patient> parNom;
        List<Patient> parPrenom;

        if (role == Role.MEDECIN) {
            parNom = patientRepository.findByMedecinReferentAndNomContainingIgnoreCase(medecinId, requete);
            parPrenom = patientRepository.findByMedecinReferentAndPrenomContainingIgnoreCase(medecinId, requete);
        } else {
            parNom = patientRepository.findByNomContainingIgnoreCase(requete);
            parPrenom = patientRepository.findByPrenomContainingIgnoreCase(requete);
        }

        Map<String, Patient> resultats = new LinkedHashMap<>();
        for (Patient patient : parNom) {
            resultats.put(patient.getId(), patient);
        }
        for (Patient patient : parPrenom) {
            resultats.put(patient.getId(), patient);
        }
        return new ArrayList<>(resultats.values());
    }

    public Patient obtenirPatient(String id , String medecinId , Role role){
        if(role == Role.MEDECIN){
            return patientRepository.findByIdAndMedecinReferent(id, medecinId)
                .orElseThrow(() -> new PatientIntrouvableException(id));
        }
        return patientRepository.findById(id).orElseThrow(() -> new PatientIntrouvableException(id));
    }

    public Patient modifierPatient(String id , Patient patientModifie , String medecinId) {
        Patient patientExistant = patientRepository.findByIdAndMedecinReferent(id, medecinId)
            .orElseThrow(() -> new PatientIntrouvableException(id));

        String nouveauNumero = patientModifie.getNumeroSecuriteSociale();
        if(nouveauNumero != null && !nouveauNumero.equals(patientExistant.getNumeroSecuriteSociale())){
            patientRepository.findByNumeroSecuriteSociale(nouveauNumero)
                .filter(autre -> !autre.getId().equals(id))
                .ifPresent(autre -> {
                    throw new NumeroSecuriteSocialeDejaExistantException(nouveauNumero);
                });
        }

        validerMedecinReferent(patientModifie.getMedecinReferent());
        patientExistant.setNom(patientModifie.getNom());
        patientExistant.setPrenom(patientModifie.getPrenom());
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

    public void supprimerPatient(String id , String medecinId) {
        patientRepository.findByIdAndMedecinReferent(id, medecinId)
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

    public Page<Patient> listerPatients(Pageable pageable , String medecinId , Role role){
        if(role == Role.MEDECIN){
            return patientRepository.findByMedecinReferent(medecinId, pageable);
        }
        return patientRepository.findAll(pageable);
    }


    private void validerMedecinReferent(String medecinId){
        if(medecinId == null){
            return;
        }
        User medecin = userRepository.findById(medecinId)
                        .orElseThrow(() -> new DonneesInvalidesException("Le medecin referent sepecifie n'existe pas."));
        if(medecin.getRole() != Role.MEDECIN){
            throw new DonneesInvalidesException("Le medecin referent doit avoir le role MEDECIN.");
        }
    }
}
