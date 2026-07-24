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
import org.mockito.junit.jupiter.MockitoExtension;

import com.medapp.backend.exception.DonneesInvalidesException;
import com.medapp.backend.exception.NumeroSecuriteSocialeDejaExistantException;
import com.medapp.backend.exception.PatientIntrouvableException;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.Sexe;
import com.medapp.backend.repository.PatientRepository;

@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void creerPatient_reussit_siDonneesValides(){
        //given
        Patient patient = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512123456",
                List.of("Diabète type 2"), null, null, null);

        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));
        //when
        Patient patientCree = patientService.creerPatient(patient);

        //then
        assertEquals("Dupont", patientCree.getNom());
        assertNotNull(patientCree.getDateCreation());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void creerPatient_lanceException_siNumeroSecuriteSocialeDejaExistant(){
        //given
        String numero = "1900512123456";
        Patient patientExistant = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", numero,
                List.of(), null, null, null);

        when(patientRepository.findByNumeroSecuriteSociale(numero)).thenReturn(Optional.of(patientExistant));

         Patient nouveauPatient = new Patient("Martin", "Paul", LocalDate.of(1985, 1, 1), Sexe.M,
                "87654321", "1 rue de Rome", numero,
                List.of(), null, null, null);

        assertThrows(NumeroSecuriteSocialeDejaExistantException.class, () -> 
            patientService.creerPatient(nouveauPatient)
        );
        verify(patientRepository , never()).save(any(Patient.class));
    }


    @Test
    void creerPatient_lanceException_siDonneesInvalides(){
        //given
        Patient patientDateFuture = new Patient("Dupont", "Marie", LocalDate.now().plusDays(1), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512123457",
                List.of(), null, null, null);

        assertThrows(DonneesInvalidesException.class, () -> 
            patientService.creerPatient(patientDateFuture));

        verify(patientRepository , never()).save(any(Patient.class));
    }

    @Test
    void rechercherPatients_retournePatientsCorrespondants_parNomOuPrenom(){
        String requete = "dupo";
        Patient patient1 = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512123458",
                List.of(), null, null, null);
        patient1.setId("patient-1");
        Patient patient2 = new Patient("Martin", "Dupois", LocalDate.of(1985, 1, 1), Sexe.M,
                "87654321", "1 rue de Rome", "1850101654322",
                List.of(), null, null, null);
        patient2.setId("patient-2");

        when(patientRepository.findByNomContainingIgnoreCase(requete)).thenReturn(List.of(patient1));
        when(patientRepository.findByPrenomContainingIgnoreCase(requete)).thenReturn(List.of(patient2));

       
        List<Patient> resultats = patientService.rechercherPatients(requete);
        assertEquals(2, resultats.size());
    }

    @Test
    void obtenirPatient_lanceException_siIdInexistant(){
        String idInexistant = "id-inexistant";
        when(patientRepository.findById(idInexistant)).thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class, () -> 
            patientService.obtenirPatient(idInexistant));
    }

    @Test
    void modifierPatient_metAJourDateMiseAJour_siDonneesValides(){
        String id = "patient-existant-id";
        Patient patientExistant = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512123459",
                List.of(), null, null, null);
        patientExistant.setId(id);

        Patient patientModifie = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "99999999", "12 rue de la Paix", "1900512123459",
                List.of(), null, null, null);

        when(patientRepository.findById(id)).thenReturn(Optional.of(patientExistant));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient resultat = patientService.modifierPatient(id , patientModifie);

        assertEquals("99999999", resultat.getTelephone());
        assertNotNull(resultat.getDateMiseAJour());

    }

    @Test
    void modifierPatient_lanceException_siIdInexistant() {
        // given
        String idInexistant = "id-inexistant";
        Patient patientModifie = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "99999999", "12 rue de la Paix", "1900512123459",
                List.of(), null, null, null);

        when(patientRepository.findById(idInexistant)).thenReturn(Optional.empty());

        // then
        assertThrows(PatientIntrouvableException.class, () ->
            patientService.modifierPatient(idInexistant, patientModifie)
        );
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void supprimerPatient_reussit_siPatientExiste() {
        // given
        String id = "patient-existant-id";
        Patient patientExistant = new Patient("Dupont", "Marie", LocalDate.of(1990, 5, 12), Sexe.F,
                "12345678", "12 rue de la Paix", "1900512123460",
                List.of(), null, null, null);
        patientExistant.setId(id);

        when(patientRepository.findById(id)).thenReturn(Optional.of(patientExistant));

        // when
        patientService.supprimerPatient(id);

        // then
        verify(patientRepository).deleteById(id);
    }
    
}
