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

        validerDateHeure(request.getDate(), request.getHeure());
        validerDuree(request.getDuree());
        TypeRendezVous type = validerType(request.getType());

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

        if (request.getDate() != null || request.getHeure() != null) {
            LocalDate nouvelleDate = request.getDate() != null ? request.getDate() : existant.getDate();
            LocalTime nouvelleHeure = request.getHeure() != null ? request.getHeure() : existant.getHeure();
            validerDateHeure(nouvelleDate, nouvelleHeure);
            existant.setDate(nouvelleDate);
            existant.setHeure(nouvelleHeure);
        }

        if (request.getDuree() != 0) {
            validerDuree(request.getDuree());
            existant.setDuree(request.getDuree());
        }

        if (request.getType() != null) {
            existant.setType(validerType(request.getType()));
        }

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

        existant.setStatut(validerStatut(statut));

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
        if (!medecin.isActif()) {
            throw new DonneesInvalidesException("Le medecin specifie n'est pas actif.");
        }
    }

    private void validerDateHeure(LocalDate date, LocalTime heure) {
        if (date == null || heure == null) {
            throw new DonneesInvalidesException("La date et l'heure sont obligatoires");
        }
        if (date.isBefore(LocalDate.now()) || (date.isEqual(LocalDate.now()) && heure.isBefore(LocalTime.now()))) {
            throw new DonneesInvalidesException("Impossible de programmer un rendez-vous dans le passe");
        }
    }

    private void validerDuree(int duree) {
        if (duree <= 0) {
            throw new DonneesInvalidesException("La duree doit etre superieure a zero");
        }
    }

    private TypeRendezVous validerType(String type) {
        if (type == null || type.isEmpty()) {
            throw new DonneesInvalidesException("Le type de rendez-vous est obligatoire");
        }
        try {
            return TypeRendezVous.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new DonneesInvalidesException("Type de rendez-vous invalide");
        }
    }

    private StatutRendezVous validerStatut(String statut) {
        if (statut == null || statut.isEmpty()) {
            throw new DonneesInvalidesException("Le statut est obligatoire");
        }
        try {
            return StatutRendezVous.valueOf(statut);
        } catch (IllegalArgumentException e) {
            throw new DonneesInvalidesException("Statut invalide");
        }
    }
}