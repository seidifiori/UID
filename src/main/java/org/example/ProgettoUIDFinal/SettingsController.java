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

        // Recupera la stringa di stile attuale (es: "-fx-font-size: 18px;")
        String currentStyle = StyleManager.getInstance().getFontSize();

        // Controlla quale opzione corrisponde allo stile salvato
        if (currentStyle.contains("12px")) {
            choiceBox.setValue("Piccolo");
        } else if (currentStyle.contains("18px")) {
            choiceBox.setValue("Grande");
        } else {
            // Default o 14px
            choiceBox.setValue("Medio");
        }
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
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");
            StyleManager.getInstance().applyStyle(homeScene);

            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        }
    }
    // IN SettingsController.java

    @FXML
    public void mute() {
        MusicManager.getInstance().toggleMute();
        System.out.println("Musica mutata/smutata");
    }
    @FXML
    public void muteSoundEffects() {
        MusicManager.getInstance().toggleSoundEffects();
        System.out.println("Suoni mutati/smutati");
    }
}