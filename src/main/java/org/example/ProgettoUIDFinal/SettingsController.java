package org.example.ProgettoUIDFinal;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.net.URL;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;
import org.example.ProgettoUIDFinal.Services.GameRepository;

public class SettingsController {

    // --- ELEMENTI UI ---
    @FXML private ChoiceBox<String> choiceBox;
    @FXML private Button BackButton;
    @FXML private Button QuitButton;

    // Checkmarks esistenti
    @FXML private ImageView musicCheckmark;
    @FXML private ImageView sfxCheckmark;

    // NUOVO: Checkmark per le animazioni Flash
    @FXML private ImageView flashCheckmark;

    private Scene homeScene;
    private Image checkmarkImage;

    public void initialize() {
        // ... (codice choiceBox esistente) ...
        choiceBox.getItems().addAll("Piccolo", "Medio", "Grande");
        String currentStyle = StyleManager.getInstance().getFontSize();
        if (currentStyle.contains("12px")) choiceBox.setValue("Piccolo");
        else if (currentStyle.contains("18px")) choiceBox.setValue("Grande");
        else choiceBox.setValue("Medio");
        // ...

        try {
            URL imgUrl = getClass().getResource("/org/example/ProgettoUIDFinal/imagini/Settings/checkmark.png");
            if (imgUrl != null) {
                checkmarkImage = new Image(imgUrl.toString());
            }
        } catch (Exception e) {
            System.err.println("SettingsController: Errore checkmark.png");
        }

        updateCheckmarkUI();
        BackButton.setCancelButton(true);
    }

    private void updateCheckmarkUI() {
        if (checkmarkImage == null) return;

        // Audio
        musicCheckmark.setImage(!MusicManager.getInstance().isMusicMuted() ? checkmarkImage : null);
        sfxCheckmark.setImage(!MusicManager.getInstance().isSfxMuted() ? checkmarkImage : null);

        // NUOVO: Aggiorna la spunta delle animazioni leggendo dal Repository
        boolean isFlashActive = GameRepository.getInstance().isFlashEffectsEnabled();
        flashCheckmark.setImage(isFlashActive ? checkmarkImage : null);
    }

    // ... (setHomeScene, choiceChanged, Home methods rimangono uguali) ...
    public void setHomeScene(Scene scene) { this.homeScene = scene; }

    @FXML private void choiceChanged() {
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

    @FXML public void Home() {
        if (homeScene != null) {
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");
            StyleManager.getInstance().applyStyle(homeScene);
            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        }
    }

    @FXML public void toggleMusicMute() {
        MusicManager.getInstance().toggleMute();
        updateCheckmarkUI();
    }

    @FXML public void toggleSoundEffectsMute() {
        MusicManager.getInstance().toggleSoundEffects();
        updateCheckmarkUI();
    }

    /**
     * NUOVO METODO: Attiva/Disattiva le luci stroboscopiche
     * Collega questo metodo al pulsante nel file FXML!
     */
    @FXML
    public void toggleFlashEffects() {
        boolean currentStatus = GameRepository.getInstance().isFlashEffectsEnabled();
        // Invertiamo lo stato
        GameRepository.getInstance().setFlashEffectsEnabled(!currentStatus);

        // Aggiorniamo la grafica
        updateCheckmarkUI();
    }

    @FXML public void exitGame() {
        GameRepository.getInstance().saveGameToJSON();
        Platform.exit();
        System.exit(0);
    }
}