package com.medapp.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medapp.backend.dto.RegisterRequest;
import com.medapp.backend.model.Role;

import org.testcontainers.junit.jupiter.Container;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class AuthControllerIT {

    @Container
    @ServiceConnection
    static MongoDBContainer  mongoDBContainer = new MongoDBContainer("mongo:4.4");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_retourne201_siInscriptionValide()throws Exception {
        //
        RegisterRequest request = new RegisterRequest(
            "medecin@medapp.com" , "MotDePasse123!" , "Dupont" , "Jean" , Role.MEDECIN
        );

        //when/then
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("medecin@medapp.com"));   
    }

    @Test
    void register_retourne409_siEmailDejaUtilise()throws Exception {
        //
        RegisterRequest rquest = new RegisterRequest(
            "medecin@medapp.com" , "MotDePasse123!" , "Dupont" , "Jean" , Role.MEDECIN
        );

        //on inscrit une premiere fois
        mockMvc.perform(post("/api/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(rquest)));

        //when / then la deuxieme inscription avec la meme email doit echouer
        mockMvc.perform(post("/api/auth/register")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(rquest)))
                .andExpect(status().isConflict());

    }
    
}
