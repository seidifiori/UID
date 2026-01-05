package org.example.ProgettoUIDFinal;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.model.BossModel;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.ResourceBundle;

public class bossController{

    // Inietta gli elementi dall'FXML
    @FXML private Pane flashPane;
    @FXML private Button battleButton;
    @FXML private Button BackButton;
    @FXML private Scene homeScene;
    @FXML private Label playerName;
    @FXML private Label bossName;
    @FXML private ImageView profilePicImageView;
    @FXML private ImageView bossSprite;
    @FXML private ImageView backgroundImage;
    @FXML private ProgressBar xpBar;
    @FXML private Label countdownLabel;


    private double FLASH_DURATION_MS = 120; // Durata minima singolo flash
    private double LAST_FLASH_DURATION_MS = 1000; // Durata dell'ultimo flash nero (1 secondo)
    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }

    private Timeline timerCountdown;

    @FXML
    public void initialize(){
        PlayerModel player = GameRepository.getInstance().getPlayer();
        GameRepository.getInstance().checkForBossUpdate();

        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }

        if (playerName != null) {
            playerName.textProperty().bind(player.playerNameProperty());
        }

        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }

        aggiornaDatiBoss();
        startCountdown();
    }

    private void aggiornaDatiBoss() {
        BossModel boss = GameRepository.getInstance().getBoss();// Prende il boss (nuovo o vecchio)
        if (bossName != null) bossName.textProperty().bind(boss.bossNameProperty());
        if (bossSprite != null) bossSprite.imageProperty().bind(boss.bossSpriteProperty());
        if (backgroundImage != null) backgroundImage.imageProperty().bind(boss.backgroundProperty());

    }

    @FXML
    void handleBattleButton(ActionEvent event) {
        stopCountdown();

        battleButton.setDisable(true);
        flashPane.setVisible(true);

        double time1 = FLASH_DURATION_MS; // T=120ms
        double time2 = time1 + FLASH_DURATION_MS; // T=200ms
        double time3 = time2 + FLASH_DURATION_MS; // T=280ms (inizio ultimo flash)
        double time4_fine = time3 + LAST_FLASH_DURATION_MS; // T=280 + 1000 = 1280ms

        //Timeline per l'animazione
        Timeline timeline = new Timeline(

                new KeyFrame(Duration.ZERO, e -> {
                    flashPane.setStyle("-fx-background-color: white;");
                    flashPane.setBlendMode(BlendMode.DIFFERENCE); // inversione colori
                }),

                new KeyFrame(Duration.millis(time1), e -> {
                    flashPane.setBlendMode(null); // reset inversione colori
                    flashPane.setStyle("-fx-background-color: black;");
                }),

                new KeyFrame(Duration.millis(time2), e -> {
                    flashPane.setStyle("-fx-background-color: white;");
                }),

                new KeyFrame(Duration.millis(time3), e -> {
                    flashPane.setStyle("-fx-background-color: black;");
                }),

                new KeyFrame(Duration.millis(time4_fine))
        );

        // fine timeline
        timeline.setOnFinished(e -> {
            flashPane.setVisible(false);
            flashPane.setBlendMode(null);

            startBattle();
        });

        timeline.setDelay(Duration.millis(100));
        timeline.play();
    }

    //cambio scena dopo la timeline
    private void startBattle() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("bossBattle.fxml"));
            Parent bossBattleRoot = loader.load();
            bossBattleRoot.setOpacity(0.0);

            bossBattleController bbc = loader.getController();
            Scene currentBossScene = flashPane.getScene();
            bbc.setBossScene(currentBossScene);

            Stage stage = (Stage) flashPane.getScene().getWindow();
            Scene bossScene = new Scene(bossBattleRoot);
            bossScene.getStylesheets().addAll(flashPane.getScene().getStylesheets());
            bbc.setLobbyController(this);

            battleButton.setDisable(false);

            stage.setScene(bossScene);
            stage.show();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(700), bossBattleRoot);
            fadeIn.setFromValue(0.0); // Opacità iniziale
            fadeIn.setToValue(1.0);   // Opacità finale
            fadeIn.play();

        } catch (IOException e) {
            System.err.println("Errore: Impossibile caricare bossBattle.fxml");
            e.printStackTrace();
            battleButton.setDisable(false);
        }
    }

    public void onReturnFromBattle() {
        // 1. Controlliamo ORA se il tempo è scaduto mentre eravamo in battaglia
        boolean bossChanged = GameRepository.getInstance().checkForBossUpdate();

        if (bossChanged) {
            System.out.println("Il boss è cambiato mentre eri in battaglia!");
            // Se è cambiato, ricolleghiamo le immagini e i testi al nuovo BossModel
            aggiornaDatiBoss();
        }

        // 2. Facciamo ripartire il timer (che mostrerà il tempo del nuovo ciclo)
        startCountdown();
    }

    public void startCountdown() {
        if (countdownLabel != null) {
            countdownLabel.setText(GameRepository.getInstance().getTimeUntilNextBossFormatted());
        }

        // 2. IMPOSTAZIONE DEL TIMER (Per i secondi successivi)
        timerCountdown = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            // Chiede al Repository quanto manca
            String tempoRimasto = GameRepository.getInstance().getTimeUntilNextBossFormatted();

            // Aggiorna la scritta
            if (countdownLabel != null) {
                countdownLabel.setText(tempoRimasto);
            }
        }));

        timerCountdown.setCycleCount(Timeline.INDEFINITE); // Ripeti all'infinito
        timerCountdown.play(); // Avvia
    }

    // Importante: Quando cambi scena (vai in battaglia o home), ferma il timer per risparmiare risorse
    private void stopCountdown() {
        if (timerCountdown != null) {
            timerCountdown.stop();
        }
    }

    @FXML
    public void Home() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        stopCountdown();
        if (homeScene != null) {
            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        } else {
            System.err.println("⚠ Nessuna scena Home disponibile!");
        }
    }
}

