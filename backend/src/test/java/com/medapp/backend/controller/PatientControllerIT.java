package com.medapp.backend.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

import com.medapp.backend.TestDataFactory;
import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.Sexe;
import com.medapp.backend.repository.PatientRepository;
import com.medapp.backend.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

public class PatientControllerIT extends IntegrationTestBase {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void nettoyageBase() {
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String creerPatientEtRecupererId(String token, PatientRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    @Test
    void creerPatien_retourne201_siDonneesValides() throws Exception {
        String token = obtenirAccessToken("medecin-patient@medapp.com", Role.MEDECIN);

        PatientRequest patientRequest = TestDataFactory.unPatientRequest("1900512123499");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nom").value("Dupont"))
                .andExpect(jsonPath("$.numeroSecuriteSociale").value("1900512123499"));
    }

    @Test
    void creerPatient_retourne409_siNumeroSecuriteSocialeDejaUtilise() throws Exception {
        String token = obtenirAccessToken("medecin-doublon@medapp.com", Role.MEDECIN);
        String numeroPartage = "1900512100001";

        PatientRequest premierPatient = TestDataFactory.unPatientRequest(numeroPartage);

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(premierPatient)))
                .andExpect(status().isCreated());

        // deliberately a distinct person on every field except NSS - kept explicit
        // since this is the specific thing under test, not a fit for the shared factory
        PatientRequest deuxiemePatient = new PatientRequest(
                "Martin", "Paul", LocalDate.of(1985, 1, 1), Sexe.M,
                "87654321", "1 rue de Rome", numeroPartage,
                List.of(), null
        );

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(deuxiemePatient)))
                .andExpect(status().isConflict());
    }

    @Test
    void creerPatient_retourne400_siDonneesInvalides() throws Exception {
        String token = obtenirAccessToken("medecin-invalide@medapp.com", Role.MEDECIN);

        PatientRequest requeteInvalide = TestDataFactory.unPatientRequestInvalide("1900512100002");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requeteInvalide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenirPatient_retourne200_siPatientExiste() throws Exception {
        String token = obtenirAccessToken("medcin-detail@medapp.com", Role.MEDECIN);

        PatientRequest request = TestDataFactory.unPatientRequest("1900512100003");
        String patientId = creerPatientEtRecupererId(token, request);

        mockMvc.perform(get("/api/patients/" + patientId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Dupont"));
    }

    @Test
    void obtenirPatient_retourne404_siIdInexistant() throws Exception {
        String token = obtenirAccessToken("medcin-404@medapp.com", Role.MEDECIN);

        mockMvc.perform(get("/api/patients/id-inexistant")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void modifierPatient_retourne200_siDonneesValides() throws Exception {
        String token = obtenirAccessToken("medcin-404@medapp.com", Role.MEDECIN);

        PatientRequest requeteInitiale = TestDataFactory.unPatientRequest("1900512100004");
        String patientId = creerPatientEtRecupererId(token, requeteInitiale);

        PatientRequest requeteModifiee = TestDataFactory.unPatientRequestAvecTelephone("1900512100004", "99999999");

        mockMvc.perform(put("/api/patients/" + patientId)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requeteModifiee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telephone").value("99999999"));
    }

    @Test
    void supprimerPatient_retourne204_siRoleAdmin() throws Exception {
        String tokenAdmin = obtenirAccessToken("admin-suppr@medapp.com", Role.ADMIN);

        PatientRequest request = TestDataFactory.unPatientRequest("1900512100005");
        String patientId = creerPatientEtRecupererId(tokenAdmin, request);

        mockMvc.perform(delete("/api/patients/" + patientId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }

    @Test
    void supprimerPatient_retourne403_siRoleNonAdmin() throws Exception {
        String tokenMedecin = obtenirAccessToken("medecin-suppr-refuse@medapp.com", Role.MEDECIN);

        PatientRequest request = TestDataFactory.unPatientRequest("1900512100006");
        String patientId = creerPatientEtRecupererId(tokenMedecin, request);

        mockMvc.perform(delete("/api/patients/" + patientId)
                        .header("Authorization", "Bearer " + tokenMedecin))
                .andExpect(status().isForbidden());
    }

    @Test
    void listerPatients_retourne200_avecPageDePatients() throws Exception {
        String token = obtenirAccessToken("medcin-liste@medapp.com", Role.MEDECIN);

        PatientRequest request = TestDataFactory.unPatientRequest("1900512100011");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nom").exists());
    }

    @Test
    void recherchePatients_retournePatientsCorrespondants() throws Exception {
        String token = obtenirAccessToken("medecin-recherche@medapp.com", Role.MEDECIN);

        PatientRequest request = TestDataFactory.unPatientRequest("1900512100012");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients/search")
                        .param("query", "dupo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Dupont"));
    }

    @Test
    void obtenirPatient_masqueSSN_pourRoleSecretaire() throws Exception {
        String tokenSecretaire = obtenirAccessToken("secretaire-consulte@medapp.com", Role.SECRETAIRE);

        PatientRequest request = TestDataFactory.unPatientRequest("1900512100013");
        String patientId = creerPatientEtRecupererId(tokenSecretaire, request);

        mockMvc.perform(get("/api/patients/" + patientId)
                        .header("Authorization", "Bearer " + tokenSecretaire))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroSecuriteSociale").value("XXXXXXXXXX013"));
    }

    @Test
    void creerPatient_retourneMessageCoherent_siDonneesInvalides() throws Exception {
        String token = obtenirAccessToken("medecin-format-erreur@medapp.com", Role.MEDECIN);

        PatientRequest requeteInvalide = TestDataFactory.unPatientRequestInvalide("1900512100014");

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requeteInvalide)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}