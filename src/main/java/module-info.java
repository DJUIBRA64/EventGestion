module com.cours.poo.gestionevenements { // Le nom de votre module
    // Dépendances pour JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Dépendances pour Jackson (JSON)
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310; // Pour LocalDateTime etc.
    requires com.fasterxml.jackson.annotation;     // Pour les annotations Jackson

    // Exporter le package de l'application GUI pour que JavaFX puisse le lancer
    exports com.gestionevents.gui;

    // Ouvrir les packages aux modules spécifiques pour la réflexion
    // Nécessaire pour que FXML puisse accéder aux contrôleurs et à leurs champs @FXML
    opens com.gestionevents.gui.controllers to javafx.fxml;

    // Nécessaire pour que Jackson puisse (dé)sérialiser vos objets modèles
    // et pour que JavaFX (PropertyValueFactory) puisse accéder aux propriétés des modèles
    opens com.cours.poo.gestionevenements.model to com.fasterxml.jackson.databind, javafx.base;

    // Exporter les autres packages si leurs classes publiques doivent être accessibles
    // par d'autres modules (moins critique pour ce problème spécifique, mais bonne pratique)
    exports com.cours.poo.gestionevenements.controller;
    exports com.cours.poo.gestionevenements.exceptions;
    exports com.cours.poo.gestionevenements.observer;
    exports com.cours.poo.gestionevenements.services;
    exports com.cours.poo.gestionevenements.utils;
    exports com.cours.poo.gestionevenements.model ;
}