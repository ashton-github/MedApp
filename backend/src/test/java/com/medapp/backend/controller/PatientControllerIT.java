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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
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
    
}
