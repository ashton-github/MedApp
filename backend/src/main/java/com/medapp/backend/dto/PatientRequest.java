package com.medapp.backend.dto;


import java.time.LocalDate;
import java.util.List;

import com.medapp.backend.model.Sexe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public record PatientRequest(
    @NotBlank String nom,
    @NotBlank String prenom ,
    @NotNull @Past LocalDate dateNaissance,
    @NotNull Sexe sexe,
    String telephone,
    String adresse,
    @NotBlank String numeroSecuriteSociale,
    List<String> antecedents,
    String medecinReferent
) {
    
}
