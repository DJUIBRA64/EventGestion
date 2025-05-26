package com.cours.poo.gestionevenements.model;


import com.cours.poo.gestionevenements.observer.ParticipantObserver; // [cite: 4]
import com.cours.poo.gestionevenements.services.NotificationService; // [cite: 3]
import java.util.Objects;

// Représente un participant à un événement
public class Participant implements ParticipantObserver { // Implémente ParticipantObserver [cite: 4]
    protected String idParticipant; // ID unique du participant [cite: 1]
    protected String nomParticipant; // Nom du participant [cite: 1]
    protected String emailParticipant; // Email du participant [cite: 1]
    private NotificationService serviceDeNotification; // Pour envoyer des notifications

    public Participant(String id, String nom, String email, NotificationService notificationService) {
        this.idParticipant = id;
        this.nomParticipant = nom;
        this.emailParticipant = email;
        this.serviceDeNotification = notificationService;
    }

    // Getters
    public String getId() { return idParticipant; }
    public String getNom() { return nomParticipant; }
    public String getEmail() { return emailParticipant; }

    // Setters (si modifiables)
    public void setNom(String nom) { this.nomParticipant = nom; }
    public void setEmail(String email) { this.emailParticipant = email; }

    @Override
    public void recevoirNotification(String message) {
        // Logique pour que le participant reçoive une notification
        // Par exemple, via le NotificationService
        if (serviceDeNotification != null) {
            serviceDeNotification.envoyerNotification("À " + nomParticipant + " (" + emailParticipant + "): " + message);
        } else {
            System.out.println("Notification pour " + nomParticipant + ": " + message + " (Service de notification non configuré)");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        return Objects.equals(idParticipant, that.idParticipant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idParticipant);
    }

    @Override
    public String toString() {
        return "Participant [ID=" + idParticipant + ", Nom=" + nomParticipant + ", Email=" + emailParticipant + "]";
    }
}