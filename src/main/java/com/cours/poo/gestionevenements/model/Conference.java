package com.cours.poo.gestionevenements.model;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Conference extends Evenement {
    private String themeConference; // Thème de la conférence
    private List<Intervenant> listeIntervenants; // Liste des intervenants

    //constructeur par défaut pour le fichier Jackson

    public Conference() { super(); }

    //constructeur normal
    public Conference(String id, String nom, LocalDateTime dateHeure, String lieu, int capaciteMax, String theme, List<Intervenant> intervenants) {
        super(id, nom, dateHeure, lieu, capaciteMax);
        this.themeConference = theme;
        this.listeIntervenants = intervenants;
    }

    public String getThemeConference() {
        return themeConference;
    }

    public void setThemeConference(String theme) {
        this.themeConference = theme;
        // On pourrait notifier d'un changement de thème si c'est pertinent
        notifierObservateurs("Le thème de la conférence '" + this.nomEvent + "' a été mis à jour : " + theme);
    }

    public List<Intervenant> getListeIntervenants() {
        return new ArrayList<>(listeIntervenants); // Retourne une copie
    }

    public void setListeIntervenants(List<Intervenant> intervenants) {
        this.listeIntervenants = intervenants;
        notifierObservateurs("La liste des intervenants pour la conférence '" + this.nomEvent + "' a été mise à jour.");
    }

    @Override
    public String afficherDetails() { //
        String details = super.toString() + "\n" +
                "Type: Conférence\n" +
                "Thème: " + themeConference + "\n" +
                "Intervenants: \n";
        if (listeIntervenants != null && !listeIntervenants.isEmpty()) {
            details += listeIntervenants.stream()
                    .map(intervenant -> "  - " + intervenant.getNomIntervenant() + " (" + intervenant.getSpecialite() + ")")
                    .collect(Collectors.joining("\n"));
        } else {
            details += "  Aucun intervenant spécifié.";
        }
        details += "\nParticipants inscrits: " + participantsInscrits.size() + "/" + capaciteMax;
        return details;
    }
}