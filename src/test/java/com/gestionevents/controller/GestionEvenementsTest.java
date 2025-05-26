package com.gestionevents.controller;

import com.cours.poo.gestionevenements.controller.GestionEvenements;
import com.cours.poo.gestionevenements.model.*;
import com.cours.poo.gestionevenements.exceptions.*;
import com.cours.poo.gestionevenements.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // Pour mocker NotificationService

import java.time.LocalDateTime;
import java.util.ArrayList;

public class GestionEvenementsTest {

    private GestionEvenements gestionEvenements;
    private Evenement evenement1;
    private Evenement evenement2;
    private NotificationService mockNotificationService;


    @BeforeEach
    void setUp() {
        // Réinitialiser le singleton pour l'isolation des tests (nécessite une méthode de reset ou réflexion)
        // Pour la simplicité, on suppose que le singleton est frais ou on le gère.
        // Une meilleure approche serait d'injecter les dépendances plutôt que d'utiliser un Singleton pur pour la testabilité.
        // Ou avoir une méthode reset pour le test: GestionEvenements.resetInstance();
        gestionEvenements = GestionEvenements.getInstance(); // Ceci peut poser problème si l'état persiste entre les tests.
        // Pour un test propre, il faudrait une instance fraîche ou une méthode de reset.
        // Dans ce cas, on va vider les événements manuellement pour simuler un reset.
        gestionEvenements.listerTousLesEvenements().forEach(evt -> {
            try {
                gestionEvenements.supprimerEvenement(evt.getIdEvent());
            } catch (EvenementNonTrouveException e) { /* ignore */ }
        });


        mockNotificationService = mock(NotificationService.class);

        evenement1 = new Concert("C001", "Concert Test", LocalDateTime.now().plusDays(10), "Lieu Test 1", 100, "Artiste Test", "Genre Test");
        evenement2 = new Conference("F001", "Conférence Test", LocalDateTime.now().plusDays(20), "Lieu Test 2", 50, "Thème Test", new ArrayList<>());
    }

    @Test
    void testAjouterEvenement() throws EvenementDejaExistantException {
        gestionEvenements.ajouterEvenement(evenement1);
        assertNotNull(gestionEvenements.rechercherEvenement("C001").orElse(null));
        assertEquals(1, gestionEvenements.listerTousLesEvenements().size());
    }

    @Test
    void testAjouterEvenementExistant() throws EvenementDejaExistantException {
        gestionEvenements.ajouterEvenement(evenement1);
        assertThrows(EvenementDejaExistantException.class, () -> { // [cite: 4, 7]
            gestionEvenements.ajouterEvenement(evenement1);
        });
    }

    @Test
    void testSupprimerEvenement() throws EvenementDejaExistantException, EvenementNonTrouveException {
        gestionEvenements.ajouterEvenement(evenement1);
        gestionEvenements.supprimerEvenement("C001");
        assertTrue(gestionEvenements.rechercherEvenement("C001").isEmpty());
        assertEquals(0, gestionEvenements.listerTousLesEvenements().size());
    }

    @Test
    void testSupprimerEvenementNonExistant() {
        assertThrows(EvenementNonTrouveException.class, () -> { // [cite: 7]
            gestionEvenements.supprimerEvenement("ID_INEXISTANT");
        });
    }

    @Test
    void testRechercherEvenement() throws EvenementDejaExistantException {
        gestionEvenements.ajouterEvenement(evenement1);
        assertTrue(gestionEvenements.rechercherEvenement("C001").isPresent());
        assertEquals(evenement1, gestionEvenements.rechercherEvenement("C001").get());
    }

    @Test
    void testRechercherEvenementNonExistant() {
        assertTrue(gestionEvenements.rechercherEvenement("ID_INEXISTANT").isEmpty());
    }
}