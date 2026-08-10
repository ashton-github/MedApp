package com.medapp.backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.medapp.backend.exception.OrdonnanceIntrouvableException;
import com.medapp.backend.exception.PatientIntrouvableException;
import com.medapp.backend.model.Ordonnance;
import com.medapp.backend.model.StatutOrdonnance;
import com.medapp.backend.repository.OrdonnanceRepository;
import com.medapp.backend.repository.PatientRepository;

@Service
public class OrdonnanceService {

    final private OrdonnanceRepository ordonnanceRepository;
    final private PatientRepository patientRepository;

    public OrdonnanceService(OrdonnanceRepository ordonnanceRepository , PatientRepository patientRepository){
        this.ordonnanceRepository = ordonnanceRepository;
        this.patientRepository = patientRepository;
    }

    public Ordonnance creerOrdonnance(Ordonnance ordonnance){
        patientRepository.findById(ordonnance.getPatientId()).orElseThrow(
            () -> new PatientIntrouvableException(ordonnance.getPatientId())
        );

        ordonnance.setStatut(StatutOrdonnanceCalculator.calculer(ordonnance.getDateValidite(), ordonnance.getStatut()));
        return ordonnanceRepository.save(ordonnance);
    }

    public Ordonnance archiverOrdonnance(String ordonnanceId) {
        Ordonnance ordonnance = ordonnanceRepository.findById(ordonnanceId)
                    .orElseThrow(() -> new OrdonnanceIntrouvableException(ordonnanceId));

        

        ordonnance.setStatut(StatutOrdonnance.ARCHIVEE);
        return ordonnanceRepository.save(ordonnance);

        
    }

    public List<Ordonnance> obtenirHistorique(String patientId) {
        return ordonnanceRepository.findByPatientId(patientId).stream()
            .sorted(Comparator.comparing(Ordonnance::getDateEmission).reversed().thenComparing(Ordonnance::getId))
            .toList();
    }

    
}
