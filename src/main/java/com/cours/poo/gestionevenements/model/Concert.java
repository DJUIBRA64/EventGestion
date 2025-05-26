package com.cours.poo.gestionevenements.model;

import java.time.LocalDateTime;

public class Concert extends Evenement {
    private String artistePrincipal; // Artiste du concert
    private String genreMusicalConcert; // Genre musical

    public Concert() { super(); } // Constructeur par défaut pour Jackson

    public Concert(String id, String nom, LocalDateTime dateHeure, String lieu, int capaciteMax, String artiste, String genreMusical) {
        super(id, nom, dateHeure, lieu, capaciteMax);
        this.artistePrincipal = artiste;
        this.genreMusicalConcert = genreMusical;
    }

    public String getArtistePrincipal() {
        return artistePrincipal;
    }

    public void setArtistePrincipal(String artiste) {
        this.artistePrincipal = artiste;
        notifierObservateurs("L'artiste principal du concert '" + this.nomEvent + "' a été mis à jour : " + artiste);
    }

    public String getGenreMusicalConcert() {
        return genreMusicalConcert;
    }

    public void setGenreMusicalConcert(String genreMusical) {
        this.genreMusicalConcert = genreMusical;
        notifierObservateurs("Le genre musical du concert '" + this.nomEvent + "' a été mis à jour : " + genreMusical);
    }

    @Override
    public String afficherDetails() { //
        return super.toString() + "\n" +
                "Type: Concert\n" +
                "Artiste: " + artistePrincipal + "\n" +
                "Genre Musical: " + genreMusicalConcert + "\n" +
                "Participants inscrits: " + participantsInscrits.size() + "/" + capaciteMax;
    }
}