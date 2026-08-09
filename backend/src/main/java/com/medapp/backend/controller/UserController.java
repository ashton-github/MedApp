package com.medapp.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medapp.backend.dto.UserResponse;
import com.medapp.backend.model.Role;
import com.medapp.backend.service.UserService;



/**
 * Endpoint de consultation des utilisateurs, ajoute pour resoudre un probleme
 * concret rencontre lors de la Sprint 2 : le champ "medecinReferent" du modele
 * Patient (fiche technique, ref. users) ne peut pas etre fiable si le frontend
 * ne dispose d'aucun moyen de recuperer la liste des medecins existants.
 *
 * Sans cet endpoint, le champ etait rempli en texte libre (ex. "Dr. Martin"),
 * sans garantie que la valeur corresponde a un utilisateur reel, et sans
 * possibilite pour PatientService de valider la reference (cf.
 * PatientService.validerMedecinReferent). Cet endpoint permet au frontend de
 * proposer un select/autocomplete base sur des donnees reelles plutot que du
 * texte libre.
 *
 * Perimetre volontairement restreint a role=MEDECIN pour l'instant : c'est
 * le seul cas d'usage actuel (assignation d'un medecin referent a un patient).
 * Pas explicitement demande par la fiche technique/fiche de stage — decision
 * prise pour rendre le champ medecinReferent effectivement utilisable et
 * validable.
 */



@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listerUtilisateurs(@RequestParam Role role) {
        if (role == Role.MEDECIN) {
            return ResponseEntity.ok(userService.listerMedecinsActifs());
        }
        return ResponseEntity.ok(List.of());
    }
}