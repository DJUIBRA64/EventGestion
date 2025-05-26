package com.cours.poo.gestionevenements.model;

import java.time.LocalDateTime; // Correction: Doit être LocalDateTime si l'heure est incluse
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.cours.poo.gestionevenements.exceptions.CapaciteMaxAtteinteException;
import com.cours.poo.gestionevenements.exceptions.ParticipantDejaInscritException;
import com.cours.poo.gestionevenements.observer.EvenementObservable; // Pour le pattern Observer
import com.cours.poo.gestionevenements.observer.ParticipantObserver; // Pour le pattern Observer

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Classe abstraite pour un événement
@JsonIgnoreProperties({"observateurs"}) //
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "typeEvenement" // C'est le champ crucial
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Conference.class, name = "conference"), //
        @JsonSubTypes.Type(value = Concert.class, name = "concert") //
})

public abstract class Evenement extends EvenementObservable { // Étend EvenementObservable pour les notifications
    protected String idEvent; // ID unique de l'événement
    protected String nomEvent; // Nom de l'événement
    protected LocalDateTime dateHeure; // Date et heure de l'événement
    protected String lieuEvent; // Lieu de l'événement
    protected int capaciteMax; // Capacité maximale
    protected List<Participant> participantsInscrits; // Liste des participants

    //constructeur pour le fichier Jackson
    public Evenement(){
        this.participantsInscrits = new ArrayList<>();
    }

    //constructeur normal
    public Evenement(String id, String nom, LocalDateTime dateHeure, String lieu, int capaciteMax) {
        this.idEvent = id;
        this.nomEvent = nom;
        this.dateHeure = dateHeure;
        this.lieuEvent = lieu;
        this.capaciteMax = capaciteMax;
        this.participantsInscrits = new ArrayList<>();
    }



    // Getters
    public String getIdEvent() { return idEvent; }
    public String getNomEvent() { return nomEvent; }
    public LocalDateTime getDateHeure() { return dateHeure; }
    public String getLieuEvent() { return lieuEvent; }
    public int getCapaciteMax() { return capaciteMax; }
    public List<Participant> getParticipantsInscrits() { return new ArrayList<>(participantsInscrits); } // Retourne une copie

    // Setters (pour modifications éventuelles)
    public void setNomEvent(String nomEvent) {
        this.nomEvent = nomEvent;
        notifierObservateurs("Le nom de l'événement '" + this.nomEvent + "' a été mis à jour.");
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
        notifierObservateurs("La date/heure de l'événement '" + this.nomEvent + "' a été mise à jour.");
    }

    public void setLieuEvent(String lieuEvent) {
        this.lieuEvent = lieuEvent;
        notifierObservateurs("Le lieu de l'événement '" + this.nomEvent + "' a été mis à jour.");
    }

    public void setCapaciteMax(int capaciteMax) {
        this.capaciteMax = capaciteMax;
        // Potentiellement notifier si la capacité change et affecte les inscriptions
    }


    /**
     * Ajoute un participant à l'événement.
     * @param participant Le participant à ajouter.
     * @throws CapaciteMaxAtteinteException si la capacité maximale est atteinte. [cite: 4]
     * @throws ParticipantDejaInscritException si le participant est déjà inscrit.
     */
    public void ajouterParticipant(Participant participant) throws CapaciteMaxAtteinteException, ParticipantDejaInscritException {
        if (participantsInscrits.size() >= capaciteMax) {
            throw new CapaciteMaxAtteinteException("La capacité maximale pour l'événement '" + nomEvent + "' est atteinte.");
        }
        if (participantsInscrits.contains(participant)) {
            throw new ParticipantDejaInscritException("Le participant " + participant.getNom() + " est déjà inscrit à " + nomEvent);
        }
        participantsInscrits.add(participant);
        // Ajouter le participant comme observateur de cet événement
        if (participant instanceof ParticipantObserver) {
            this.ajouterObservateur((ParticipantObserver) participant);
        }
        System.out.println(participant.getNom() + " a été inscrit à " + this.nomEvent);
    }

    /**
     * Désinscrit un participant de l'événement.
     * @param participant Le participant à désinscrire.
     */
    public boolean desinscrireParticipant(Participant participant) {
        boolean removed = participantsInscrits.remove(participant);
        if (removed) {
            // Retirer le participant des observateurs
            if (participant instanceof ParticipantObserver) {
                this.retirerObservateur((ParticipantObserver) participant);
            }
            System.out.println(participant.getNom() + " a été désinscrit de " + this.nomEvent);
        }
        return removed;
    }

    /**
     * Annule l'événement et notifie les participants. [cite: 1]
     */
    public void annuler() {
        System.out.println("L'événement '" + nomEvent + "' a été annulé.");
        // Notifier tous les participants/observateurs
        notifierObservateurs("L'événement '" + nomEvent + "' prévu le " + dateHeure + " à " + lieuEvent + " a été ANNULÉ.");
        participantsInscrits.clear(); // Vider la liste des inscrits après notification
    }

    // Méthode abstraite pour afficher les détails spécifiques à chaque type d'événement
    public abstract String afficherDetails(); // [cite: 1]

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Evenement evenement = (Evenement) o;
        return Objects.equals(idEvent, evenement.idEvent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEvent);
    }

    @Override
    public String toString() {
        return "Evenement [ID=" + idEvent + ", Nom=" + nomEvent + ", Date=" + dateHeure + ", Lieu=" + lieuEvent + ", Capacité Max=" + capaciteMax + "]";
    }
}
