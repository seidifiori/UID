package org.example.ProgettoUIDFinal;

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

    // --- NUOVI RIFERIMENTI ALLE IMMAGINI ---
    @FXML private ImageView musicCheckmark;
    @FXML private ImageView sfxCheckmark;

    private Scene homeScene;
    private Image checkmarkImage; // Variabile per tenere l'immagine in memoria

    public void initialize() {
        // 1. Setup ChoiceBox
        choiceBox.getItems().addAll("Piccolo", "Medio", "Grande");
        String currentStyle = StyleManager.getInstance().getFontSize();
        if (currentStyle.contains("12px")) choiceBox.setValue("Piccolo");
        else if (currentStyle.contains("18px")) choiceBox.setValue("Grande");
        else choiceBox.setValue("Medio");

        // 2. Carichiamo l'immagine UNA VOLTA sola (Percorso Relativo!)
        try {
            // Nota: Il percorso parte da 'resources'
            URL imgUrl = getClass().getResource("/org/example/ProgettoUIDFinal/imagini/Settings/checkmark.png");
            if (imgUrl != null) {
                checkmarkImage = new Image(imgUrl.toString());
            } else {
                System.err.println("Impossibile trovare checkmark.png! Controlla il percorso.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateCheckmarkUI();
    }

    private void updateCheckmarkUI() {
        if (checkmarkImage == null) return;

        if (!MusicManager.getInstance().isMuted()) {
            musicCheckmark.setImage(checkmarkImage); // Si sente -> Spunta
        } else {
            musicCheckmark.setImage(null);           // Muto -> Vuoto
        }

        // Gestione Effetti Sonori
        if (!MusicManager.getInstance().SoundEffectisMuted()) {
            sfxCheckmark.setImage(checkmarkImage);   // Si sentono -> Spunta
        } else {
            sfxCheckmark.setImage(null);             // Muti -> Vuoto
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
        // 1. Cambia lo stato
        MusicManager.getInstance().toggleMute();
        // 2. Aggiorna l'immagine
        updateCheckmarkUI();

        System.out.println("Musica mutata: " + MusicManager.getInstance().isMuted());
    }

    @FXML
    public void muteSoundEffects() {
        // 1. Cambia lo stato
        MusicManager.getInstance().toggleSoundEffects(); // Usa il metodo corretto che abbiamo sistemato prima
        // 2. Aggiorna l'immagine
        updateCheckmarkUI();

        System.out.println("Suoni mutati: " + MusicManager.getInstance().SoundEffectisMuted());
    }
}