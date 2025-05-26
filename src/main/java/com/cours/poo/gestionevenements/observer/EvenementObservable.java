package com.cours.poo.gestionevenements.observer;


import java.util.ArrayList;
import java.util.List;

// Sujet (Observable) dans le pattern Observer
public abstract class EvenementObservable {
    private List<ParticipantObserver> observateurs = new ArrayList<>();

    public void ajouterObservateur(ParticipantObserver observateur) {
        if (!observateurs.contains(observateur)) {
            observateurs.add(observateur);
        }
    }

    public void retirerObservateur(ParticipantObserver observateur) {
        observateurs.remove(observateur);
    }

    public void notifierObservateurs(String message) {
        // Utilisation de Streams et Lambdas pour notifier
        // On crée une copie pour éviter ConcurrentModificationException si un observateur se désinscrit pendant la notification
        new ArrayList<>(observateurs).forEach(observateur -> observateur.recevoirNotification(message));
    }
}
