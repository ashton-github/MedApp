package com.medapp.backend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="patients")
public class Patient {

    @Id
    private String id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private Sexe sexe;
    private String telephone;
    private String adresse;
    private String numeroSecuriteSociale;
    private List<String> antecedents;
    private String medecinReferent;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;

    public Patient(){}

    public Patient(String nom , String prenom , LocalDate dateNaissance ,
        Sexe sexe , String telephone , String adresse , String numeroSecuriteSociale ,
        List<String> antecedants , String medecinReferent , LocalDateTime dateCreation ,
        LocalDateTime dateMiseAJour) {
            this.nom = nom;
            this.prenom = prenom;
            this.dateNaissance = dateNaissance;
            this.sexe = sexe;
            this.telephone = telephone;
            this.adresse = adresse;
            this.numeroSecuriteSociale = numeroSecuriteSociale;
            this.antecedents = antecedants;
            this.medecinReferent = medecinReferent;
            this.dateCreation = dateCreation;
            this.dateMiseAJour = dateMiseAJour;
    }

    public String getId(){
        return id;
    }

    public void  setId(String id){
        this.id = id;
    }

    public String getNom(){
        return nom;
    }

    public void  setNom(String nom){
        this.nom = nom;
    }

     public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Sexe getSexe() {
        return sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNumeroSecuriteSociale() {
        return numeroSecuriteSociale;
    }

    public void setNumeroSecuriteSociale(String numeroSecuriteSociale) {
        this.numeroSecuriteSociale = numeroSecuriteSociale;
    }

    public List<String> getAntecedents() {
        return antecedents;
    }

    public void setAntecedents(List<String> antecedents) {
        this.antecedents = antecedents;
    }

    public String getMedecinReferent() {
        return medecinReferent;
    }

    public void setMedecinReferent(String medecinReferent) {
        this.medecinReferent = medecinReferent;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateMiseAJour() {
        return dateMiseAJour;
    }

    public void setDateMiseAJour(LocalDateTime dateMiseAJour) {
        this.dateMiseAJour = dateMiseAJour;
    }
    
}
