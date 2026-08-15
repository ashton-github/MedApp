package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import com.medapp.backend.exception.AccesRefuseException;
import com.medapp.backend.exception.OrdonnanceDejaArchiveeException;
import com.medapp.backend.exception.OrdonnanceIntrouvableException;
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

    @Test
    void archiverOrdonnance_passeStatutAArichivee_siOrdonnanceExiste() {
        String id = "ordonnance-1";
        Ordonnance ordonnance = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.ACTIVE, null
        );
        ordonnance.setId(id);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance result = ordonnanceService.archiverOrdonnance(id , "medecin-1");

        assertEquals(StatutOrdonnance.ARCHIVEE, result.getStatut());
    }

    @Test
    void archiverOrdonnance_lanceException_siIdInexistant(){
        String idInexistant = "id-inexistant";

        when(ordonnanceRepository.findById(idInexistant)).thenReturn(Optional.empty());

        assertThrows(OrdonnanceIntrouvableException.class,
            () -> ordonnanceService.archiverOrdonnance(idInexistant , "medecin-1") );

        verify(ordonnanceRepository , never()).save(any(Ordonnance.class));
    }


    @Test
    void obtenirHistorique_retourneOrdonnancesDuPatient_trieesParDate(){
        String patientId = "patient-123";

        Ordonnance ancienne = new Ordonnance(
            patientId, "medecin-1", LocalDate.now().minusMonths(2), LocalDate.now().minusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.EXPIREE, null
        );
        ancienne.setId("ordo-1");

        Ordonnance recente = new Ordonnance(
            patientId, "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Amoxicilline", "500mg", "2x/jour", "7 jours")),
            StatutOrdonnance.ACTIVE, null
        );
        recente.setId("ordo-2");

        //repository returns them out of order , service must sort 
        when(ordonnanceRepository.findByPatientId(patientId)).thenReturn(List.of(ancienne , recente));

        List<Ordonnance> result = ordonnanceService.obtenirHistorique(patientId , null);

        assertEquals(2, result.size());
        assertEquals("ordo-2", result.get(0).getId());
        assertEquals("ordo-1", result.get(1).getId());

    }

    @Test
    void obtenirHistorique_filtreParStatut_siStatutFourni() {
        String patientId = "patient-123";

        Ordonnance active = new Ordonnance(
            patientId, "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.ACTIVE, null
        );
        active.setId("ordo-active");

        when(ordonnanceRepository.findByPatientIdAndStatut(patientId, StatutOrdonnance.ACTIVE))
            .thenReturn(List.of(active));

        List<Ordonnance> resultat = ordonnanceService.obtenirHistorique(patientId, StatutOrdonnance.ACTIVE);

        assertEquals(1, resultat.size());
        assertEquals("ordo-active", resultat.get(0).getId());
    }

    @Test
    void obtenirHistorique_lanceException_siPatientInexistant() {
        String patientIdInexistant = "patient-inexistant";
        when(patientRepository.findById(patientIdInexistant)).thenReturn(Optional.empty());

        assertThrows(PatientIntrouvableException.class,
            () -> ordonnanceService.obtenirHistorique(patientIdInexistant, null));
    }


      @Test
    void archiverOrdonnance_lanceException_siMedecinNestPasLePrescripteur(){
        String id = "ordonnance-1";
        Ordonnance ordonnance = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.ACTIVE, null
        );
        ordonnance.setId(id);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));

        assertThrows(AccesRefuseException.class, 
            () -> ordonnanceService.archiverOrdonnance(id , "medecin-2")
        );

        verify(ordonnanceRepository , never()).save(any(Ordonnance.class));
    }

    @Test
    void archiverOrdonnance_lanceException_siDejaArchivee() {
        String id = "ordonnance-1";
        Ordonnance ordonnance = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.ARCHIVEE, null
        );
        ordonnance.setId(id);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));

        assertThrows(OrdonnanceDejaArchiveeException.class,
            () -> ordonnanceService.archiverOrdonnance(id, "medecin-1"));

        verify(ordonnanceRepository, never()).save(any(Ordonnance.class));
    }

    @Test
    void modifierOrdonnance_reussit_siMedecinEstLePrescripteur(){
        String id = "ordonnance-1";

        Ordonnance ordonnanceExistante = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.ACTIVE, null
        );
        ordonnanceExistante.setId(id);

        Ordonnance ordonnanceModifiee = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(2),
            List.of(new Medicament("Doliprane", "500mg", "2x/jour", "3 jours")),
            null, "Dosage ajuste"
        );

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnanceExistante));
        when(ordonnanceRepository.save(any(Ordonnance.class))).thenAnswer(inv -> inv.getArgument(0));

        Ordonnance result = ordonnanceService.modifierOrdonnance(id , ordonnanceModifiee , "medecin-1");

        assertEquals("500mg", result.getMedicaments().get(0).getDosage());
        assertEquals("Dosage ajuste", result.getRemarques());
    }


    @Test
    void modifierOrdonnance_lanceException_siMedecinNestPasLePrescripteur(){

         String id = "ordonnance-1";

        Ordonnance ordonnanceExistante = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.ACTIVE, null
        );
        ordonnanceExistante.setId(id);

        Ordonnance ordonnanceModifiee = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(2),
            List.of(new Medicament("Doliprane", "500mg", "2x/jour", "3 jours")),
            null, "Dosage ajuste"
        );

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnanceExistante));

        assertThrows(AccesRefuseException.class,
            () -> ordonnanceService.modifierOrdonnance(id , ordonnanceModifiee , "medecin-2")
        );

        verify(ordonnanceRepository,never()).save(any(Ordonnance.class));
    }

    @Test
    void modifierOrdonnance_lanceException_siIdInexistant() {
        String idInexistant = "id-inexistant";
        Ordonnance ordonnanceModifiee = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(2),
            List.of(new Medicament("Doliprane", "500mg", "2x/jour", "3 jours")),
            null, null
        );

        when(ordonnanceRepository.findById(idInexistant)).thenReturn(Optional.empty());

        assertThrows(OrdonnanceIntrouvableException.class,
            () -> ordonnanceService.modifierOrdonnance(idInexistant, ordonnanceModifiee, "medecin-1"));

        verify(ordonnanceRepository, never()).save(any(Ordonnance.class));
    }

    @Test
    void modifierOrdonnance_lanceException_siOrdonnanceEstArchive(){
        String id = "ordonnance-1";

        Ordonnance ordonnance = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.ARCHIVEE, null
        );
        ordonnance.setId(id);


        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));

        assertThrows(OrdonnanceDejaArchiveeException.class,
            () -> ordonnanceService.modifierOrdonnance(id, ordonnance, "medecin-1")
        );
    }


    @Test
    void generatePdf_retourneDesBytesNonVides_siOrdonnanceExiste(){
        String id = "ordonnance-1";
        Ordonnance ordonnance = new Ordonnance(
            "patient-123", "medecin-1", LocalDate.now(), LocalDate.now().plusMonths(1),
            List.of(new Medicament("Doliprane", "1000mg", "3x/jour", "5 jours")),
            StatutOrdonnance.ACTIVE, "RAS"
        );
        ordonnance.setId(id);

        when(ordonnanceRepository.findById(id)).thenReturn(Optional.of(ordonnance));

        byte[] result = ordonnanceService.generatePdf(id);

        assertNotNull(result);
        assertTrue(result.length > 0);
        //a real PDF file always starts with this exact byte signature
        assertEquals("%PDF", new String(result , 0 , 4));
    }

    @Test
    void generatePdf_lanceException_siIdInexistant(){
        String idInexistant = "id-inexistant";
        when(ordonnanceRepository.findById(idInexistant)).thenReturn(Optional.empty());

        assertThrows(OrdonnanceIntrouvableException.class,
            () -> ordonnanceService.generatePdf(idInexistant));
    }



    
}
