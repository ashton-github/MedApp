package com.medapp.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.dto.PatientResponse;
import com.medapp.backend.mapper.PatientMapper;
import com.medapp.backend.model.Patient;
import com.medapp.backend.security.UtilisateurAuthentifie;
import com.medapp.backend.service.PatientService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final PatientMapper patientMapper;

    public PatientController(PatientService patientService , PatientMapper patientMapper){
        this.patientService = patientService;
        this.patientMapper = patientMapper;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> creePatient(@Valid @RequestBody PatientRequest request , 
        @AuthenticationPrincipal UtilisateurAuthentifie utilisateur) {
        Patient patient = patientMapper.versEntite(request);

        Patient patientCree = patientService.creerPatient(patient);

        Patient patientMasque = patientService.appliquerMasquageSelonRole(patientCree,utilisateur.role());

        return ResponseEntity.status(HttpStatus.CREATED).body(patientMapper.versResponse(patientMasque));

    }
    
    

    

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> obtenirPatient(@PathVariable String id , 
        @AuthenticationPrincipal UtilisateurAuthentifie utilisateur) {
        Patient patient = patientService.obtenirPatient(id);
        
        Patient patientMasque = patientService.appliquerMasquageSelonRole(patient, utilisateur.role());

        return ResponseEntity.ok(patientMapper.versResponse(patientMasque));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> modifierPatient(@PathVariable String id,
         @Valid @RequestBody PatientRequest request , 
         @AuthenticationPrincipal UtilisateurAuthentifie utilisateur) {
        
        Patient patientModifier =  patientMapper.versEntite(request);

        Patient patientMisAJour = patientService.modifierPatient(id, patientModifier);

        Patient patientMasque = patientService.appliquerMasquageSelonRole(patientMisAJour, utilisateur.role());

        return ResponseEntity.ok(patientMapper.versResponse(patientMasque));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETAIRE')")
    public ResponseEntity<Void> supprimerPatient(@PathVariable String id) {
        patientService.supprimerPatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<PatientResponse>> listerPatients(Pageable pageable ,
         @AuthenticationPrincipal UtilisateurAuthentifie utilisateur) {
        Page<Patient> patients = patientService.listerPatients(pageable);

        Page<PatientResponse> responses = patients.map(patient -> {
            Patient patientMasque = patientService.appliquerMasquageSelonRole(patient, utilisateur.role());
            return patientMapper.versResponse(patientMasque);
        });
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientResponse>> rechercherPatients(@RequestParam String query , 
        @AuthenticationPrincipal UtilisateurAuthentifie utilisateur){
        List<Patient> patients = patientService.rechercherPatients(query);

        List<PatientResponse> responses = patients.stream()
                .map(patient -> patientMapper.versResponse(patientService.appliquerMasquageSelonRole(patient, utilisateur.role())))
                .toList();
                    
        return ResponseEntity.ok(responses);
    }
       
}
