package org.example.ProgettoUIDFinal;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;

public class SettingsController {

    @FXML private ChoiceBox<String> choiceBox;
    @FXML private Button BackButton; // Assicurati che l'fx:id sia BackButton nel FXML

    private Scene homeScene;

    public void initialize() {
        choiceBox.getItems().addAll("Piccolo", "Medio", "Grande");
        // Opzionale: Seleziona il valore di default attuale
        choiceBox.setValue("Medio");
    }

    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }

    @FXML
    private void choiceChanged() {
        String selected = choiceBox.getValue();
        if (selected == null) return;

        String fontSizeStyle = "";

        // Tieni SOLO UNO switch, non due diversi!
        switch (selected) {
            case "Piccolo":
                fontSizeStyle = "-fx-font-size: 12px;";
                break;
            case "Medio":
                fontSizeStyle = "-fx-font-size: 14px;";
                break;
            case "Grande":
                fontSizeStyle = "-fx-font-size: 18px;";
                break;
            default:
                return;
        }

        // 1. Salva la preferenza nel Manager (così rimane per dopo)
        StyleManager.getInstance().setFontSize(fontSizeStyle);

        // 2. Applica subito alla scena corrente (Settings) per vedere l'effetto live
        if (choiceBox.getScene() != null) {
            StyleManager.getInstance().applyStyle(choiceBox.getScene());
        }

        System.out.println("Dimensione cambiata in: " + selected);
    }

    @FXML
    public void Home() {
        if (homeScene != null) {
            // --- LA SOLUZIONE È QUI ---
            // Prima di mostrare la Home, applichiamo il nuovo stile salvato!
            StyleManager.getInstance().applyStyle(homeScene);

            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        }
    }
}