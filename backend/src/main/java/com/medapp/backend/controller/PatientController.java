package com.medapp.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.dto.PatientResponse;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.Role;
import com.medapp.backend.service.PatientService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;




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

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getMethodName(@PathVariable String id , Authentication authentication) {
        Patient patient = patientService.obtenirPatient(id);
        Role role = extraireRole(authentication);
        Patient patientMasque = patientService.appliquerMasquageSelonRole(patient, role);

        return ResponseEntity.ok(versResponse(patientMasque));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> modifierPatient(@PathVariable String id, @Valid @RequestBody PatientRequest request , Authentication authentication) {
        
        Patient patientModifier =  new Patient(
                request.nom(), request.prenom(), request.dateNaissance(), request.sexe(),
                request.telephone(), request.adresse(), request.numeroSecuriteSociale(),
                request.antecedents(), request.medecinReferent(), null, null
        );

        Patient patientMisAJour = patientService.modifierPatient(id, patientModifier);

        Role role = extraireRole(authentication);
        Patient patientMasque = patientService.appliquerMasquageSelonRole(patientMisAJour, role);

        return ResponseEntity.ok(versResponse(patientMasque));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerPatient(@PathVariable String id) {
        patientService.supprimerPatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> listerPatients(Pageable pageable , Authentication authentication) {
        Page<Patient> patients = patientService.listerPatients(pageable);
        Role role = extraireRole(authentication);
        Page<PatientResponse> responses = patients.map(patient -> {
            Patient patientMasque = patientService.appliquerMasquageSelonRole(patient, role);
            return versResponse(patientMasque);
        });
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientResponse>> rechercherPatients(@RequestParam String query , Authentication authentication){
        List<Patient> patients = patientService.rechercherPatients(query);
        Role role = extraireRole(authentication);

        List<PatientResponse> responses = patients.stream()
                .map(patient -> versResponse(patientService.appliquerMasquageSelonRole(patient, role)))
                .toList();
                    
        return ResponseEntity.ok(responses);
    }
       
}
