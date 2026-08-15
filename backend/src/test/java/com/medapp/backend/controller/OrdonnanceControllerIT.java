package com.medapp.backend.controller;


import org.junit.jupiter.api.AfterEach;
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
import com.medapp.backend.repository.OrdonnanceRepository;
import com.medapp.backend.repository.PatientRepository;
import com.medapp.backend.repository.UserRepository;
import com.medapp.backend.service.OrdonnanceService;


import org.testcontainers.junit.jupiter.Container;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired 
    private OrdonnanceService ordonnanceService;

    @Autowired
    private OrdonnanceRepository ordonnanceRepository;
    
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void nettoyageBase(){
        ordonnanceRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

   
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

    @Test
    void creeOrdonnance_retourne403SiRoleDifferentauMedecin()throws Exception{

        String tokenSecretaire = obtenirAccessToken("secretaire-test@medapp.com", Role.SECRETAIRE);
        String patientId = creerPatientEtRecupererId(tokenSecretaire, "8776876786");

        OrdonnanceRequest request = new OrdonnanceRequest(
            patientId , LocalDate.now().plusMonths(1),
            List.of(new Medicament("Duliprane" , "1000mg" , "3x/jour" , "5 jours")) , 
            null
        );

        mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization" , "Bearer " + tokenSecretaire)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

    }

    @Test
    void obtenirOrdonnance_retourne200_siExiste()throws Exception{
        String tokenMedecin = obtenirAccessToken("medecin-test@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "8776876786");

        OrdonnanceRequest request = new OrdonnanceRequest(
            patientId , LocalDate.now().plusMonths(1),
            List.of(new Medicament("Duliprane" , "1000mg" , "3x/jour" , "5 jours")) , 
            null
        );

        MvcResult result = mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization" , "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andReturn();

        String ordonnanceId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(get("/api/ordonnances/" + ordonnanceId)
                        .header("Authorization" , "Bearer " + tokenMedecin ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ordonnanceId))
                    .andExpect(jsonPath("$.patientId").value(patientId));
    }

    @Test
    void obtenirHistorique_retourneOrdonnancesDuPatient()throws Exception{
        String tokenMedecin = obtenirAccessToken("medecin-ordo-historique@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "8776876787");

        OrdonnanceRequest request = new OrdonnanceRequest(
            patientId, LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null
        );

        mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/ordonnances/patient/" + patientId)
                        .header("Authorization" , "Bearer " + tokenMedecin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].patientId").value(patientId));
                        
    }


    @Test
    void obtenirHistorique_filtreParStatut_quandParamPresent()throws Exception{
          String tokenMedecin = obtenirAccessToken("medecin-ordo-historique@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "8776876787");

        OrdonnanceRequest request = new OrdonnanceRequest(
            patientId, LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null
        );

        mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/ordonnances/patient/" + patientId)
                        .param("statut" , "ACTIVE")
                        .header("Authorization" , "Bearer " + tokenMedecin))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].statut").value("ACTIVE"));
    }

    @Test
    void archiverOrdonnance_retourne200_etPasseStatutAArchivee()throws Exception{
        String tokenMedecin = obtenirAccessToken("medecin-ordo-historique@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "8776876787");

        OrdonnanceRequest request = new OrdonnanceRequest(
            patientId, LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null
        );

        MvcResult result = mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String ordonnanceId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(patch("/api/ordonnances/" + ordonnanceId + "/archiver")
                        .header("Authorization" , "Bearer " + tokenMedecin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ARCHIVEE"));

        
    }


    @Test
    void archiverOrdonnance_retourne403_siMedecinNestPasLePrescripteur() throws Exception {
        String tokenMedecin1 = obtenirAccessToken("medecin-ordo-archivage-1@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin1, "8776876791");

        OrdonnanceRequest request = new OrdonnanceRequest(
            patientId, LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null
        );

        MvcResult creationResult = mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization", "Bearer " + tokenMedecin1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String ordonnanceId = objectMapper.readTree(creationResult.getResponse().getContentAsString()).get("id").asText();

        String tokenMedecin2 = obtenirAccessToken("medecin-ordo-archivage-2@medapp.com", Role.MEDECIN);

        mockMvc.perform(patch("/api/ordonnances/" + ordonnanceId + "/archiver")
                        .header("Authorization", "Bearer " + tokenMedecin2))
                .andExpect(status().isForbidden());
    }

    @Test
    void modifierOrdonnance_retourne200_siMedecinEstLePrescripteur()throws Exception{
        String tokenMedecin = obtenirAccessToken("medecin-ordo-modif@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "879765643432326");

        OrdonnanceRequest creationRequest = new OrdonnanceRequest(
            patientId, LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null
        );

        MvcResult result = mockMvc.perform(post("/api/ordonnances")
                    .header("Authorization","Bearer " + tokenMedecin)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String ordonnanceId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        OrdonnanceRequest modificationRequest = new OrdonnanceRequest(
            patientId, LocalDate.now().plusMonths(2),
            List.of(new Medicament("Doliprane", "500mg", "2x/jour", "3 jours")),
            "Dosage ajuste"
        );

        mockMvc.perform(put("/api/ordonnances/" + ordonnanceId)
                    .header("Authorization", "Bearer " + tokenMedecin)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(modificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicaments[0].dosage").value("500mg"))
                .andExpect(jsonPath("$.remarques").value("Dosage ajuste"));
    }


    @Test
    void modifierOrdonnance_retourne403_siMedecinNestPasLePrescripteur() throws Exception{
        String tokenMedecin = obtenirAccessToken("medecin-ordo-modif@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "879765643432326");

        OrdonnanceRequest creationRequest = new OrdonnanceRequest(
            patientId, LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null
        );

        MvcResult result = mockMvc.perform(post("/api/ordonnances")
                    .header("Authorization","Bearer " + tokenMedecin)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String ordonnanceId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        OrdonnanceRequest modificationRequest = new OrdonnanceRequest(
            patientId, LocalDate.now().plusMonths(2),
            List.of(new Medicament("Doliprane", "500mg", "2x/jour", "3 jours")),
            "Dosage ajuste"
        );

        String tokenMedecin2 = obtenirAccessToken("medecin-ordo-modif-2@medapp.com", Role.MEDECIN);

        mockMvc.perform(put("/api/ordonnances/" + ordonnanceId)
                    .header("Authorization", "Bearer " + tokenMedecin2)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(modificationRequest)))
                .andExpect(status().isForbidden());
    }
    
}








