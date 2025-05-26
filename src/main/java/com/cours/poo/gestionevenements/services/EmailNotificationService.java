package com.cours.poo.gestionevenements.services;


import java.util.concurrent.CompletableFuture; // Pour le bonus

// Implémentation concrète pour l'envoi de notifications par email
public class EmailNotificationService implements NotificationService {

    @Override
    public void envoyerNotification(String message) {
        System.out.println("Notification par Email Envoyée: " + message);
        // Dans une vraie application, ici on utiliserait une API d'email.
    }

    /**
     * Simule l'envoi de notifications en différé. [cite: 6]
     * @param message Le message à envoyer.
     * @param delaiMillis Le délai en millisecondes avant l'envoi.
     */
    public CompletableFuture<Void> envoyerNotificationAsync(String message, long delaiMillis) {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(delaiMillis); // Simule le délai
                System.out.println("Notification Asynchrone par Email (après " + delaiMillis + "ms): " + message);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Envoi de notification asynchrone interrompu: " + e.getMessage());
            }
        });
    }
}