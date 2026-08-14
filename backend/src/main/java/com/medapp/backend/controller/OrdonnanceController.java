package com.medapp.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medapp.backend.dto.OrdonnanceRequest;
import com.medapp.backend.dto.OrdonnanceResponse;
import com.medapp.backend.mapper.OrdonnanceMapper;
import com.medapp.backend.model.Ordonnance;
import com.medapp.backend.model.StatutOrdonnance;
import com.medapp.backend.security.UtilisateurAuthentifie;
import com.medapp.backend.service.OrdonnanceService;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping("api/ordonnances")
public class OrdonnanceController {

    private final OrdonnanceService ordonnanceService;
    private final OrdonnanceMapper ordonnanceMapper;

    public OrdonnanceController(OrdonnanceService ordonnanceService , OrdonnanceMapper ordonnanceMapper){
        this.ordonnanceService = ordonnanceService;
        this.ordonnanceMapper = ordonnanceMapper;
    }


    @PostMapping
    @PreAuthorize("hasRole('MEDECIN')")
    public ResponseEntity<OrdonnanceResponse> creerOrdonnance(
        @Valid @RequestBody OrdonnanceRequest ordonnanceRequest,
        @AuthenticationPrincipal UtilisateurAuthentifie utilisateur) {

        Ordonnance ordonnance = ordonnanceMapper.versEntite(ordonnanceRequest , utilisateur.id());
        Ordonnance ordonnanceCreee = ordonnanceService.creerOrdonnance(ordonnance);

        return ResponseEntity.status(HttpStatus.CREATED).body(ordonnanceMapper.versResponse(ordonnanceCreee));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdonnanceResponse> obtenirOrdonnance( @PathVariable String id) {
        Ordonnance ordonnance = ordonnanceService.obtenirOrdonnance(id);

        return ResponseEntity.status(HttpStatus.OK).body(ordonnanceMapper.versResponse(ordonnance));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<OrdonnanceResponse>> obtenirHistorique(
        @PathVariable String patientId,
        @RequestParam (required = false) StatutOrdonnance statut) {
        
            List<Ordonnance> ordonnances = ordonnanceService.obtenirHistorique(patientId, statut);

            List<OrdonnanceResponse> response = ordonnances.stream()
                .map(ordonnanceMapper::versResponse)
                .toList();

            return ResponseEntity.ok(response);
    }
    
    
    
    
    
}
