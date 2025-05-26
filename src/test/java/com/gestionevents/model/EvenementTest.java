package com.gestionevents.model;

import com.cours.poo.gestionevenements.model.*;
import com.cours.poo.gestionevenements.exceptions.CapaciteMaxAtteinteException;
import com.cours.poo.gestionevenements.exceptions.ParticipantDejaInscritException;
import com.cours.poo.gestionevenements.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EvenementTest {

    private Concert concertTest;
    private Participant participant1;
    private Participant participant2;
    private NotificationService mockNotificationService;

    @BeforeEach
    void setUp() {
        mockNotificationService = mock(NotificationService.class);
        concertTest = new Concert("CON001", "Test Concert", LocalDateTime.now().plusMonths(1), "Test Venue", 2, "Test Artist", "Test Genre");
        participant1 = new Participant("P001", "Alice", "alice@test.com", mockNotificationService);
        participant2 = new Participant("P002", "Bob", "bob@test.com", mockNotificationService);
    }

    @Test
    void testAjouterParticipant() throws CapaciteMaxAtteinteException, ParticipantDejaInscritException { // [cite: 6]
        concertTest.ajouterParticipant(participant1);
        assertEquals(1, concertTest.getParticipantsInscrits().size());
        assertTrue(concertTest.getParticipantsInscrits().contains(participant1));
    }

    @Test
    void testAjouterParticipantDejaInscrit() throws CapaciteMaxAtteinteException, ParticipantDejaInscritException { // [cite: 6, 7]
        concertTest.ajouterParticipant(participant1);
        assertThrows(ParticipantDejaInscritException.class, () -> {
            concertTest.ajouterParticipant(participant1);
        });
    }

    @Test
    void testCapaciteMaxAtteinte() throws CapaciteMaxAtteinteException, ParticipantDejaInscritException { // [cite: 7]
        Participant participant3 = new Participant("P003", "Charlie", "charlie@test.com", mockNotificationService);
        concertTest.ajouterParticipant(participant1);
        concertTest.ajouterParticipant(participant2); // Capacité = 2, maintenant pleine

        assertThrows(CapaciteMaxAtteinteException.class, () -> {
            concertTest.ajouterParticipant(participant3);
        });
        assertEquals(2, concertTest.getParticipantsInscrits().size());
    }

    @Test
    void testDesinscrireParticipant() throws CapaciteMaxAtteinteException, ParticipantDejaInscritException { // [cite: 6]
        concertTest.ajouterParticipant(participant1);
        assertTrue(concertTest.desinscrireParticipant(participant1));
        assertEquals(0, concertTest.getParticipantsInscrits().size());
        assertFalse(concertTest.getParticipantsInscrits().contains(participant1));
    }

    @Test
    void testDesinscrireParticipantNonInscrit() {
        assertFalse(concertTest.desinscrireParticipant(participant1));
    }

    @Test
    void testAnnulerEvenementNotifieParticipants() throws CapaciteMaxAtteinteException, ParticipantDejaInscritException {
        concertTest.ajouterParticipant(participant1); // participant1 est un ParticipantObserver
        concertTest.ajouterParticipant(participant2);

        concertTest.annuler();

        // Vérifier que recevoirNotification a été appelée sur chaque participant (qui est un ParticipantObserver)
        // Le message exact peut être vérifié si besoin
        String expectedMessage = "L'événement '" + concertTest.getNomEvent() + "' prévu le " + concertTest.getDateHeure() + " à " + concertTest.getLieuEvent() + " a été ANNULÉ.";
        verify(mockNotificationService, times(1)).envoyerNotification("À Alice (alice@test.com): " + expectedMessage);
        verify(mockNotificationService, times(1)).envoyerNotification("À Bob (bob@test.com): " + expectedMessage);
        assertTrue(concertTest.getParticipantsInscrits().isEmpty());
    }

    @Test
    void testModificationEvenementNotifieParticipants() throws CapaciteMaxAtteinteException, ParticipantDejaInscritException {
        concertTest.ajouterParticipant(participant1);
        String nouveauNom = "Super Test Concert";
        concertTest.setNomEvent(nouveauNom); // Cette méthode doit appeler notifierObservateurs

        String expectedMessage = "Le nom de l'événement '" + nouveauNom + "' a été mis à jour.";
        // Ici, on assume que Participant.recevoirNotification utilise son NotificationService
        verify(mockNotificationService, times(1)).envoyerNotification(contains("À Alice (alice@test.com): " + expectedMessage));
    }
}