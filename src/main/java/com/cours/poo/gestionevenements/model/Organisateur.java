package com.cours.poo.gestionevenements.model;


import com.cours.poo.gestionevenements.services.NotificationService; // [cite: 3]
import java.util.ArrayList;
import java.util.List;

// Représente un organisateur d'événements, hérite de Participant [cite: 3]
public class Organisateur extends Participant {
    private List<Evenement> evenementsOrganises; // Liste des événements organisés par cet organisateur [cite: 3]

    public Organisateur(String id, String nom, String email, NotificationService notificationService) {
        super(id, nom, email, notificationService);
        this.evenementsOrganises = new ArrayList<>();
    }

    public List<Evenement> getEvenementsOrganises() {
        return new ArrayList<>(evenementsOrganises); // Retourne une copie
    }

    /**
     * Ajoute un événement à la liste des événements organisés.
     *
     * @param event L'événement à ajouter.
     */
    public void ajouterEvenementOrganise(Evenement event) {
        if (event != null && !this.evenementsOrganises.contains(event)) {
            this.evenementsOrganises.add(event);
            System.out.println("L'événement '" + event.getNomEvent() + "' a été ajouté aux événements organisés par " + this.nomParticipant);
        }
    }

    /**
     * Retire un événement de la liste des événements organisés.
     *
     * @param event L'événement à retirer.
     */
    public boolean retirerEvenementOrganise(Evenement event) {
        boolean removed = this.evenementsOrganises.remove(event);
        if (removed) {
            System.out.println("L'événement '" + event.getNomEvent() + "' a été retiré des événements organisés par " + this.nomParticipant);
        }
        return removed;
    }

    @Override
    public String toString() {
        return "Organisateur [ID=" + idParticipant + ", Nom=" + nomParticipant + ", Email=" + emailParticipant +
                ", Événements Organisés=" + evenementsOrganises.size() + "]";
    }

}