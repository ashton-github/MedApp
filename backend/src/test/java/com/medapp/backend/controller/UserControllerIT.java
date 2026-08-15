package com.medapp.backend.controller;

import org.junit.jupiter.api.Test;
import com.medapp.backend.model.Role;



import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

public class UserControllerIT extends IntegrationTestBase {

    

     
    @Test
    void listerUtilisateurs_retourneMedecinsActifs_siRoleMedecin() throws Exception {
        String token = obtenirAccessToken("medecin-liste-users@medapp.com", Role.MEDECIN);

        mockMvc.perform(get("/api/users")
                        .param("role", "MEDECIN")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void listerUtilisateurs_retourneListeVide_siRoleAutreQueMedecin() throws Exception {
        String token = obtenirAccessToken("secretaire-liste-users@medapp.com", Role.SECRETAIRE);

        mockMvc.perform(get("/api/users")
                        .param("role", "SECRETAIRE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
    
}
