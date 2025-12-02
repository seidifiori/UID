package org.example.ProgettoUIDFinal;

import javafx.application.Platform; // <--- AGGIUNTO QUESTO IMPORT IMPORTANTE
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;

public class SettingsController {

    @FXML private ChoiceBox<String> choiceBox;
    @FXML private Button BackButton;
    // Assicurati di avere un bottone nel FXML con fx:id="QuitButton" (opzionale) e onAction="#exitGame"
    @FXML private Button QuitButton;

    // --- RIFERIMENTI ALLE IMMAGINI ---
    @FXML private ImageView musicCheckmark;
    @FXML private ImageView sfxCheckmark;

    private Scene homeScene;
    private Image checkmarkImage;

    public void initialize() {
        // 1. Setup ChoiceBox
        choiceBox.getItems().addAll("Piccolo", "Medio", "Grande");
        String currentStyle = StyleManager.getInstance().getFontSize();
        if (currentStyle.contains("12px")) choiceBox.setValue("Piccolo");
        else if (currentStyle.contains("18px")) choiceBox.setValue("Grande");
        else choiceBox.setValue("Medio");

        // 2. Carichiamo l'immagine UNA VOLTA
        try {
            URL imgUrl = getClass().getResource("/org/example/ProgettoUIDFinal/imagini/Settings/checkmark.png");
            if (imgUrl != null) {
                checkmarkImage = new Image(imgUrl.toString());
            } else {
                System.err.println("GLaDOS: Impossibile trovare checkmark.png. Patetico.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateCheckmarkUI();
    }

    private void updateCheckmarkUI() {
        if (checkmarkImage == null) return;

        if (!MusicManager.getInstance().isMuted()) {
            musicCheckmark.setImage(checkmarkImage);
        } else {
            musicCheckmark.setImage(null);
        }

        if (!MusicManager.getInstance().SoundEffectisMuted()) {
            sfxCheckmark.setImage(checkmarkImage);
        } else {
            sfxCheckmark.setImage(null);
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
        switch (selected) {
            case "Piccolo": fontSizeStyle = "-fx-font-size: 12px;"; break;
            case "Medio":   fontSizeStyle = "-fx-font-size: 14px;"; break;
            case "Grande":  fontSizeStyle = "-fx-font-size: 18px;"; break;
        }

        StyleManager.getInstance().setFontSize(fontSizeStyle);
        if (choiceBox.getScene() != null) {
            StyleManager.getInstance().applyStyle(choiceBox.getScene());
        }
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

    @FXML
    public void mute() {
        MusicManager.getInstance().toggleMute();
        updateCheckmarkUI();
    }

    @FXML
    public void muteSoundEffects() {
        MusicManager.getInstance().toggleSoundEffects();
        updateCheckmarkUI();
    }

    // --- ECCO IL PULSANTE DI USCITA ---
    // Collega questo metodo al pulsante nel FXML con onAction="#exitGame"
    @FXML
    public void exitGame() {
        System.out.println("Spegnimento in corso...");

        // Chiude l'interfaccia JavaFX
        Platform.exit();

        // Forza la chiusura della JVM (Uccide anche la musica se è rimasta bloccata)
        System.exit(0);
    }
}