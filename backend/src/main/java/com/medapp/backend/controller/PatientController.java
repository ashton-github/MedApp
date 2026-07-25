package com.medapp.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.dto.PatientResponse;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.Role;
import com.medapp.backend.service.PatientService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService){
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> creePatient(@Valid @RequestBody PatientRequest request , Authentication authentication) {
        Patient patient = new Patient(request.nom() , request.prenom() , request.dateNaissance(),
                    request.sexe() , request.telephone() , request.adresse() , request.numeroSecuriteSociale() ,
                    request.antecedents() , request.medecinReferent() , null , null );

        Patient patientCree = patientService.creerPatient(patient);

        Role role = extraireRole(authentication);
        Patient patientMasque = patientService.appliquerMasquageSelonRole(patientCree,role);

        return ResponseEntity.status(HttpStatus.CREATED).body(versResponse(patientMasque));

    }
    
    private Role extraireRole(Authentication authentication){
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> Role.valueOf(a.getAuthority().replace("ROLE_", "")))
                .orElseThrow(() -> new IllegalStateException("Aucun role trouvee pour l'utilisateur courant."));
    }

    private PatientResponse versResponse(Patient patient){
        return new PatientResponse(
                patient.getId(), patient.getNom(), patient.getPrenom(), patient.getDateNaissance(),
                patient.getSexe(), patient.getTelephone(), patient.getAdresse(),
                patient.getNumeroSecuriteSociale(), patient.getAntecedents(), patient.getMedecinReferent(),
                patient.getDateCreation(), patient.getDateMiseAJour()
        );
    }
    
}
