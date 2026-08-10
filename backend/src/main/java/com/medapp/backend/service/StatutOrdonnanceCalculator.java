package com.medapp.backend.service;

import java.time.LocalDate;

import com.medapp.backend.model.StatutOrdonnance;

public class StatutOrdonnanceCalculator {

    private StatutOrdonnanceCalculator(){}

    public static StatutOrdonnance calculer(LocalDate dateValidite , StatutOrdonnance statutActuel){
        if(statutActuel == StatutOrdonnance.ARCHIVEE){
            return StatutOrdonnance.ARCHIVEE;
        }
        if(dateValidite == null){
            throw new IllegalArgumentException("La date de validite est requise pour calculer le statut.");
        }

        return dateValidite.isBefore(LocalDate.now())? StatutOrdonnance.EXPIREE : StatutOrdonnance.ACTIVE;
    }
    
}
