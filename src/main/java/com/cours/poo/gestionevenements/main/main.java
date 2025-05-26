package com.cours.poo.gestionevenements.main;


import com.cours.poo.gestionevenements.controller.GestionEvenements;
import com.cours.poo.gestionevenements.exceptions.*;
import com.cours.poo.gestionevenements.model.*;
import com.cours.poo.gestionevenements.services.EmailNotificationService;
import com.cours.poo.gestionevenements.services.NotificationService;
import com.cours.poo.gestionevenements.utils.SerialisationUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class main {
    public static void main(String[] args) {
        System.out.println("Bienvenue au Système de Gestion d'Événements!");

        // Service de notification
        NotificationService emailService = new EmailNotificationService();

        // Création des participants
        Participant part1 = new Participant("P001", "Alice Wonderland", "alice@example.com", emailService);
        Participant part2 = new Participant("P002", "Bob The Builder", "bob@example.com", emailService);
        Organisateur org1 = new Organisateur("ORG001", "Charlie Organizer", "charlie@example.com", emailService);

        // Gestionnaire d'événements (Singleton)
        GestionEvenements manager = GestionEvenements.getInstance();

        // Création d'événements
        List<Intervenant> intervenantsConf = new ArrayList<>();
        intervenantsConf.add(new Intervenant("Dr. KnowItAll", "IA Avancée"));
        intervenantsConf.add(new Intervenant("Mme. CodeWell", "Clean Code"));

        Evenement confJava = new Conference(
                "CONF001", "Conférence Super Java",
                LocalDateTime.of(2025, 10, 15, 9, 0),
                "Grand Amphi", 100,
                "Nouveautés Java 25", intervenantsConf
        );

        Evenement concertRock = new Concert(
                "CON001", "Rock Night Fest",
                LocalDateTime.of(2025, 11, 20, 20, 0),
                "Stadium Central", 500,
                "The Rockers", "Rock Progressif"
        );

        Evenement concertPop = new Concert(
                "CON002", "Pop Sensation",
                LocalDateTime.of(2025, 12, 5, 18, 0),
                "Arena Pop", 2, // Petite capacité pour tester CapaciteMaxAtteinteException
                "Miss Melody", "Pop"
        );

        // Ajout des événements au gestionnaire
        try {
            manager.ajouterEvenement(confJava); //
            manager.ajouterEvenement(concertRock);
            manager.ajouterEvenement(concertPop);
            org1.ajouterEvenementOrganise(confJava); // L'organisateur gère cet événement
        } catch (EvenementDejaExistantException e) { //
            System.err.println("Erreur ajout événement: " + e.getMessage());
        }

        // Inscription des participants aux événements
        System.out.println("\n--- Inscriptions ---");
        try {
            confJava.ajouterParticipant(part1);
            confJava.ajouterParticipant(part2);
            // part1 devient observateur de confJava

            concertRock.ajouterParticipant(part1);
            // part1 devient observateur de concertRock

            concertPop.ajouterParticipant(part1);
            concertPop.ajouterParticipant(part2);
            System.out.println(part1.getNom() + " tente de s'inscrire à nouveau à " + concertPop.getNomEvent());
            concertPop.ajouterParticipant(part1); // Tentative de réinscription

        } catch (CapaciteMaxAtteinteException | ParticipantDejaInscritException e) { //
            System.err.println("Erreur inscription: " + e.getMessage());
        }

        // Tenter de dépasser la capacité
        System.out.println("\n--- Test Capacité Max ---");
        Participant part3 = new Participant("P003", "Carol Attendee", "carol@example.com", emailService);
        try {
            System.out.println(part3.getNom() + " tente de s'inscrire à " + concertPop.getNomEvent() + " (capacité: " + concertPop.getCapaciteMax() + ")");
            concertPop.ajouterParticipant(part3);
        } catch (CapaciteMaxAtteinteException e) { //
            System.err.println("Erreur inscription pour " + part3.getNom() + ": " + e.getMessage());
        } catch (ParticipantDejaInscritException e) {
            System.err.println("Erreur inscription pour " + part3.getNom() + ": " + e.getMessage());
        }


        // Affichage des détails des événements
        System.out.println("\n--- Détails des Événements ---");
        manager.listerTousLesEvenements().forEach(event -> {
            System.out.println(event.afficherDetails()); //
            System.out.println("-----");
        });

        // Modification d'un événement (doit notifier les inscrits)
        System.out.println("\n--- Modification Événement ---");
        if (confJava instanceof Conference) {
            ((Conference) confJava).setThemeConference("Java Avancé et Patterns");
            // Alice (part1) et Bob (part2) devraient être notifiés
        }

        // Annulation d'un événement (doit notifier les inscrits)
        System.out.println("\n--- Annulation Événement ---");
        concertRock.annuler(); // Alice (part1) devrait être notifiée

        // Désinscription
        System.out.println("\n--- Désinscription ---");
        boolean desinscrit = confJava.desinscrireParticipant(part2);
        System.out.println("Désinscription de " + part2.getNom() + ": " + (desinscrit ? "Réussie" : "Échouée"));
        // Si confJava est modifié à nouveau, part2 ne sera plus notifié

        // Recherche d'événement
        System.out.println("\n--- Recherche Événement ---");
        Optional<Evenement> evtTrouve = manager.rechercherEvenement("CONF001");
        evtTrouve.ifPresentOrElse(
                e -> System.out.println("Trouvé: " + e.getNomEvent()),
                () -> System.out.println("Événement CONF001 non trouvé.")
        );

        Optional<Evenement> evtNonExistant = manager.rechercherEvenement("XYZ789");
        if (evtNonExistant.isEmpty()) {
            System.out.println("Événement XYZ789 non trouvé, comme attendu.");
        }

        // Utilisation de Streams et Lambdas pour recherche avancée
        System.out.println("\n--- Recherche Avancée (Streams/Lambdas) ---");
        List<Evenement> concerts = manager.listerTousLesEvenements().stream()
                .filter(e -> e instanceof Concert)
                .toList();
        System.out.println("Nombre de concerts: " + concerts.size());
        concerts.forEach(c -> System.out.println("- " + c.getNomEvent()));

        // Bonus: Notification Asynchrone
        System.out.println("\n--- Test Notification Asynchrone ---");
        if (emailService instanceof EmailNotificationService) {
            EmailNotificationService asyncService = (EmailNotificationService) emailService;
            CompletableFuture<Void> futureNotif = asyncService.envoyerNotificationAsync(
                    "Rappel spécial pour l'événement " + confJava.getNomEvent(), 2000 // 2 secondes de délai
            );
            System.out.println("Notification asynchrone envoyée (traitement en arrière-plan)...");

            futureNotif.join(); // Attend la fin pour la démo, sinon le programme pourrait se terminer avant
            System.out.println("Traitement de la notification asynchrone terminé.");
        }

        // Sérialisation / Désérialisation (simulation)
        System.out.println("\n--- Test Sérialisation/Désérialisation ---");
        try {
            SerialisationUtil.sauvegarderEvenementsJSON(manager.listerTousLesEvenements(), "evenements_data.json");
            List<Evenement> evenementsChargesJSON = SerialisationUtil.chargerEvenementsJSON("evenements_data.json");
            System.out.println("Nombre d'événements chargés depuis JSON (simulation): " + evenementsChargesJSON.size());

            SerialisationUtil.sauvegarderEvenementsXML(manager.listerTousLesEvenements(), "evenements_data.xml");
            List<Evenement> evenementsChargesXML = SerialisationUtil.chargerEvenementsXML("evenements_data.xml");
            System.out.println("Nombre d'événements chargés depuis XML (simulation): " + evenementsChargesXML.size());
        } catch (IOException e) {
            System.err.println("Erreur de sérialisation/désérialisation: " + e.getMessage());
        }

        // Suppression d'un événement du gestionnaire
        System.out.println("\n--- Suppression Événement du Gestionnaire ---");
        try {
            manager.supprimerEvenement("CON002"); //
        } catch (EvenementNonTrouveException e) {
            System.err.println("Erreur suppression: " + e.getMessage());
        }
        System.out.println("Nombre d'événements restants: " + manager.listerTousLesEvenements().size());


        System.out.println("\nFin de la démonstration.");
    }
}
