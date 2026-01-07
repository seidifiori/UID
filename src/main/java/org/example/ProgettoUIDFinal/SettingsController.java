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
import org.example.ProgettoUIDFinal.model.GameRepository;

public class SettingsController {

    @FXML private ChoiceBox<String> choiceBox;
    @FXML private Button BackButton;
    @FXML private Button QuitButton;

    // --- RIFERIMENTI ALLE IMMAGINI ---
    @FXML private ImageView musicCheckmark;
    @FXML private ImageView sfxCheckmark;

    private Scene homeScene;
    private Image checkmarkImage;

    public void initialize() {

        choiceBox.getItems().addAll("Piccolo", "Medio", "Grande");
        String currentStyle = StyleManager.getInstance().getFontSize();
        if (currentStyle.contains("12px")) choiceBox.setValue("Piccolo");
        else if (currentStyle.contains("18px")) choiceBox.setValue("Grande");
        else choiceBox.setValue("Medio");

        try {
            URL imgUrl = getClass().getResource("/org/example/ProgettoUIDFinal/imagini/Settings/checkmark.png");
            if (imgUrl != null) {
                checkmarkImage = new Image(imgUrl.toString());
            } else {
                System.err.println("Impossibile trovare checkmark.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Imposta i checkmark in base alle preferenze caricate
        updateCheckmarkUI();
    }

    private void updateCheckmarkUI() {
        if (checkmarkImage == null) return;

        // Verifica se la musica è mutata (MusicManager.isMusicMuted())
        if (!MusicManager.getInstance().isMusicMuted()) {
            musicCheckmark.setImage(checkmarkImage);
        } else {
            musicCheckmark.setImage(null);
        }

        // Verifica se gli effetti sono mutati (MusicManager.isSfxMuted())
        if (!MusicManager.getInstance().isSfxMuted()) {
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
    public void toggleMusicMute() {
        MusicManager.getInstance().toggleMute();
        updateCheckmarkUI();
    }

    @FXML
    public void toggleSoundEffectsMute() {
        MusicManager.getInstance().toggleSoundEffects();
        updateCheckmarkUI();
    }

    // pulsante di salvataggio/chiusura
    @FXML
    public void exitGame() {
        System.out.println("Spegnimento in corso...");

        // salva i dati prima di chiudere
        GameRepository.getInstance().saveGameToJSON();

        // Chiude l'interfaccia JavaFX
        Platform.exit();

        // Forza la chiusura completa
        System.exit(0);
    }
}