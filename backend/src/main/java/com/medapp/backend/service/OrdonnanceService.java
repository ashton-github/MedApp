package com.medapp.backend.service;

import org.springframework.stereotype.Service;

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

        ordonnance.setStatut(StatutOrdonnance.ACTIVE);
        return ordonnanceRepository.save(ordonnance);
    }

    
}
