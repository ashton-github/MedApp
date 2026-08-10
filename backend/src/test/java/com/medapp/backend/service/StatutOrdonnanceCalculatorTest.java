package com.medapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.medapp.backend.model.StatutOrdonnance;

public class StatutOrdonnanceCalculatorTest {

    @Test
    void calculer_retourneACTIVE_siDateValiditeDansLeFutur(){
        LocalDate dateValidite = LocalDate.now().plusMonths(1);

        StatutOrdonnance result = StatutOrdonnanceCalculator.calculer(dateValidite , null);

        assertEquals(StatutOrdonnance.ACTIVE, result);
    }

    @Test
    void calculer_retourneACTIVE_siDateValiditeAujourdhui(){
        LocalDate dateValidite = LocalDate.now();

        StatutOrdonnance result = StatutOrdonnanceCalculator.calculer(dateValidite, null);

        assertEquals(StatutOrdonnance.ACTIVE, result);

    }

    @Test
    void calculer_retourneExpiree_siDateValiditeDansLePasse(){
        LocalDate dateValidite = LocalDate.now().minusDays(1);

        StatutOrdonnance result = StatutOrdonnanceCalculator.calculer(dateValidite, null);

        assertEquals(StatutOrdonnance.EXPIREE, result);
    }

    @Test
    void calculer_lanceException_siDateValiditeNull(){
        assertThrows(IllegalArgumentException.class,
            () -> StatutOrdonnanceCalculator.calculer(null, null));
    }

    @Test
    void calculer_retourneArchivee_siOrdonnanceExplicitementArchivee() {
        LocalDate dateValiditeFuture = LocalDate.now().plusMonths(1);

        StatutOrdonnance resultat = StatutOrdonnanceCalculator.calculer(dateValiditeFuture, StatutOrdonnance.ARCHIVEE);

        assertEquals(StatutOrdonnance.ARCHIVEE, resultat);
    }
    
}
