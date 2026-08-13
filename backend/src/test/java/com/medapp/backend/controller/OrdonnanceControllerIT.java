package com.medapp.backend.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medapp.backend.dto.LoginRequest;
import com.medapp.backend.dto.OrdonnanceRequest;
import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.dto.RegisterRequest;
import com.medapp.backend.model.Medicament;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.Sexe;
import com.medapp.backend.repository.PatientRepository;

import org.testcontainers.junit.jupiter.Container;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class OrdonnanceControllerIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


   
    private String obtenirAccessToken(String email, Role role) throws Exception {
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

    private String creerPatientEtRecupererId(String tokenMedcin , String numeroSecurite) throws JsonProcessingException, Exception{

        PatientRequest patientRequest = new PatientRequest(
            "Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
             "98778665754", "12 rue de la paix", numeroSecurite, 
             List.of(), null);

             MvcResult result = mockMvc.perform(post("/api/patients")
                                .header("Authorization", "Bearer " + tokenMedcin)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(patientRequest)))
                            .andExpect(status().isCreated())
                            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }


    @Test
    void creerOrdonnance_retourne201_siDonneeValides()throws Exception{
        String tokenMedcin = obtenirAccessToken("medecin-ordo-creation@medapp.com", Role.MEDECIN);

        String patientId = creerPatientEtRecupererId(tokenMedcin, "8776876786");

        OrdonnanceRequest request = new OrdonnanceRequest(
            patientId , LocalDate.now().plusMonths(1),
            List.of(new Medicament("Duliprane" , "1000mg" , "3x/jour" , "5 jours")) , 
            null
        );

        mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization","Bearer " + tokenMedcin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.patientId").value(patientId))
                    .andExpect(jsonPath("$.statut").value("ACTIVE"))
                    .andExpect(jsonPath("$.dateEmission").value(LocalDate.now().toString()));
    }

    
}
