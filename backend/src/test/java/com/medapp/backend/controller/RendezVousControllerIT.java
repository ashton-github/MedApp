package com.medapp.backend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.dto.RendezVousRequest;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.Sexe;
import com.medapp.backend.repository.PatientRepository;
import com.medapp.backend.repository.RendezVousRepository;
import com.medapp.backend.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class RendezVousControllerIT extends IntegrationTestBase {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void nettoyageBase() {
        rendezVousRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String creerPatientEtRecupererId(String tokenMedecin, String numeroSecurite) throws Exception {
        PatientRequest patientRequest = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "98778665754", "12 rue de la paix", numeroSecurite,
                List.of(), null
        );

        MvcResult result = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void creerRendezVous_retourne201_siDonneesValides() throws Exception {
        String tokenMedecin = obtenirAccessToken("medecin-rdv-creation@medapp.com", Role.SECRETAIRE);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "1234567890123");

        RendezVousRequest request = new RendezVousRequest();
        request.setPatientId(patientId);
        request.setDate(LocalDate.now().plusDays(1));
        request.setHeure(LocalTime.of(10, 0));
        request.setDuree(30);
        request.setType("CONSULTATION");
        request.setRemarques("Premiere consultation");

        mockMvc.perform(post("/api/rendezvous")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.patientId").value(patientId))
                .andExpect(jsonPath("$.statut").value("PLANIFIE"))
                .andExpect(jsonPath("$.type").value("CONSULTATION"));
    }

    @Test
    void listerRendezVous_retourne200_avecListeDesRendezVous() throws Exception {
        String tokenMedecin = obtenirAccessToken("medecin-rdv-liste@medapp.com", Role.SECRETAIRE);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "1234567890124");

        RendezVousRequest request = new RendezVousRequest();
        request.setPatientId(patientId);
        request.setDate(LocalDate.now().plusDays(1));
        request.setHeure(LocalTime.of(10, 0));
        request.setDuree(30);
        request.setType("CONSULTATION");

        mockMvc.perform(post("/api/rendezvous")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/rendezvous")
                        .header("Authorization", "Bearer " + tokenMedecin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patientId));
    }

    @Test
    void modifierRendezVous_retourne200_siDonneesValides() throws Exception {
        String tokenMedecin = obtenirAccessToken("medecin-rdv-modif@medapp.com", Role.SECRETAIRE);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "1234567890125");

        RendezVousRequest creationRequest = new RendezVousRequest();
        creationRequest.setPatientId(patientId);
        creationRequest.setDate(LocalDate.now().plusDays(1));
        creationRequest.setHeure(LocalTime.of(10, 0));
        creationRequest.setDuree(30);
        creationRequest.setType("CONSULTATION");

        MvcResult result = mockMvc.perform(post("/api/rendezvous")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String rdvId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        RendezVousRequest modificationRequest = new RendezVousRequest();
        modificationRequest.setPatientId(patientId);
        modificationRequest.setDate(LocalDate.now().plusDays(2));
        modificationRequest.setHeure(LocalTime.of(14, 30));
        modificationRequest.setDuree(45);
        modificationRequest.setType("SUIVI");

        mockMvc.perform(put("/api/rendezvous/" + rdvId)
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(modificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("SUIVI"))
                .andExpect(jsonPath("$.duree").value(45));
    }

    @Test
    void changerStatut_retourne200_siStatutValide() throws Exception {
        String tokenMedecin = obtenirAccessToken("medecin-rdv-statut@medapp.com", Role.SECRETAIRE);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "1234567890126");

        RendezVousRequest creationRequest = new RendezVousRequest();
        creationRequest.setPatientId(patientId);
        creationRequest.setDate(LocalDate.now().plusDays(1));
        creationRequest.setHeure(LocalTime.of(10, 0));
        creationRequest.setDuree(30);
        creationRequest.setType("CONSULTATION");

        MvcResult result = mockMvc.perform(post("/api/rendezvous")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String rdvId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/rendezvous/" + rdvId + "/statut")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("statut", "TERMINE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("TERMINE"));
    }

    @Test
    void supprimerRendezVous_retourne204_siRDVExiste() throws Exception {
        String tokenMedecin = obtenirAccessToken("medecin-rdv-suppr@medapp.com", Role.SECRETAIRE);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "1234567890127");

        RendezVousRequest creationRequest = new RendezVousRequest();
        creationRequest.setPatientId(patientId);
        creationRequest.setDate(LocalDate.now().plusDays(1));
        creationRequest.setHeure(LocalTime.of(10, 0));
        creationRequest.setDuree(30);
        creationRequest.setType("CONSULTATION");

        MvcResult result = mockMvc.perform(post("/api/rendezvous")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String rdvId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(delete("/api/rendezvous/" + rdvId)
                        .header("Authorization", "Bearer " + tokenMedecin))
                .andExpect(status().isNoContent());

        // Verify it was deleted by fetching the list
        mockMvc.perform(get("/api/rendezvous")
                        .header("Authorization", "Bearer " + tokenMedecin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
