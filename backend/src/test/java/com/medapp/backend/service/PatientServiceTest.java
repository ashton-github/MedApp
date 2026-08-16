package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.medapp.backend.TestDataFactory;
import com.medapp.backend.exception.DonneesInvalidesException;
import com.medapp.backend.exception.NumeroSecuriteSocialeDejaExistantException;
import com.medapp.backend.exception.PatientIntrouvableException;
import com.medapp.backend.model.Patient;
import com.medapp.backend.model.Role;
import com.medapp.backend.model.User;
import com.medapp.backend.repository.PatientRepository;
import com.medapp.backend.repository.UserRepository;


@ExtendWith(MockitoExtension.class)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    
    @Mock 
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

    @Test
    void creerPatient_reussit_siDonneesValides(){
        //given
        Patient patient = TestDataFactory.unPatient();
        
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
        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setNumeroSecuriteSociale(numero);
        
        when(patientRepository.findByNumeroSecuriteSociale(numero)).thenReturn(Optional.of(patientExistant));

        Patient nouveauPatient = TestDataFactory.unPatient();
        nouveauPatient.setNumeroSecuriteSociale(numero);

        assertThrows(NumeroSecuriteSocialeDejaExistantException.class, () -> 
            patientService.creerPatient(nouveauPatient)
        );
        verify(patientRepository , never()).save(any(Patient.class));
    }


    @Test
    void creerPatient_lanceException_siDonneesInvalides(){
        //given
        Patient patientDateFuture = TestDataFactory.unPatient();
        patientDateFuture.setDateNaissance(LocalDate.now().plusDays(1)); // date de naissance dans le futur
     
        assertThrows(DonneesInvalidesException.class, () -> 
            patientService.creerPatient(patientDateFuture));

        verify(patientRepository , never()).save(any(Patient.class));
    }

    @Test
    void rechercherPatients_retournePatientsCorrespondants_parNomOuPrenom(){
        String requete = "dupo";

        Patient patient1 = TestDataFactory.unPatient();
        patient1.setId("patient-1");

        Patient patient2 = TestDataFactory.unPatient();
        patient2.setNom("Martin");
        patient2.setPrenom("Dupois");
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
        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setId(id);

        Patient patientModifie = TestDataFactory.unPatient();
        patientModifie.setTelephone("99999999");

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
        Patient patientModifie = TestDataFactory.unPatient();

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
        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setId(id);

        when(patientRepository.findById(id)).thenReturn(Optional.of(patientExistant));

        // when
        patientService.supprimerPatient(id);

        // then
        verify(patientRepository).deleteById(id);
    }

    @Test
    void supprimerPatient_lanceException_siIdInexistant(){
        String idInexistant = "id-inexistant";
        when(patientRepository.findById(idInexistant)).thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class , () -> patientService.supprimerPatient(idInexistant) );

        verify(patientRepository , never()).deleteById(anyString());
    }

    @Test
    void masquerNumeroSecuriteSociale_pourRoleSecretaire(){
        Patient patient = TestDataFactory.unPatient();
        patient.setNumeroSecuriteSociale("1900512123456");

        Patient result = patientService.appliquerMasquageSelonRole(patient , Role.SECRETAIRE);
        assertEquals("XXXXXXXXXX456", result.getNumeroSecuriteSociale());
    }

    @Test 
    void nePasMasquerNumeroSecuriteSociale_pourRoleMedecinOuAdmin(){
       
        Patient patientPourMedecin = TestDataFactory.unPatient();
        patientPourMedecin.setNumeroSecuriteSociale("1900512123456");
        Patient patientPourAdmin = TestDataFactory.unPatient();
        patientPourAdmin.setNumeroSecuriteSociale("1900512123456");
       
        Patient resultMedecin = patientService.appliquerMasquageSelonRole(patientPourMedecin , Role.MEDECIN);
        Patient resultAdmin = patientService.appliquerMasquageSelonRole(patientPourAdmin , Role.ADMIN);

        assertEquals("1900512123456", resultMedecin.getNumeroSecuriteSociale());
        assertEquals("1900512123456", resultAdmin.getNumeroSecuriteSociale());
    }
    

    @Test 
    void listerPatients_retourneUnePageDePatients(){
        Patient patient1 = TestDataFactory.unPatient();
        patient1.setId("patient-1");

        Pageable pageable = PageRequest.of(0 , 10 );
        
        Page<Patient> pageAttendue = new PageImpl<>(List.of(patient1), pageable, 1);
        when(patientRepository.findAll(pageable)).thenReturn(pageAttendue);

        Page<Patient> result = patientService.listerPatients(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Dupont", result.getContent().get(0) .getNom());

        
    }

    @Test
    void modifierPatient_metAJourLePrenom_siDonneesValides() {
        String id = "patient-existant-id";
        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setId(id);
        
        Patient patientModifie = TestDataFactory.unPatient();
        patientModifie.setPrenom("Marie-Claire");   
        
        when(patientRepository.findById(id)).thenReturn(Optional.of(patientExistant));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient resultat = patientService.modifierPatient(id, patientModifie);

        assertEquals("Marie-Claire", resultat.getPrenom());
    }

    @Test
    void modifierPatient_lanceException_siNouveauNumeroSecuriteSocialeDejaExistatn(){
        String id = "patient-existant-id";

        Patient patientExistant = TestDataFactory.unPatient();
        patientExistant.setId(id);

        Patient patientModifie = TestDataFactory.unPatient();
        patientModifie.setNumeroSecuriteSociale("1900513999999"); // NSS belonging to another patient
        patientModifie.setId("patient-modifie-id");

        Patient autrePatient = TestDataFactory.unPatient();
        autrePatient.setNumeroSecuriteSociale("1900513999999"); // NSS belonging to another patient
        autrePatient.setId("autre-patient-id");

        when(patientRepository.findById(id)).thenReturn(Optional.of(patientExistant));
        when((patientRepository.findByNumeroSecuriteSociale("1900513999999"))).thenReturn(Optional.of(autrePatient));

        assertThrows(NumeroSecuriteSocialeDejaExistantException.class,
            () -> patientService.modifierPatient(id , patientModifie));

    }

    @Test
    void creerPatient_reussit_siMedcinReferentAbsent(){
        Patient patient = TestDataFactory.unPatient();
       
        when(patientRepository.findByNumeroSecuriteSociale(any())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenAnswer(inv -> inv.getArgument(0));

        Patient result = patientService.creerPatient(patient);
        
        assertNotNull(result);
    }

    @Test
    void creerPatient_lanceException_siMedecinReferentInexistant(){
        Patient patient = TestDataFactory.unPatient();
        patient.setMedecinReferent("id-inexistant");

        when(patientRepository.findByNumeroSecuriteSociale(any())).thenReturn(Optional.empty());
        when(userRepository.findById("id-inexistant")).thenReturn(Optional.empty());

        assertThrows(DonneesInvalidesException.class,
            () ->  patientService.creerPatient(patient));
    }
    
    @Test
    void creerPatient_lanceException_siMedecinReferentNaPasLeRoleMedecin(){

        Patient patient = TestDataFactory.unPatient();
        patient.setMedecinReferent("id-secretaire");

        User secretaire = TestDataFactory.unUtilisateur(Role.SECRETAIRE);
        secretaire.setId("id-secretaire");

        when(patientRepository.findByNumeroSecuriteSociale(any())).thenReturn(Optional.empty());
        when(userRepository.findById("id-secretaire")).thenReturn(Optional.of(secretaire));

        assertThrows(DonneesInvalidesException.class,
            () -> patientService.creerPatient(patient));
    }
}
