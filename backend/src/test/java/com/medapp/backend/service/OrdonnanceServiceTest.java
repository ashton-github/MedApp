package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.matchers.Or;
import org.mockito.junit.jupiter.MockitoExtension;

import com.medapp.backend.exception.PatientIntrouvableException;
import com.medapp.backend.model.Medicament;
import com.medapp.backend.model.Ordonnance;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.Sexe;
import com.medapp.backend.model.StatutOrdonnance;
import com.medapp.backend.repository.OrdonnanceRepository;
import com.medapp.backend.repository.PatientRepository;

@ExtendWith(MockitoExtension.class)
public class OrdonnanceServiceTest {

    @Mock
    private OrdonnanceRepository ordonnanceRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private OrdonnanceService ordonnanceService;

    @Test
    void creeOrdonnance_reussit_siPatientExistantEtMedecinAutorise(){

        String patientId = "patient-123";
        Patient patient = new Patient(
            "Dupont" , "Marie" , LocalDate.of(1990 , 5 , 12) , Sexe.F,
            "12345678" , "12 rue de la Paix" , "6576644354347768", 
            List.of() , null , null , null  
        );
        patient.setId(patientId);

        Ordonnance ordonnance = new Ordonnance(
            patientId, "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null, null
        );

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance result = ordonnanceService.creerOrdonnance(ordonnance);

        assertNotNull(result);
        assertEquals(patientId, result.getPatientId());

    }

    @Test
    void creerOrdonnance_lanceException_siPatientInexistant(){
        String patientIdInexistant = "patient-inexistant";

        Ordonnance ordonnance = new Ordonnance(
            patientIdInexistant, "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null, null
        );

        when(patientRepository.findById(patientIdInexistant)).thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class,
            () -> ordonnanceService.creerOrdonnance(ordonnance));

        verify(ordonnanceRepository , never()).save(any(Ordonnance.class));
    }


    @Test
    void creerOrdonnance_calculeStatutActive_siDateValiditeFuture() {

        String patientId = "patient-123";
        Patient patient = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
            "12345678", "12 rue de la Paix", "6576644354347768",
            List.of(), null, null, null);
        patient.setId(patientId);

        Ordonnance ordonnance = new Ordonnance(
            patientId, "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null, null
        );

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance resultat = ordonnanceService.creerOrdonnance(ordonnance);

        assertEquals(StatutOrdonnance.ACTIVE, resultat.getStatut());
    }

    @Test
    void creerOrdonnance_calculeStatutExpiree_siDateValiditeDansLePasse(){
        String patientId = "patient-123";
        Patient patient = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
            "12345678", "12 rue de la Paix", "6576644354347768",
            List.of(), null, null, null);
        patient.setId(patientId);

        Ordonnance ordonnance = new Ordonnance(
            patientId, "medecin-1", LocalDate.now().minusMonths(2), LocalDate.now().minusDays(5),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            null, null
        );

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance resultat = ordonnanceService.creerOrdonnance(ordonnance);

        assertEquals(StatutOrdonnance.EXPIREE, resultat.getStatut());


    }


    
}
