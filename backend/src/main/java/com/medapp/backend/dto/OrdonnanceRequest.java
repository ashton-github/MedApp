package com.medapp.backend.dto;

import java.time.LocalDate;
import java.util.List;

import com.medapp.backend.model.Medicament;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record OrdonnanceRequest(
     @NotBlank String patientId,
    @NotNull LocalDate dateValidite, 
    @NotEmpty @Valid List<Medicament> medicaments,
    String remarques
) {
   
} 

//the medcinId comes from the authenticated principal not something the client should be trusted to set 