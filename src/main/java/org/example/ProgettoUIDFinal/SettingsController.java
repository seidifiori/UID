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

/**
 * CONTROLLER IMPOSTAZIONI: Gestisce la configurazione utente del gioco.
 * Permette la regolazione dell'accessibilità (font size), le preferenze audio
 * (Mute/Unmute) e il processo di terminazione sicura dell'applicazione.
 */
public class SettingsController {

    // --- ELEMENTI UI (Iniezione FXML) ---
    @FXML private ChoiceBox<String> choiceBox;
    @FXML private Button BackButton;
    @FXML private Button QuitButton;

    // Indicatori grafici per lo stato audio (Checkmarks)
    @FXML private ImageView musicCheckmark;
    @FXML private ImageView sfxCheckmark;

    private Scene homeScene;
    private Image checkmarkImage;

    /**
     * INIZIALIZZAZIONE: Configura lo stato iniziale della finestra.
     * Sincronizza i controlli grafici con lo stato dei servizi MusicManager e StyleManager.
     */
    public void initialize() {
        // Popolamento ChoiceBox per la regolazione dell'accessibilità visiva
        choiceBox.getItems().addAll("Piccolo", "Medio", "Grande");

        // Recupero dello stile corrente dallo StyleManager per impostare il valore di default
        String currentStyle = StyleManager.getInstance().getFontSize();
        if (currentStyle.contains("12px")) choiceBox.setValue("Piccolo");
        else if (currentStyle.contains("18px")) choiceBox.setValue("Grande");
        else choiceBox.setValue("Medio");

        // Caricamento della risorsa grafica per i checkmark
        try {
            URL imgUrl = getClass().getResource("/org/example/ProgettoUIDFinal/imagini/Settings/checkmark.png");
            if (imgUrl != null) {
                checkmarkImage = new Image(imgUrl.toString());
            }
        } catch (Exception e) {
            System.err.println("SettingsController: Errore nel caricamento del file checkmark.png");
        }

        // Aggiornamento della UI in base alle preferenze caricate
        updateCheckmarkUI();
    }

    /**
     * UI SYNC: Aggiorna la visibilità dei checkmark basandosi sullo stato dei flussi audio.
     * Interroga il MusicManager per sapere se la musica o i suoni sono attivi.
     */
    private void updateCheckmarkUI() {
        if (checkmarkImage == null) return;

        // Visualizza il checkmark se l'audio NON è mutato
        musicCheckmark.setImage(!MusicManager.getInstance().isMusicMuted() ? checkmarkImage : null);
        sfxCheckmark.setImage(!MusicManager.getInstance().isSfxMuted() ? checkmarkImage : null);
    }

    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }

    /**
     * GESTIONE ACCESSIBILITÀ: Reagisce alla selezione del ChoiceBox per cambiare il font.
     * Utilizza lo StyleManager per iniettare regole CSS inline dinamicamente.
     */
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

        // Aggiornamento dello stato globale e applicazione immediata alla scena corrente
        StyleManager.getInstance().setFontSize(fontSizeStyle);
        if (choiceBox.getScene() != null) {
            StyleManager.getInstance().applyStyle(choiceBox.getScene());
        }
    }

    /**
     * NAVIGAZIONE: Ritorna alla scena precedente applicando lo stile aggiornato.
     */
    @FXML
    public void Home() {
        if (homeScene != null) {
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");
            StyleManager.getInstance().applyStyle(homeScene);
            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        }
    }

    /**
     * AUDIO CONTROL: Alterna lo stato di muto della musica tramite il MusicManager.
     */
    @FXML
    public void toggleMusicMute() {
        MusicManager.getInstance().toggleMute();
        updateCheckmarkUI();
    }

    /**
     * AUDIO CONTROL: Alterna lo stato di muto degli effetti sonori.
     */
    @FXML
    public void toggleSoundEffectsMute() {
        MusicManager.getInstance().toggleSoundEffects();
        updateCheckmarkUI();
    }

    /**
     * SAFE SHUTDOWN: Gestisce la procedura di chiusura controllata.
     * 1. Triggera il salvataggio su disco (JSON) tramite il GameRepository.
     * 2. Chiude correttamente il ciclo di vita delle finestre JavaFX.
     * 3. Termina il processo della Virtual Machine.
     */
    @FXML
    public void exitGame() {
        System.out.println("SettingsController: Procedura di salvataggio e spegnimento...");

        // Sincronizzazione dello stato RAM -> DISCO
        GameRepository.getInstance().saveGameToJSON();

        // Terminazione del Toolkit grafico
        Platform.exit();

        // Uscita definitiva dal processo
        System.exit(0);
    }
}