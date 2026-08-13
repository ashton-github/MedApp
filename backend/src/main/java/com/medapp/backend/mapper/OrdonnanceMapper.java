package com.medapp.backend.mapper;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.medapp.backend.dto.OrdonnanceRequest;
import com.medapp.backend.dto.OrdonnanceResponse;
import com.medapp.backend.model.Ordonnance;

@Component
public class OrdonnanceMapper {
    
    public Ordonnance versEntite(OrdonnanceRequest ordonnanceRequest , String medcinId){
        return new Ordonnance(
            ordonnanceRequest.patientId() , medcinId , null ,
            ordonnanceRequest.dateValidite() , ordonnanceRequest.medicaments() ,
            null , ordonnanceRequest.remarques()
        );
    }

    public OrdonnanceResponse versResponse(Ordonnance ordonnance){
        return new OrdonnanceResponse(
            ordonnance.getId() , ordonnance.getPatientId() , ordonnance.getMedecinId(),
            ordonnance.getDateEmission() , ordonnance.getDateValidite() , ordonnance.getMedicaments() ,
            ordonnance.getStatut() , ordonnance.getRemarques()
        );
    }
    
}
