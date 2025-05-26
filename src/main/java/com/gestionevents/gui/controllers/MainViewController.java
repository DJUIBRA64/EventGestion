package com.gestionevents.gui.controllers;



import com.cours.poo.gestionevenements.controller.GestionEvenements;
import com.cours.poo.gestionevenements.model.Concert ;
import com.cours.poo.gestionevenements.model.Conference ;
import com.cours.poo.gestionevenements.model.Evenement;
import com.cours.poo.gestionevenements.model.Participant ;
import com.cours.poo.gestionevenements.model.Intervenant ;
import com.cours.poo.gestionevenements.exceptions.CapaciteMaxAtteinteException;
import com.cours.poo.gestionevenements.exceptions.EvenementNonTrouveException;
import com.cours.poo.gestionevenements.exceptions.ParticipantDejaInscritException;
import com.cours.poo.gestionevenements.observer.ParticipantObserver;
import com.cours.poo.gestionevenements.utils.SerialisationUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainViewController implements ParticipantObserver {

    @FXML private BorderPane mainBorderPane;
    @FXML private TableView<Evenement> eventTableView;
    @FXML private TableColumn<Evenement, String> idColumn;
    @FXML private TableColumn<Evenement, String> nomColumn;
    @FXML private TableColumn<Evenement, String> dateColumn;
    @FXML private TableColumn<Evenement, String> lieuColumn;
    @FXML private TableColumn<Evenement, String> typeColumn;
    @FXML private TableColumn<Evenement, Integer> capaciteColumn;
    @FXML private TableColumn<Evenement, Integer> inscritsColumn;

    @FXML private Label detailsIdLabel;
    @FXML private Label detailsNomLabel;
    @FXML private Label detailsDateLabel;
    @FXML private Label detailsLieuLabel;
    @FXML private Label detailsCapaciteLabel;
    @FXML private Label detailsTypeSpecificLabel1;
    @FXML private Label detailsTypeSpecificLabel2;
    @FXML private Label detailsInscritsLabel;
    @FXML private Button registerButton;

    @FXML private Label notificationLabel;
    @FXML private Label statusLabel;
    @FXML private TextField searchField;


    private ObservableList<Evenement> masterEventList = FXCollections.observableArrayList();
    private FilteredList<Evenement> filteredEventList;

    private GestionEvenements gestionEvenements;
    private Stage primaryStage;

    // Simuler un participant pour les inscriptions depuis l'UI
    // Dans une vraie app, on aurait une gestion de session utilisateur
    private Participant currentUser;


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Dans com.gestionevents.gui.controllers.MainViewController.java

    @FXML
    public void initialize() {
        gestionEvenements = GestionEvenements.getInstance();
        setupCurrentUser();
        setupTableColumns();



        // Étape 2: Initialiser filteredEventList AVANT toute utilisation
        filteredEventList = new FilteredList<>(masterEventList, p -> true); // p -> true pour tout afficher au début
        eventTableView.setItems(filteredEventList); // Lier la TableView à la FilteredList

        // Étape 3: Charger les données et appliquer le filtre initial (si searchField a déjà du texte)
        loadEventsIntoTable(); // Maintenant, filteredEventList n'est plus null

        // Étape 4: Mettre en place les listeners
        eventTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showEventDetails(newValue));

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applySearchFilter(newValue);
        });

        Platform.runLater(() -> {
            if (statusLabel != null) { // Ajout d'une vérification null pour robustesse
                statusLabel.setText(masterEventList.size() + " événement(s) chargé(s).");
            }
        });
    }

    private void setupCurrentUser() {
        // Pour la démo, on crée un utilisateur "système" qui s'inscrit.
        // Son service de notification est un peu spécial car il doit mettre à jour l'UI
        currentUser = new Participant("USER_GUI_001", "Utilisateur GUI", "gui@example.com",
                message -> { // C'est une implémentation à la volée de NotificationService
                    Platform.runLater(() -> {
                        showNotification("Pour " + currentUser.getNom() + ": " + message);
                    });
                }
        );
    }


    private void setupTableColumns() {
        idColumn = new TableColumn<>("ID"); //
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idEvent")); //

        nomColumn = new TableColumn<>("Nom"); //
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomEvent")); //

        dateColumn = new TableColumn<>("Date et Heure"); //
        dateColumn.setCellValueFactory(cellData -> {
            LocalDateTime dateTime = cellData.getValue().getDateHeure(); //
            return new SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        });

        lieuColumn = new TableColumn<>("Lieu"); //
        lieuColumn.setCellValueFactory(new PropertyValueFactory<>("lieuEvent")); //

        typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() instanceof Conference) return new SimpleStringProperty("Conférence"); //
            if (cellData.getValue() instanceof Concert) return new SimpleStringProperty("Concert"); //
            return new SimpleStringProperty("N/A");
        });

        capaciteColumn = new TableColumn<>("Capacité"); //
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capaciteMax")); //

        inscritsColumn = new TableColumn<>("Inscrits");
        inscritsColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getParticipantsInscrits().size()).asObject());


        eventTableView.getColumns().setAll(idColumn, nomColumn, dateColumn, lieuColumn, typeColumn, capaciteColumn, inscritsColumn);
    }


    private void loadEventsIntoTable() {
        masterEventList.setAll(gestionEvenements.listerTousLesEvenements());
        applySearchFilter(searchField.getText()); // Appliquer le filtre actuel
        statusLabel.setText(masterEventList.size() + " événement(s).");
    }

    private void showEventDetails(Evenement event) {
        if (event != null) {
            detailsIdLabel.setText("ID: " + event.getIdEvent()); //
            detailsNomLabel.setText("Nom: " + event.getNomEvent()); //
            detailsDateLabel.setText("Date: " + event.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))); //
            detailsLieuLabel.setText("Lieu: " + event.getLieuEvent()); //
            detailsCapaciteLabel.setText("Capacité Max: " + event.getCapaciteMax()); //
            detailsInscritsLabel.setText("Participants Inscrits: " + event.getParticipantsInscrits().size() + "/" + event.getCapaciteMax()); //

            if (event instanceof Conference) {
                Conference conf = (Conference) event;
                detailsTypeSpecificLabel1.setText("Thème: " + conf.getThemeConference()); //
                String intervenantsStr = conf.getListeIntervenants().stream() //
                        .map(Intervenant::getNomIntervenant)
                        .collect(Collectors.joining(", "));
                detailsTypeSpecificLabel2.setText("Intervenants: " + (intervenantsStr.isEmpty() ? "N/A" : intervenantsStr)); //
            } else if (event instanceof Concert) {
                Concert conc = (Concert) event;
                detailsTypeSpecificLabel1.setText("Artiste: " + conc.getArtistePrincipal()); //
                detailsTypeSpecificLabel2.setText("Genre Musical: " + conc.getGenreMusicalConcert()); //
            } else {
                detailsTypeSpecificLabel1.setText("");
                detailsTypeSpecificLabel2.setText("");
            }
            registerButton.setDisable(event.getParticipantsInscrits().contains(currentUser) || event.getParticipantsInscrits().size() >= event.getCapaciteMax());
        } else {
            clearDetailsPane();
        }
    }

    private void clearDetailsPane() {
        detailsIdLabel.setText("ID: ");
        detailsNomLabel.setText("Nom: ");
        detailsDateLabel.setText("Date: ");
        detailsLieuLabel.setText("Lieu: ");
        detailsCapaciteLabel.setText("Capacité Max: ");
        detailsTypeSpecificLabel1.setText("");
        detailsTypeSpecificLabel2.setText("");
        detailsInscritsLabel.setText("Inscrits: ");
        registerButton.setDisable(true);
    }

    @FXML
    private void handleNewEvent() {
        showEventEditDialog(null);
    }

    @FXML
    private void handleEditEvent() {
        Evenement selectedEvent = eventTableView.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            showEventEditDialog(selectedEvent);
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucune Sélection", "Veuillez sélectionner un événement à modifier.");
        }
    }

    private void showEventEditDialog(Evenement eventToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EventEditDialog.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(eventToEdit == null ? "Nouvel Événement" : "Modifier Événement");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage); // Ou mainBorderPane.getScene().getWindow()
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            EventEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setEvenement(eventToEdit); // Si null, c'est une création

            dialogStage.showAndWait();

            if (controller.isOkClicked()) {
                loadEventsIntoTable(); // Rafraîchir la table
                Evenement savedEvent = controller.getEvenement();
                if (savedEvent != null) {
                    eventTableView.getSelectionModel().select(savedEvent); // Sélectionner l'élément ajouté/modifié
                    showNotification("Événement " + (eventToEdit == null ? "créé" : "modifié") + ": " + savedEvent.getNomEvent());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'édition: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteEvent() {
        Evenement selectedEvent = eventTableView.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation de Suppression");
            alert.setHeaderText("Supprimer l'événement: " + selectedEvent.getNomEvent() + "?");
            alert.setContentText("Cette action est irréversible. L'événement sera annulé et les participants notifiés.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    gestionEvenements.supprimerEvenement(selectedEvent.getIdEvent()); // supprimerEvenement notifie et annule déjà
                    loadEventsIntoTable();
                    showNotification("Événement '" + selectedEvent.getNomEvent() + "' supprimé et participants notifiés.");
                } catch (EvenementNonTrouveException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de Suppression", e.getMessage());
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucune Sélection", "Veuillez sélectionner un événement à supprimer.");
        }
    }

    @FXML
    private void handleRegisterToEvent() {
        Evenement selectedEvent = eventTableView.getSelectionModel().getSelectedItem();
        if (selectedEvent == null) {
            showAlert(Alert.AlertType.WARNING, "Aucun événement sélectionné", "Veuillez sélectionner un événement pour vous inscrire.");
            return;
        }
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur Utilisateur", "Aucun utilisateur courant défini pour l'inscription.");
            return;
        }

        try {
            selectedEvent.ajouterParticipant(currentUser); //
            // currentUser implémente ParticipantObserver et son service de notif met à jour l'UI
            // La méthode `ajouterParticipant` dans Evenement ajoute aussi le participant comme observateur.
            showNotification("Vous êtes inscrit à : " + selectedEvent.getNomEvent());
            loadEventsIntoTable(); // Rafraichir pour le nombre d'inscrits
            showEventDetails(selectedEvent); // Rafraichir les détails et l'état du bouton
        } catch (CapaciteMaxAtteinteException | ParticipantDejaInscritException e) { //
            showAlert(Alert.AlertType.WARNING, "Inscription Échouée", e.getMessage());
        }
    }

    @FXML
    private void handleSaveData() {
        try {
            SerialisationUtil.sauvegarderEvenementsJSON(gestionEvenements.listerTousLesEvenements(), "evenements_data.json"); //
            showAlert(Alert.AlertType.INFORMATION, "Sauvegarde", "Données sauvegardées avec succès !");
            showNotification("Données sauvegardées.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Sauvegarde", "Impossible de sauvegarder les données: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoadData() {
        try {
            List<Evenement> evenementsCharges = SerialisationUtil.chargerEvenementsJSON("evenements_data.json"); //
            // Vider le gestionnaire actuel et re-peupler
            gestionEvenements.listerTousLesEvenements().forEach(evt -> {
                try {
                    // On ne veut pas re-notifier pour une annulation lors d'un simple rechargement.
                    // Il faudrait une méthode "clearAllEvents" dans GestionEvenements
                    // Pour l'instant, on supprime sans la notification d'annulation massive.
                    // Ceci est un hack. Mieux: GestionEvenements.getInstance().clearAndLoad(evenementsCharges);
                    gestionEvenements.supprimerEvenementSilencieusement(evt.getIdEvent()); // Méthode à ajouter
                } catch (EvenementNonTrouveException e) { /* ignore */ }
            });

            for (Evenement evt : evenementsCharges) {
                try {
                    gestionEvenements.ajouterEvenement(evt); //
                } catch (Exception e) { // EvenementDejaExistantException
                    System.err.println("Chargement: " + e.getMessage());
                }
            }
            loadEventsIntoTable();
            showAlert(Alert.AlertType.INFORMATION, "Chargement", evenementsCharges.size() + " événements chargés.");
            showNotification("Données chargées.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de Chargement", "Impossible de charger les données: " + e.getMessage());
        }
    }


    @FXML
    private void handleSearch() {
        applySearchFilter(searchField.getText());
    }

    private void applySearchFilter(String searchText) {
        String lowerCaseFilter = searchText == null ? "" : searchText.toLowerCase();
        filteredEventList.setPredicate(event -> {
            if (searchText == null || searchText.isEmpty()) {
                return true; // Afficher tout si le champ de recherche est vide
            }
            // Vérifier si le texte de recherche correspond à une propriété de l'événement
            if (event.getNomEvent().toLowerCase().contains(lowerCaseFilter)) return true; //
            if (event.getLieuEvent().toLowerCase().contains(lowerCaseFilter)) return true; //
            if (event.getIdEvent().toLowerCase().contains(lowerCaseFilter)) return true; //
            if (event instanceof Conference && "conférence".contains(lowerCaseFilter)) return true; //
            if (event instanceof Concert && "concert".contains(lowerCaseFilter)) return true; //
            // Ajouter d'autres critères si besoin
            return false;
        });
        statusLabel.setText(filteredEventList.size() + " événement(s) trouvé(s).");
    }


    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À Propos");
        alert.setHeaderText("Système de Gestion d'Événements");
        alert.setContentText("Version 1.0 (GUI)\nDéveloppé pour le TP de POO.");
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Pour mettre à jour la barre de notification
    public void showNotification(String message) {
        Platform.runLater(() -> notificationLabel.setText(message));
    }

    // Implémentation de ParticipantObserver (si MainViewController devait observer qqch directement)
    @Override
    public void recevoirNotification(String message) {
        // Cette méthode serait appelée si MainViewController s'abonnait à un EvenementObservable
        showNotification("Notification reçue: " + message);
    }

}