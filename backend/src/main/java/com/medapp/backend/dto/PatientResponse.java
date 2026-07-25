package com.medapp.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.medapp.backend.model.Sexe;

public record PatientResponse(
    String id ,
    String nom,
    String prenom,
    LocalDate dateNaissance,
    Sexe sexe,
    String telephone, 
    String adresse,
    String numeroSecuriteSociale,
    List<String> antecedents,
    String medecinReferent,
    LocalDateTime dateCreation,
    LocalDateTime dateMiseAJour
) {
    
}
