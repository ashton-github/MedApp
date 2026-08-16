package com.medapp.backend.mapper;

import com.medapp.backend.dto.RendezVousRequest;
import com.medapp.backend.dto.RendezVousResponse;
import com.medapp.backend.model.RendezVous;
import com.medapp.backend.model.TypeRendezVous;
import org.springframework.stereotype.Component;

@Component
public class RendezVousMapper {

    public RendezVous toEntity(RendezVousRequest request, String medecinId) {
        RendezVous rendezVous = new RendezVous();
        rendezVous.setPatientId(request.getPatientId());
        rendezVous.setMedecinId(medecinId);
        rendezVous.setDate(request.getDate());
        rendezVous.setHeure(request.getHeure());
        rendezVous.setDuree(request.getDuree());
        if (request.getType() != null) {
            rendezVous.setType(TypeRendezVous.valueOf(request.getType()));
        }
        rendezVous.setRemarques(request.getRemarques());
        return rendezVous;
    }

    public RendezVousResponse toResponse(RendezVous rendezVous, String patientName) {
        RendezVousResponse response = new RendezVousResponse();
        response.setId(rendezVous.getId());
        response.setPatientId(rendezVous.getPatientId());
        response.setPatientName(patientName);
        response.setMedecinId(rendezVous.getMedecinId());
        response.setDate(rendezVous.getDate());
        response.setHeure(rendezVous.getHeure());
        response.setDuree(rendezVous.getDuree());
        if (rendezVous.getType() != null) {
            response.setType(rendezVous.getType().name());
        }
        if (rendezVous.getStatut() != null) {
            response.setStatut(rendezVous.getStatut().name());
        }
        response.setRemarques(rendezVous.getRemarques());
        return response;
    }
}
