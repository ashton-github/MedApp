package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medapp.backend.TestDataFactory;
import com.medapp.backend.dto.UserResponse;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void listerMedecinsActifs_retourneDesUserResponseSansMotDePasse() {
        User medecin = TestDataFactory.unUtilisateur(Role.MEDECIN);
        
        medecin.setId("medecin-1");
        medecin.setNom("Martin");
        medecin.setPrenom("Jean");

        when(userRepository.findByRoleAndActifTrue(Role.MEDECIN)).thenReturn(List.of(medecin));

        List<UserResponse> resultat = userService.listerMedecinsActifs();

        assertEquals(1, resultat.size());
        assertEquals("medecin-1", resultat.get(0).id());
        assertEquals("Martin", resultat.get(0).nom());
        assertEquals("Jean", resultat.get(0).prenom());
    }
}