package com.medapp.backend.dto;

import java.time.LocalDate;
import java.util.List;

import com.medapp.backend.model.Medicament;
import com.medapp.backend.model.StatutOrdonnance;

public record OrdonnanceResponse(
    String id ,
    String patientId ,
    String medecinId,
    LocalDate dateEmission, 
    LocalDate dateValidite,
    List<Medicament> medicaments,
    StatutOrdonnance statut,
    String remarques
    

) {
} 
