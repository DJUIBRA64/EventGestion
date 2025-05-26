package com.gestionevents.gui;


import com.cours.poo.gestionevenements.controller.GestionEvenements;
import com.cours.poo.gestionevenements.model.Evenement;
import com.cours.poo.gestionevenements.utils.SerialisationUtil;
import com.gestionevents.gui.controllers.MainViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MainGuiApp extends Application {

    private static final String DATA_FILE = "evenements_data.json"; //
    private GestionEvenements gestionEvenements;

    @Override
    public void init() throws Exception {
        // Charger les données au démarrage de l'application
        gestionEvenements = GestionEvenements.getInstance();
        try {
            List<Evenement> charges = SerialisationUtil.chargerEvenementsJSON(DATA_FILE); //
            // Il faut s'assurer que le singleton GestionEvenements est peuplé avec ces données.
            // La version actuelle de getInstance() ne permet pas de réinjecter une map,
            // donc on va ajouter les événements un par un (ou modifier getInstance pour accepter une map initiale).
            // Pour la simplicité du TP, si le singleton est déjà peuplé, on ne fait rien,
            // sinon on charge. Dans une vraie app, la gestion serait plus robuste.
            if (gestionEvenements.listerTousLesEvenements().isEmpty()) {
                for (Evenement evt : charges) {
                    try {
                        gestionEvenements.ajouterEvenement(evt); //
                    } catch (Exception e) { // Exception générique pour couvrir EvenementDejaExistantException
                        System.err.println("Erreur à l'ajout de l'événement pré-chargé " + evt.getIdEvent() + ": " + e.getMessage());
                    }
                }
            }
            System.out.println(gestionEvenements.listerTousLesEvenements().size() + " événements dans le gestionnaire après chargement initial.");
        } catch (IOException e) {
            System.err.println("Impossible de charger les données au démarrage: " + e.getMessage());
            // Continuer avec une liste vide
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();

        MainViewController controller = loader.getController();
        controller.setPrimaryStage(primaryStage); // Pour gérer les dialogues modaux
        // controller.setGestionEvenements(GestionEvenements.getInstance()); // Déjà géré par getInstance()

        primaryStage.setTitle("Système de Gestion d'Événements");
        Scene scene = new Scene(root, 900, 600) ;
        primaryStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.show();

        // Sauvegarder les données à la fermeture de l'application
        primaryStage.setOnCloseRequest(event -> {
            try {
                SerialisationUtil.sauvegarderEvenementsJSON(gestionEvenements.listerTousLesEvenements(), DATA_FILE); //
            } catch (IOException e) {
                System.err.println("Impossible de sauvegarder les données à la fermeture: " + e.getMessage());
                // Peut-être afficher une alerte à l'utilisateur
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
