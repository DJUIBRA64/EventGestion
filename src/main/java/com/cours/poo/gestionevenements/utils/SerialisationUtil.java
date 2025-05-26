package com.cours.poo.gestionevenements.utils;



import com.cours.poo.gestionevenements.model.Evenement;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SerialisationUtil {

    private static ObjectMapper jsonMapper = new ObjectMapper();

    static {
        // Enregistre le module pour gérer Java Time API (LocalDateTime, etc.)
        jsonMapper.registerModule(new JavaTimeModule());
        // Pour un affichage formaté (lisible) du JSON
        jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Pour ne pas échouer sur des propriétés inconnues lors de la désérialisation
        // jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Sauvegarde la liste des événements dans un fichier JSON.
     * @param evenements La liste des événements à sauvegarder.
     * @param nomFichier Le chemin du fichier de sauvegarde.
     * @throws IOException En cas d'erreur d'écriture.
     */
    public static void sauvegarderEvenementsJSON(List<Evenement> evenements, String nomFichier) throws IOException { //
        try {
            jsonMapper.writeValue(new File(nomFichier), evenements);
            System.out.println("Données sauvegardées avec succès dans " + nomFichier);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde JSON: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Charge la liste des événements depuis un fichier JSON.
     * @param nomFichier Le chemin du fichier à charger.
     * @return La liste des événements chargée.
     * @throws IOException En cas d'erreur de lecture ou si le fichier est vide/malformé.
     */
    public static List<Evenement> chargerEvenementsJSON(String nomFichier) throws IOException { //
        File file = new File(nomFichier);
        if (!file.exists() || file.length() == 0) {
            System.out.println("Fichier JSON non trouvé ou vide ("+ nomFichier +"), retourne une liste vide.");
            return new ArrayList<>();
        }
        try {
            // Utilisation de TypeReference pour désérialiser une liste d'objets polymorphiques
            TypeReference<List<Evenement>> typeRef = new TypeReference<List<Evenement>>() {};
            List<Evenement> evenementsCharges = jsonMapper.readValue(file, typeRef);
            System.out.println(evenementsCharges.size() + " événements chargés depuis " + nomFichier);
            return evenementsCharges;
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement JSON: " + e.getMessage());
            throw e;
        }
    }

    // Les méthodes pour XML peuvent être implémentées de manière similaire avec JAXB si besoin.
    // Pour ce TP, nous nous concentrons sur JSON avec Jackson.
    public static void sauvegarderEvenementsXML(List<Evenement> evenements, String nomFichier) throws IOException { //
        System.out.println("DEMO: La sauvegarde XML n'est pas implémentée avec JAXB dans cette version GUI. Utilisez JSON.");
    }

    public static List<Evenement> chargerEvenementsXML(String nomFichier) throws IOException { //
        System.out.println("DEMO: Le chargement XML n'est pas implémenté avec JAXB dans cette version GUI. Utilisez JSON.");
        return new ArrayList<>();
    }
}