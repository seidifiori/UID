package org.example.ProgettoUIDFinal;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;
import org.example.ProgettoUIDFinal.model.BossModel;
import org.example.ProgettoUIDFinal.Services.GameRepository;

import java.io.IOException;

/**
 * Controller della schermata Boss Lobby.
 * Gestisce:
 * - Visualizzazione del boss corrente
 * - Countdown per il cambio boss
 * - Transizione animata verso la battaglia
 * - Ritorno dalla battaglia e aggiornamento dati
 */
public class BossController {

    /* =======================
       COMPONENTI UI
       ======================= */
    @FXML private Pane flashPane;
    @FXML private Button battleButton;
    @FXML private Button BackButton;
    @FXML private Button RecollectionButton;
    @FXML private Scene homeScene;
    @FXML private Label playerName;
    @FXML private Label bossName;
    @FXML private Label recommendedLevelLabel;
    @FXML private ImageView profilePicImageView;
    @FXML private ImageView bossSprite;
    @FXML private ImageView backgroundImage;
    @FXML private ProgressBar xpBar;
    @FXML private Label countdownLabel;

    /* =======================
       COSTANTI ANIMAZIONE
       ======================= */
    private final double FLASH_DURATION_MS = 120;
    private final double LAST_FLASH_DURATION_MS = 1000;

    /* Timer per il countdown del cambio boss */
    private Timeline timerCountdown;

    /**
     * Imposta la scena Home per il ritorno alla schermata principale.
     */
    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }

    /**
     * Metodo chiamato automaticamente all'inizializzazione della schermata.
     * Aggiorna i dati del boss e avvia il countdown.
     */
    @FXML
    public void initialize() {
        GameRepository.getInstance().checkForBossUpdate();
        UpdateBossData();
        startCountdown();
        javafx.application.Platform.runLater(() -> {
            Scene scene = battleButton.getScene();
            if (scene != null) {
                scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                        if (!battleButton.isDisabled()) {
                            battleButton.fire(); // Simula il click fisico (fa partire l'animazione)
                            event.consume();
                        }
                    }
                    else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        if (!BackButton.isDisabled()) {
                            BackButton.fire();
                            event.consume();
                        }
                    }
                });
                flashPane.requestFocus();
            }
        });
    }

    /**
     * Collega la UI al BossModel corrente tramite binding.
     */
    private void UpdateBossData() {
        BossModel boss = GameRepository.getInstance().getBoss();

        bossName.textProperty().bind(boss.bossNameProperty());
        bossSprite.imageProperty().bind(boss.bossSpriteProperty());
        backgroundImage.imageProperty().bind(boss.backgroundProperty());

        recommendedLevelLabel.setText("Livello consigliato: " + boss.getRecommendedLevel());
    }

    /**
     * Gestisce il click sul pulsante "Battle".
     * Avvia una sequenza di flash e poi carica la scena di battaglia.
     */
    @FXML
    void handleBattleButton(ActionEvent event) {
        stopCountdown();
        battleButton.setDisable(true);

        // --- CONTROLLO ACCESSIBILITÀ ---
        // Verifichiamo se l'utente ha abilitato o meno i flash
        boolean flashEnabled = GameRepository.getInstance().isFlashEffectsEnabled();

        if (flashEnabled) {
            // ==========================================
            // CASO A: Animazioni attive (Utente normale)
            // ==========================================
            flashPane.setVisible(true);
            MusicManager.getInstance().playSoundEffect("battle_intro.mp3");

            double t1 = FLASH_DURATION_MS;
            double t2 = t1 + FLASH_DURATION_MS;
            double t3 = t2 + FLASH_DURATION_MS;
            double t4 = t3 + LAST_FLASH_DURATION_MS;

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> {
                        flashPane.setStyle("-fx-background-color: white;");
                        flashPane.setBlendMode(BlendMode.DIFFERENCE);
                    }),
                    // ... (resto dei keyframes identico a prima) ...
                    new KeyFrame(Duration.millis(t1), e -> {
                        flashPane.setBlendMode(null);
                        flashPane.setStyle("-fx-background-color: black;");
                    }),
                    new KeyFrame(Duration.millis(t2), e -> flashPane.setStyle("-fx-background-color: white;")),
                    new KeyFrame(Duration.millis(t3), e -> flashPane.setStyle("-fx-background-color: black;")),
                    new KeyFrame(Duration.millis(t4))
            );

            timeline.setOnFinished(e -> {
                flashPane.setVisible(false);
                flashPane.setBlendMode(null);
                startBattle(); // Avvia la battaglia alla fine dell'animazione
            });

            timeline.setDelay(Duration.millis(100));
            timeline.play();

        } else {

            Timeline safeDelay = new Timeline(new KeyFrame(Duration.millis(500), e -> startBattle()));
            safeDelay.play();
        }
    }

    private void startBattle() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bossBattle.fxml"));
            Parent bossBattleRoot = loader.load();
            bossBattleRoot.setOpacity(0.0);

            BossBattleController battleController = loader.getController();
            Scene currentScene = flashPane.getScene();

            battleController.setBossScene(currentScene);
            battleController.setLobbyController(this);

            Stage stage = (Stage) flashPane.getScene().getWindow();
            Scene battleScene = new Scene(bossBattleRoot);
            battleScene.getStylesheets().addAll(currentScene.getStylesheets());

            stage.setScene(battleScene);
            stage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(700), bossBattleRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            battleButton.setDisable(false);

        } catch (IOException e) {
            System.err.println("Errore: impossibile caricare bossBattle.fxml");
            battleButton.setDisable(false);
        }
    }

    /**
     * Metodo richiamato al ritorno dalla battaglia.
     * Controlla se il boss è cambiato e riavvia il countdown.
     */
    public void onReturnFromBattle() {

        boolean bossChanged = GameRepository.getInstance().checkForBossUpdate();

        if (bossChanged) {
            UpdateBossData();
        }

        startCountdown();
    }

    /**
     * Avvia il timer che aggiorna il countdown del prossimo boss.
     */
    public void startCountdown() {

        countdownLabel.setText(GameRepository.getInstance().getTimeUntilNextBossFormatted());

        timerCountdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            countdownLabel.setText(GameRepository.getInstance().getTimeUntilNextBossFormatted());
        }));

        timerCountdown.setCycleCount(Timeline.INDEFINITE);
        timerCountdown.play();
    }

    /**
     * Ferma il timer del countdown per evitare aggiornamenti inutili.
     */
    private void stopCountdown() {
        if (timerCountdown != null) {
            timerCountdown.stop();
        }
    }

    /**
     * Ritorna alla schermata Home.
     */
    @FXML
    public void Home() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        stopCountdown();

        if (homeScene != null) {
            Stage stage = (Stage) BackButton.getScene().getWindow();
            stage.setScene(homeScene);
        } else {
            System.err.println("Nessuna scena Home disponibile");
        }
    }
    @FXML
    public void goToRecollection() {
        try {
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");


            FXMLLoader loader = new FXMLLoader(getClass().getResource("RecollectionRoom.fxml"));
            Parent recollectionRoot = loader.load();

            Object controller = loader.getController();
            try {
                controller.getClass().getMethod("setHomeScene", Scene.class)
                        .invoke(controller, RecollectionButton.getScene());
            } catch (Exception e) {
                System.err.println("Errore nel passaggio della scena al controller: " + e.getMessage());
            }
            StyleManager.getInstance().applyStyle((Region) recollectionRoot);

            Stage currentStage = (Stage) RecollectionButton.getScene().getWindow();
            Scene nextScene = new Scene(recollectionRoot);
            nextScene.getStylesheets().addAll(RecollectionButton.getScene().getStylesheets());
            currentStage.setScene(nextScene);
            currentStage.show();

        } catch (IOException e) {
            System.err.println("Errore: Impossibile caricare RecollectionRoom.fxml. Controlla il percorso!");
            e.printStackTrace();
        }
    }

}
