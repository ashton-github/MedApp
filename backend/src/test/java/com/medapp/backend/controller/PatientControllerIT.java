package com.medapp.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medapp.backend.dto.LoginRequest;
import com.medapp.backend.dto.LoginResponse;
import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.dto.RegisterRequest;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.Sexe;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class PatientControllerIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String obtenirAccessToken(String email , Role role)throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(email, "MotDePasse123!", "Dupont", "Jean", role);

         mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)));

        LoginRequest loginRequest = new LoginRequest(email, "MotDePasse123!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(loginRequest)))
                        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("accessToken").asText();

    }

    @Test
    void creerPatien_retourne201_siDonneesValides()throws Exception {
        String token = obtenirAccessToken("medecin-patient@medapp.com", Role.MEDECIN);

        PatientRequest patientRequest = new PatientRequest(
            "Dupont", "Marie",  LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512123499",
                List.of("Diabète type 2"), null
        );

        mockMvc.perform(post("/api/patients")
                        .header("Authorization" , "Bearer " + token)
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

        PatientRequest premierPatient = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", numeroPartage,
                List.of(), null
        );

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(premierPatient)))
                .andExpect(status().isCreated());

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
        // given
        String token = obtenirAccessToken("medecin-invalide@medapp.com", Role.MEDECIN);

        PatientRequest requeteInvalide = new PatientRequest(
                "", "Marie", LocalDate.now().plusDays(1), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512100002",
                List.of(), null
        );

        // when/then
        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requeteInvalide)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenirPatient_retourne200_siPatientExiste() throws Exception {
        String token = obtenirAccessToken("medcin-detail@medapp.com" , Role.MEDECIN);

         PatientRequest request = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512100003",
                List.of(), null
        );

        MvcResult creationResult = mockMvc.perform(post("/api/patients")
                        .header("Authorization" , "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

        String patientId = objectMapper.readTree(creationResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/patients/" + patientId)
                        .header("Authorization" , "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nom").value("Dupont"));


    }

    @Test
    void obtenirPatient_retourne404_siIdInexistant() throws Exception {
        String token = obtenirAccessToken("medcin-404@medapp.com",Role.MEDECIN);

        mockMvc.perform(get("/api/patients/id-inexistant")
                        .header("Authorization" , "Bearer " + token))
                    .andExpect(status().isNotFound());
    }

    @Test
    void modifierPatient_retourne200_siDonneesValides() throws Exception {
        String token = obtenirAccessToken("medcin-404@medapp.com",Role.MEDECIN);

        PatientRequest requeteInitiale = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512100004",
                List.of(), null
        );

        MvcResult creationResult = mockMvc.perform(post("/api/patients")
                        .header("Authorization" , "Bearer " + token )
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requeteInitiale)))
                    .andExpect(status().isCreated())
                    .andReturn();

        String patientId = objectMapper.readTree(creationResult.getResponse().getContentAsString()).get("id").asText();

        PatientRequest requeteModifiee = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "99999999", "12 rue de la Paix", "1900512100004",
                List.of(), null
        );

        mockMvc.perform(put("/api/patients/" + patientId)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requeteModifiee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telephone").value("99999999"));


    }

    @Test
    void supprimerPatient_retourne204_siRoleAdmin()throws Exception {

        String tokenAdmin = obtenirAccessToken("admin-suppr@medapp.com", Role.ADMIN);

        PatientRequest request = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512100005",
                List.of(), null
        );

        MvcResult creationResult = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String patientId = objectMapper.readTree(creationResult.getResponse().getContentAsString()).get("id").asText();


        mockMvc.perform(delete("/api/patients/" + patientId)
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

    }

    @Test
    void supprimerPatient_retourne403_siRoleNonAdmin() throws Exception {

        String tokenMedecin = obtenirAccessToken("medecin-suppr-refuse@medapp.com", Role.MEDECIN);

        PatientRequest request = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512100006",
                List.of(), null
        );

        MvcResult creationResult = mockMvc.perform(post("/api/patients")
                        .header("Authorization" , "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String patientId = objectMapper.readTree(creationResult.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(delete("/api/patients/" + patientId )
                        .header("Authorization", "Bearer " + tokenMedecin))
                .andExpect(status().isForbidden());
    }

    @Test
    void listerPatients_retourne200_avecPageDePatients() throws Exception {
        String token = obtenirAccessToken("medcin-liste@medapp.com", Role.MEDECIN);

        PatientRequest request = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512100011",
                List.of(), null
        );

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

        PatientRequest request = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512100012",
                List.of(), null
        );

        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/patients/search")
                        .param("query" , "dupo")
                        .header("Authorization" , "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Dupont"));
    }


    @Test
    void obtenirPatient_masqueSSN_pourRoleSecretaire() throws Exception {
        String tokenSecretaire = obtenirAccessToken("secretaire-consulte@medapp.com", Role.SECRETAIRE);

        PatientRequest request = new PatientRequest(
                "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512100013",
                List.of(), null
        );

        MvcResult creationResult = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + tokenSecretaire)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String patientId = objectMapper.readTree(creationResult.getResponse().getContentAsString()).get("id").asText();

        // when/then
        mockMvc.perform(get("/api/patients/" + patientId)
                        .header("Authorization", "Bearer " + tokenSecretaire))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroSecuriteSociale").value("XXXXXXXXXX013"));
    }

    @Test
    void creerPatient_retourneMessageCoherent_siDonneesInvalides() throws Exception {
        // given
        String token = obtenirAccessToken("medecin-format-erreur@medapp.com", Role.MEDECIN);

        PatientRequest requeteInvalide = new PatientRequest(
                "", "Marie", LocalDate.now().plusDays(1), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512100014",
                List.of(), null
        );

        // when/then
        mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(requeteInvalide)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
