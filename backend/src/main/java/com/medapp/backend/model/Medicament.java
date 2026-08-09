package com.medapp.backend.model;


public class Medicament {
    private String nom ;
    private String dosage;
    private String frequence;
    private String duree;

    public Medicament(){}

    public Medicament(String nom , String dosage , String frequence , String duree){
        this.nom = nom;
        this.dosage = dosage;
        this.frequence = frequence;
        this.duree = duree;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getFrequence() { return frequence; }
    public void setFrequence(String frequence) { this.frequence = frequence; }

    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }
    
}
