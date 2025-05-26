package com.gestionevents.gui.controllers;


import com.cours.poo.gestionevenements.controller.GestionEvenements;
import com.cours.poo.gestionevenements.exceptions.EvenementDejaExistantException;
import com.cours.poo.gestionevenements.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EventEditDialogController {

    @FXML private ChoiceBox<String> typeChoiceBox;
    @FXML private TextField idField;
    @FXML private TextField nomField;
    @FXML private DatePicker datePicker;
    @FXML private TextField heureField; // Pour HH:MM
    @FXML private TextField lieuField;
    @FXML private Spinner<Integer> capaciteSpinner;

    // Champs spécifiques
    @FXML private Label specificLabel1;
    @FXML private TextField specificField1; // Pour Thème (Conf) ou Artiste (Concert)
    @FXML private Label specificLabel2;
    @FXML private TextArea specificTextArea; // Pour Intervenants (Conf) ou Genre (Concert)

    @FXML private Button okButton;

    private Stage dialogStage;
    private Evenement evenement; // L'événement en cours d'édition ou de création
    private boolean okClicked = false;
    private GestionEvenements gestionEvenements;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        gestionEvenements = GestionEvenements.getInstance();
        typeChoiceBox.setItems(FXCollections.observableArrayList("Conférence", "Concert")); //
        typeChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateSpecificFields(newValue)
        );
        // Valeur par défaut ou premier élément
        typeChoiceBox.getSelectionModel().selectFirst();

        // Convertisseur pour Spinner si besoin, mais il gère bien les entiers par défaut.
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEvenement(Evenement evenement) {
        this.evenement = evenement;

        if (evenement != null) { // Mode édition
            idField.setText(evenement.getIdEvent()); //
            idField.setEditable(false); // L'ID n'est pas modifiable
            nomField.setText(evenement.getNomEvent()); //
            datePicker.setValue(evenement.getDateHeure().toLocalDate()); //
            heureField.setText(evenement.getDateHeure().toLocalTime().format(timeFormatter)); //
            lieuField.setText(evenement.getLieuEvent()); //
            capaciteSpinner.getValueFactory().setValue(evenement.getCapaciteMax()); //

            if (evenement instanceof Conference) { //
                typeChoiceBox.setValue("Conférence"); //
                Conference conf = (Conference) evenement;
                specificField1.setText(conf.getThemeConference()); //
                String intervenantsStr = conf.getListeIntervenants().stream() //
                        .map(i -> i.getNomIntervenant() + " (" + i.getSpecialite() + ")")
                        .collect(Collectors.joining("\n"));
                specificTextArea.setText(intervenantsStr); //
            } else if (evenement instanceof Concert) { //
                typeChoiceBox.setValue("Concert"); //
                Concert conc = (Concert) evenement;
                specificField1.setText(conc.getArtistePrincipal()); //
                specificTextArea.setText(conc.getGenreMusicalConcert()); //
                specificTextArea.setPrefRowCount(1); // Ajuster pour une seule ligne
            }
        } else { // Mode création
            idField.setEditable(true);
            this.evenement = null; // Assurer qu'on est bien en mode création
        }
        updateSpecificFields(typeChoiceBox.getValue());
    }

    private void updateSpecificFields(String type) {
        if ("Conférence".equals(type)) { //
            specificLabel1.setText("Thème:"); //
            specificField1.setPromptText("Thème de la conférence"); //
            specificLabel2.setText("Intervenants:"); //
            specificTextArea.setPromptText("Nom (Spécialité) - un par ligne"); //
            specificTextArea.setPrefRowCount(3); // Plusieurs lignes pour les intervenants
            specificTextArea.setVisible(true);
            specificLabel2.setVisible(true);
        } else if ("Concert".equals(type)) { //
            specificLabel1.setText("Artiste:"); //
            specificField1.setPromptText("Nom de l'artiste"); //
            specificLabel2.setText("Genre Musical:"); //
            specificTextArea.setPromptText("Genre du concert"); //
            specificTextArea.setPrefRowCount(1); // Une seule ligne pour le genre
            specificTextArea.setVisible(true); // J'ai changé cela pour que ce soit visible
            specificLabel2.setVisible(true);  // J'ai changé cela pour que ce soit visible
        } else {
            specificLabel1.setText("");
            specificField1.setPromptText("");
            specificLabel2.setText("");
            specificTextArea.setPromptText("");
            specificTextArea.setVisible(false);
            specificLabel2.setVisible(false);
        }
        specificField1.setText(specificField1.getText()); // Forcer le rafraîchissement si le texte était déjà là
        specificTextArea.setText(specificTextArea.getText());
    }


    public boolean isOkClicked() {
        return okClicked;
    }

    public Evenement getEvenement() {
        return evenement;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            boolean isCreation = (this.evenement == null);
            String id = idField.getText(); //
            // En mode édition, on récupère l'événement existant pour le modifier
            // En mode création, on instancie un nouvel événement

            if (isCreation) {
                // Vérifier si l'ID existe déjà pour la création
                if (gestionEvenements.rechercherEvenement(id).isPresent()) { //
                    showAlert(Alert.AlertType.ERROR, "Erreur d'ID", "Un événement avec l'ID '" + id + "' existe déjà."); //
                    return;
                }
            }

            String nom = nomField.getText(); //
            LocalDate date = datePicker.getValue();
            LocalTime time;
            try {
                time = LocalTime.parse(heureField.getText(), timeFormatter);
            } catch (DateTimeParseException e) {
                showAlert(Alert.AlertType.ERROR, "Format Heure Invalide", "Veuillez entrer l'heure au format HH:MM.");
                return;
            }
            LocalDateTime dateTime = LocalDateTime.of(date, time); //
            String lieu = lieuField.getText(); //
            int capacite = capaciteSpinner.getValue(); //

            String selectedType = typeChoiceBox.getValue();

            try {
                if (isCreation) { // Création
                    if ("Conférence".equals(selectedType)) { //
                        String theme = specificField1.getText(); //
                        List<Intervenant> intervenants = parseIntervenants(specificTextArea.getText()); //
                        this.evenement = new Conference(id, nom, dateTime, lieu, capacite, theme, intervenants); //
                    } else if ("Concert".equals(selectedType)) { //
                        String artiste = specificField1.getText(); //
                        String genre = specificTextArea.getText(); //
                        this.evenement = new Concert(id, nom, dateTime, lieu, capacite, artiste, genre); //
                    }
                    gestionEvenements.ajouterEvenement(this.evenement); //
                } else { // Édition
                    // Mettre à jour les propriétés de l'objet this.evenement existant
                    this.evenement.setNomEvent(nom); //
                    this.evenement.setDateHeure(dateTime); //
                    this.evenement.setLieuEvent(lieu); //
                    this.evenement.setCapaciteMax(capacite); //

                    if (this.evenement instanceof Conference && "Conférence".equals(selectedType)) { //
                        ((Conference) this.evenement).setThemeConference(specificField1.getText()); //
                        ((Conference) this.evenement).setListeIntervenants(parseIntervenants(specificTextArea.getText())); //
                    } else if (this.evenement instanceof Concert && "Concert".equals(selectedType)) { //
                        ((Concert) this.evenement).setArtistePrincipal(specificField1.getText()); //
                        ((Concert) this.evenement).setGenreMusicalConcert(specificTextArea.getText()); //
                    } else {
                        // Cas où le type a été changé pendant l'édition - plus complexe.
                        // Pour ce TP, on pourrait interdire le changement de type ou recréer l'objet.
                        // Pour l'instant, on suppose que le type ne change pas drastiquement ou on gère par les setters.
                        showAlert(Alert.AlertType.WARNING, "Modification Type", "Le changement de type d'événement en édition a des limitations.");
                    }
                    // Pas besoin de re-ajouter à gestionEvenements, car l'objet est déjà dans la map et on l'a modifié.
                    // Mais il faut notifier les observateurs des changements, ce que les setters font déjà.
                }
                okClicked = true;
                dialogStage.close();

            } catch (EvenementDejaExistantException e) { //
                showAlert(Alert.AlertType.ERROR, "Erreur Création", e.getMessage());
            }

        }
    }

    private List<Intervenant> parseIntervenants(String text) { //
        if (text == null || text.trim().isEmpty()) return new ArrayList<>();
        return Arrays.stream(text.split("\n"))
                .map(line -> {
                    String[] parts = line.split("\\(");
                    String nom = parts[0].trim();
                    String specialite = (parts.length > 1) ? parts[1].replace(")", "").trim() : "N/A";
                    return new Intervenant(nom, specialite);
                })
                .collect(Collectors.toList());
    }


    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (idField.isEditable() && (idField.getText() == null || idField.getText().trim().isEmpty())) { //
            errorMessage += "ID de l'événement non valide !\n";
        }
        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) { //
            errorMessage += "Nom de l'événement non valide !\n";
        }
        if (datePicker.getValue() == null) {
            errorMessage += "Date non valide !\n";
        }
        if (heureField.getText() == null || heureField.getText().trim().isEmpty()) { //
            errorMessage += "Heure non valide !\n";
        } else {
            try {
                LocalTime.parse(heureField.getText(), timeFormatter);
            } catch (DateTimeParseException e) {
                errorMessage += "Format d'heure invalide (HH:MM attendu) !\n";
            }
        }
        if (lieuField.getText() == null || lieuField.getText().trim().isEmpty()) { //
            errorMessage += "Lieu non valide !\n";
        }
        if (capaciteSpinner.getValue() <= 0) { //
            errorMessage += "Capacité doit être positive !\n";
        }

        // Validations spécifiques
        String type = typeChoiceBox.getValue();
        if ("Conférence".equals(type)) { //
            if (specificField1.getText() == null || specificField1.getText().trim().isEmpty()) { //
                errorMessage += "Thème de la conférence non valide !\n"; //
            }
            // Intervenants optionnels
        } else if ("Concert".equals(type)) { //
            if (specificField1.getText() == null || specificField1.getText().trim().isEmpty()) { //
                errorMessage += "Artiste du concert non valide !\n"; //
            }
            if (specificTextArea.getText() == null || specificTextArea.getText().trim().isEmpty()) { //
                errorMessage += "Genre musical non valide !\n"; //
            }
        }


        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Champs Invalides", errorMessage);
            return false;
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.initOwner(dialogStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
