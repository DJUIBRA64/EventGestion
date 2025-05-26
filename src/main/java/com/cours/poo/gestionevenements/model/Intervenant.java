package com.cours.poo.gestionevenements.model;


public class Intervenant {
    private String nomIntervenant;
    private String specialite;

    public Intervenant(String nom, String specialite) {
        this.nomIntervenant = nom;
        this.specialite = specialite;
    }

    public String getNomIntervenant() {
        return nomIntervenant;
    }

    public void setNomIntervenant(String nom) {
        this.nomIntervenant = nom;
    }

    public String getSpecialite() {
        return specialite;
    }

    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    @Override
    public String toString() {
        return "Intervenant{" +
                "nom='" + nomIntervenant + '\'' +
                ", specialite='" + specialite + '\'' +
                '}';
    }
}
