package com.medapp.backend.service;

import com.medapp.backend.dto.RendezVousRequest;
import com.medapp.backend.dto.RendezVousResponse;
import com.medapp.backend.exception.DonneesInvalidesException;
import com.medapp.backend.mapper.RendezVousMapper;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.RendezVous;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.StatutRendezVous;
import com.medapp.backend.model.TypeRendezVous;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.PatientRepository;
import com.medapp.backend.repository.RendezVousRepository;
import com.medapp.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final RendezVousMapper rendezVousMapper;

    public RendezVousService(RendezVousRepository rendezVousRepository, PatientRepository patientRepository,
                              UserRepository userRepository, RendezVousMapper rendezVousMapper) {
        this.rendezVousRepository = rendezVousRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.rendezVousMapper = rendezVousMapper;
    }

   public RendezVousResponse creerRendezVous(RendezVousRequest request, String medecinId) {
        if (medecinId == null || medecinId.isEmpty()) {
            throw new DonneesInvalidesException("Le medecin est obligatoire pour creer un rendez-vous");
        }

        validerMedecin(medecinId);

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new DonneesInvalidesException("Patient introuvable"));

        if (request.getDate() == null || request.getHeure() == null) {
            throw new DonneesInvalidesException("La date et l'heure sont obligatoires");
        }
        
        if (request.getDate().isBefore(LocalDate.now()) || (request.getDate().isEqual(LocalDate.now()) && request.getHeure().isBefore(LocalTime.now()))) {
            throw new DonneesInvalidesException("Impossible de créer un rendez-vous dans le passé");
        }

        RendezVous rendezVous = rendezVousMapper.toEntity(request, medecinId);
        rendezVous.setStatut(StatutRendezVous.PLANIFIE);

        RendezVous saved = rendezVousRepository.save(rendezVous);
        return rendezVousMapper.toResponse(saved, patient.getPrenom() + " " + patient.getNom());
    }

    public List<RendezVousResponse> obtenirRendezVousParMedecin(String medecinId) {
        return rendezVousRepository.findByMedecinId(medecinId).stream().map(rv -> {
            String patientName = patientRepository.findById(rv.getPatientId())
                    .map(p -> p.getPrenom() + " " + p.getNom())
                    .orElse("Patient Inconnu");
            return rendezVousMapper.toResponse(rv, patientName);
        }).collect(Collectors.toList());
    }

    public List<RendezVousResponse> obtenirTousLesRendezVous() {
        return rendezVousRepository.findAll().stream().map(rv -> {
            String patientName = patientRepository.findById(rv.getPatientId())
                    .map(p -> p.getPrenom() + " " + p.getNom())
                    .orElse("Patient Inconnu");
            return rendezVousMapper.toResponse(rv, patientName);
        }).collect(Collectors.toList());
    }

    public RendezVousResponse modifierRendezVous(String id, RendezVousRequest request) {
        RendezVous existant = rendezVousRepository.findById(id)
                .orElseThrow(() -> new DonneesInvalidesException("Rendez-vous introuvable"));

        if (request.getDate() != null) existant.setDate(request.getDate());
        if (request.getHeure() != null) existant.setHeure(request.getHeure());
        if (request.getDuree() > 0) existant.setDuree(request.getDuree());
        if (request.getType() != null) existant.setType(TypeRendezVous.valueOf(request.getType()));
        if (request.getRemarques() != null) existant.setRemarques(request.getRemarques());

        RendezVous saved = rendezVousRepository.save(existant);
        String patientName = patientRepository.findById(saved.getPatientId())
                .map(p -> p.getPrenom() + " " + p.getNom())
                .orElse("Patient Inconnu");

        return rendezVousMapper.toResponse(saved, patientName);
    }

    public RendezVousResponse changerStatut(String id, String statut) {
        RendezVous existant = rendezVousRepository.findById(id)
                .orElseThrow(() -> new DonneesInvalidesException("Rendez-vous introuvable"));
        
        try {
            existant.setStatut(StatutRendezVous.valueOf(statut));
        } catch (IllegalArgumentException e) {
            throw new DonneesInvalidesException("Statut invalide");
        }

        RendezVous saved = rendezVousRepository.save(existant);
        String patientName = patientRepository.findById(saved.getPatientId())
                .map(p -> p.getPrenom() + " " + p.getNom())
                .orElse("Patient Inconnu");

        return rendezVousMapper.toResponse(saved, patientName);
    }

    public void supprimerRendezVous(String id) {
        if (!rendezVousRepository.existsById(id)) {
            throw new DonneesInvalidesException("Rendez-vous introuvable");
        }
        rendezVousRepository.deleteById(id);
    }

    private void validerMedecin(String medecinId) {
        User medecin = userRepository.findById(medecinId)
                .orElseThrow(() -> new DonneesInvalidesException("Le medecin specifie n'existe pas."));
        if (medecin.getRole() != Role.MEDECIN) {
            throw new DonneesInvalidesException("L'utilisateur specifie n'a pas le role MEDECIN.");
        }
    }
}
