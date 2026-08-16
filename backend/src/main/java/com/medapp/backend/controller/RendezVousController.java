package com.medapp.backend.controller;

import com.medapp.backend.dto.RendezVousRequest;
import com.medapp.backend.dto.RendezVousResponse;
import com.medapp.backend.security.UtilisateurAuthentifie;
import com.medapp.backend.service.RendezVousService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/rendezvous")
public class RendezVousController {

    private final RendezVousService rendezVousService;

    public RendezVousController(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SECRETAIRE')")
    public ResponseEntity<RendezVousResponse> creerRendezVous(
            @RequestBody RendezVousRequest request,
            @AuthenticationPrincipal UtilisateurAuthentifie utilisateur) {
        String medecinId = request.getMedecinId();
        // Fallback for demo purposes if medecin is not provided
        if (medecinId == null || medecinId.isEmpty()) {
            medecinId = "default-medecin-id";
        }
        return new ResponseEntity<>(rendezVousService.creerRendezVous(request, medecinId), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDECIN', 'SECRETAIRE')")
    public ResponseEntity<List<RendezVousResponse>> listerRendezVous(
            @AuthenticationPrincipal UtilisateurAuthentifie utilisateur) {
        if (utilisateur.role() == com.medapp.backend.model.Role.SECRETAIRE) {
            return ResponseEntity.ok(rendezVousService.obtenirTousLesRendezVous());
        }
        return ResponseEntity.ok(rendezVousService.obtenirRendezVousParMedecin(utilisateur.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETAIRE')")
    public ResponseEntity<RendezVousResponse> modifierRendezVous(
            @PathVariable String id,
            @RequestBody RendezVousRequest request) {
        return ResponseEntity.ok(rendezVousService.modifierRendezVous(id, request));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('SECRETAIRE')")
    public ResponseEntity<RendezVousResponse> changerStatut(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String statut = body.get("statut");
        return ResponseEntity.ok(rendezVousService.changerStatut(id, statut));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETAIRE')")
    public ResponseEntity<Void> supprimerRendezVous(@PathVariable String id) {
        rendezVousService.supprimerRendezVous(id);
        return ResponseEntity.noContent().build();
    }
}
