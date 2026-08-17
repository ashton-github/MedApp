package com.medapp.backend.controller;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.medapp.backend.TestDataFactory;
import com.medapp.backend.dto.OrdonnanceRequest;
import com.medapp.backend.dto.PatientRequest;
import com.medapp.backend.model.Medicament;
import com.medapp.backend.model.Role;
import com.medapp.backend.repository.OrdonnanceRepository;
import com.medapp.backend.repository.PatientRepository;
import com.medapp.backend.repository.UserRepository;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;


public class OrdonnanceControllerIT extends IntegrationTestBase {

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

    private String creerPatientEtRecupererId(String tokenMedecin, String numeroSecurite) throws Exception {
        PatientRequest patientRequest = TestDataFactory.unPatientRequest();

        MvcResult result = mockMvc.perform(post("/api/patients")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }

    private String creerOrdonnanceEtRecupererId(String tokenMedecin, OrdonnanceRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }


    @Test
    void creerOrdonnance_retourne201_siDonneeValides()throws Exception{
        String tokenMedcin = obtenirAccessToken("medecin-ordo-creation@medapp.com", Role.MEDECIN);

        String patientId = creerPatientEtRecupererId(tokenMedcin, "8776876786");

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId);

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

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId);

        mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization" , "Bearer " + tokenSecretaire)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

    }

    @Test
    void creerOrdonnance_retourne400_siListeMedicamentsVide() throws Exception {
        String tokenMedecin = obtenirAccessToken("medecin-ordo-validation@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "8776876796");

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId , List.of());


        mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void creerOrdonnance_retourne400_siMedicamentAChampVide() throws Exception {
        String tokenMedecin = obtenirAccessToken("medecin-ordo-validation-2@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "8776876797");

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId , List.of(new Medicament("", "1000mg", "3x/jour", "5 jours")));

        mockMvc.perform(post("/api/ordonnances")
                        .header("Authorization", "Bearer " + tokenMedecin)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenirOrdonnance_retourne200_siExiste()throws Exception{
        String tokenMedecin = obtenirAccessToken("medecin-test@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "8776876786");

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId);
        String ordonnanceId = creerOrdonnanceEtRecupererId(tokenMedecin, request);

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

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId);
        creerOrdonnanceEtRecupererId(tokenMedecin, request);

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

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId);
        creerOrdonnanceEtRecupererId(tokenMedecin, request);

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

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId);
        String ordonnanceId = creerOrdonnanceEtRecupererId(tokenMedecin, request);

        mockMvc.perform(patch("/api/ordonnances/" + ordonnanceId + "/archiver")
                        .header("Authorization" , "Bearer " + tokenMedecin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ARCHIVEE"));

    }


    @Test
    void archiverOrdonnance_retourne403_siMedecinNestPasLePrescripteur() throws Exception {
        String tokenMedecin1 = obtenirAccessToken("medecin-ordo-archivage-1@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin1, "8776876791");

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId);
        String ordonnanceId = creerOrdonnanceEtRecupererId(tokenMedecin1, request);

        String tokenMedecin2 = obtenirAccessToken("medecin-ordo-archivage-2@medapp.com", Role.MEDECIN);

        mockMvc.perform(patch("/api/ordonnances/" + ordonnanceId + "/archiver")
                        .header("Authorization", "Bearer " + tokenMedecin2))
                .andExpect(status().isForbidden());
    }

    @Test
    void modifierOrdonnance_retourne200_siMedecinEstLePrescripteur()throws Exception{
        String tokenMedecin = obtenirAccessToken("medecin-ordo-modif@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "879765643432326");

        OrdonnanceRequest creationRequest = TestDataFactory.uneOrdonnanceRequest(patientId);
        String ordonnanceId = creerOrdonnanceEtRecupererId(tokenMedecin, creationRequest);

        OrdonnanceRequest modificationRequest = TestDataFactory.uneOrdonnanceRequest(patientId ,  List.of(new Medicament("Doliprane", "500mg", "2x/jour", "3 jours")));

        mockMvc.perform(put("/api/ordonnances/" + ordonnanceId)
                    .header("Authorization", "Bearer " + tokenMedecin)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(modificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicaments[0].dosage").value("500mg"));
    }


    @Test
    void modifierOrdonnance_retourne403_siMedecinNestPasLePrescripteur() throws Exception{
        String tokenMedecin = obtenirAccessToken("medecin-ordo-modif@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "879765643432326");

        OrdonnanceRequest creationRequest = TestDataFactory.uneOrdonnanceRequest(patientId);
        String ordonnanceId = creerOrdonnanceEtRecupererId(tokenMedecin, creationRequest);

        OrdonnanceRequest modificationRequest = TestDataFactory.uneOrdonnanceRequest(patientId, LocalDate.now().plusMonths(2));

        String tokenMedecin2 = obtenirAccessToken("medecin-ordo-modif-2@medapp.com", Role.MEDECIN);

        mockMvc.perform(put("/api/ordonnances/" + ordonnanceId)
                    .header("Authorization", "Bearer " + tokenMedecin2)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(modificationRequest)))
                .andExpect(status().isForbidden());
    }


    @Test
    void exporterOrdonnancePdf_retourne200_avecContentTypePdf()throws Exception{
        String tokenMedecin = obtenirAccessToken("medecin-ordo-pdf@medapp.com", Role.MEDECIN);
        String patientId = creerPatientEtRecupererId(tokenMedecin, "8776876795");

        OrdonnanceRequest request = TestDataFactory.uneOrdonnanceRequest(patientId);
        String ordonnanceId = creerOrdonnanceEtRecupererId(tokenMedecin, request);

        mockMvc.perform(get("/api/ordonnances/" + ordonnanceId + "/pdf")
                        .header("Authorization","Bearer " + tokenMedecin))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF));

    }

}