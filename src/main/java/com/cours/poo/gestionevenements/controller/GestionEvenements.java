package com.cours.poo.gestionevenements.controller;

import com.cours.poo.gestionevenements.model.Evenement;
import com.cours.poo.gestionevenements.exceptions.EvenementDejaExistantException;
import com.cours.poo.gestionevenements.exceptions.EvenementNonTrouveException; // Exception personnalisée utile

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Gère la collection d'événements, implémenté en Singleton
public class GestionEvenements {
    private static GestionEvenements instance;
    private Map<String, Evenement> evenements; // Map pour stocker les événements par ID

    private GestionEvenements() {
        this.evenements = new HashMap<>();
    }

    // Méthode pour obtenir l'instance unique (Singleton paresseux et thread-safe)
    public static synchronized GestionEvenements getInstance() {
        if (instance == null) {
            instance = new GestionEvenements();
        }
        return instance;
    }

    /**
     * Ajoute un événement au système.
     * @param event L'événement à ajouter.
     * @throws EvenementDejaExistantException si un événement avec le même ID existe déjà.
     */
    public void ajouterEvenement(Evenement event) throws EvenementDejaExistantException {
        if (event == null || event.getIdEvent() == null) {
            throw new IllegalArgumentException("L'événement ou son ID ne peut pas être nul.");
        }
        if (evenements.containsKey(event.getIdEvent())) {
            throw new EvenementDejaExistantException("Un événement avec l'ID '" + event.getIdEvent() + "' existe déjà.");
        }
        evenements.put(event.getIdEvent(), event);
        System.out.println("Événement '" + event.getNomEvent() + "' ajouté au système.");
    }

    /**
     * Supprime un événement du système.
     * @param idEvent L'ID de l'événement à supprimer.
     * @return true si l'événement a été supprimé, false sinon.
     * @throws EvenementNonTrouveException si l'événement n'est pas trouvé.
     */
    public boolean supprimerEvenement(String idEvent) throws EvenementNonTrouveException {
        if (idEvent == null) {
            throw new IllegalArgumentException("L'ID de l'événement ne peut pas être nul.");
        }
        Evenement event = evenements.remove(idEvent);
        if (event == null) {
            throw new EvenementNonTrouveException("Aucun événement trouvé avec l'ID '" + idEvent + "'.");
        }
        // On pourrait aussi annuler l'événement avant de le supprimer pour notifier les participants
        event.annuler(); // Notifie les participants de l'annulation avant suppression du système
        System.out.println("Événement '" + event.getNomEvent() + "' supprimé du système.");
        return true;
    }

    /**
     * Recherche un événement par son ID.
     * @param idEvent L'ID de l'événement à rechercher.
     * @return un Optional contenant l'événement s'il est trouvé, sinon Optional vide.
     */
    public Optional<Evenement> rechercherEvenement(String idEvent) {
        if (idEvent == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(evenements.get(idEvent));
    }

    /**
     * Retourne une liste de tous les événements.
     * @return une liste non modifiable des événements.
     */
    public List<Evenement> listerTousLesEvenements() {
        // Utilisation de Streams pour retourner une nouvelle liste
        return this.evenements.values().stream().collect(Collectors.toUnmodifiableList());
    }

    // Méthodes de recherche supplémentaires (utilisant Streams et Lambdas)
    public List<Evenement> rechercherEvenementsParNom(String nomPartiel) {
        return evenements.values().stream()
                .filter(e -> e.getNomEvent().toLowerCase().contains(nomPartiel.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Evenement> rechercherEvenementsParDate(LocalDateTime date) {
        return evenements.values().stream()
                .filter(e -> e.getDateHeure().toLocalDate().isEqual(date.toLocalDate())) // Compare juste la date, pas l'heure
                .collect(Collectors.toList());
    }
    public void supprimerEvenementSilencieusement(String idEvent) throws EvenementNonTrouveException {
        if (evenements.remove(idEvent) == null) {
            throw new EvenementNonTrouveException("Aucun événement trouvé avec l'ID '" + idEvent + "'.");
        }
    }
}
