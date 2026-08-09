package com.medapp.backend.mapper;

import org.springframework.stereotype.Component;
import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.dto.PatientResponse;
import com.medapp.backend.model.Patient;

@Component
public class PatientMapper {

    public Patient versEntite(PatientRequest request) {
        return new Patient(
            request.nom(), request.prenom(), request.dateNaissance(), request.sexe(),
            request.telephone(), request.adresse(), request.numeroSecuriteSociale(),
            request.antecedents(), request.medecinReferent(), null, null
        );
    }

    public PatientResponse versResponse(Patient patient) {
        return new PatientResponse(
            patient.getId(), patient.getNom(), patient.getPrenom(), patient.getDateNaissance(),
            patient.getSexe(), patient.getTelephone(), patient.getAdresse(),
            patient.getNumeroSecuriteSociale(), patient.getAntecedents(), patient.getMedecinReferent(),
            patient.getDateCreation(), patient.getDateMiseAJour()
        );
    }
}