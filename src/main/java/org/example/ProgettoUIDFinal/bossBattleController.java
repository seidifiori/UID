package org.example.ProgettoUIDFinal;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.model.BossModel;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;
// import org.example.ProgettoUIDFinal.view.BattleAnimator; // Decommenta se serve

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class bossBattleController implements Initializable {

    @FXML private ImageView playerSprite, bossSprite;
    @FXML private ProgressBar playerHealthBar, bossHealthBar;

    @FXML private ImageView resultImageView;
    @FXML private Scene bossScene;

    @FXML private Button exitButton;
    @FXML private Button restartButton;

    private Image imgVittoria;
    private Image imgSconfitta;

    private final double BALSELLO_Y = -10.0;
    private final Duration DURATA_PASSO = Duration.millis(500);

    private Map<Node, Timeline> idleTimelines = new HashMap<>();

    private PlayerModel player;
    private BossModel boss;

    private int battleHpPlayer;
    private int battleHpBoss;

    private double maxHpPlayer;
    private double maxHpBoss;

    public void setBossScene(Scene scene) {
        this.bossScene = scene;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        caricaImmaginiRisultato();

        // --- 1. NASCONDIAMO TUTTO ALL'INIZIO ---
        // Semplice: setVisible false. Niente opacity o altro per ora.
        if (resultImageView != null) resultImageView.setVisible(false);
        if (exitButton != null) exitButton.setVisible(false);
        if (restartButton != null) restartButton.setVisible(false);

        // Recupero dati e setup iniziale (Come prima)
        player = GameRepository.getInstance().getPlayer();
        boss = GameRepository.getInstance().getBoss();

        if (bossSprite != null) bossSprite.imageProperty().bind(boss.bossSpriteProperty());

        maxHpPlayer = player.getHp();
        maxHpBoss = boss.getBossHp();

        // Setup battaglia iniziale
        resettaEIniziaBattaglia();
    }

    private void resettaEIniziaBattaglia() {
        // Reset variabili locali
        battleHpPlayer = (int) maxHpPlayer;
        battleHpBoss = (int) maxHpBoss;

        // Reset grafica
        playerHealthBar.setProgress(1.0);
        bossHealthBar.setProgress(1.0);

        // Avvio animazioni
        setupIdleAnimations();
        gestisciInizioBattagliaAutomatico();
    }

    private void caricaImmaginiRisultato() {
        try {
            String basePath = "/org/example/ProgettoUIDFinal/images/"; // Controlla che il path sia corretto
            imgVittoria = new Image(getClass().getResourceAsStream(basePath + "victory.png"));
            imgSconfitta = new Image(getClass().getResourceAsStream(basePath + "defeat.png"));
        } catch (Exception e) {
            System.err.println("ERRORE: Impossibile caricare le immagini.");
        }
    }

    private void gestisciInizioBattagliaAutomatico() {
        PauseTransition pausaIniziale = new PauseTransition(Duration.seconds(1.5));
        pausaIniziale.setOnFinished(e -> {
            if (player == null || boss == null) return;

            int pVel = player.getVel();
            int bVel = boss.getBossVel();

            System.out.println("Velocità -> Player: " + pVel + " | Boss: " + bVel);

            if (pVel >= bVel) {
                System.out.println("Il Player è più veloce! Inizia lui.");
                eseguiAttaccoAutomatico(playerSprite, bossSprite);
            } else {
                System.out.println("Il Boss è più veloce! Inizia lui.");
                eseguiAttaccoAutomatico(bossSprite, playerSprite);
            }
        });
        pausaIniziale.play();
    }

    private void eseguiAttaccoAutomatico(ImageView attacker, ImageView target) {
        if (idleTimelines.containsKey(attacker)) idleTimelines.get(attacker).pause();
        attacker.setTranslateY(0);

        battleAnimator.eseguiSaltoAttacco(
                attacker,
                target,
                // --- ON HIT ---
                () -> {
                    battleAnimator.playHitEffect(target);
                    calcolaDanno(attacker); // Aggiorna le variabili locali
                },
                // --- ON FINISH ---
                () -> {
                    if (idleTimelines.containsKey(attacker)) idleTimelines.get(attacker).play();

                    // --- CONTROLLO VITA SULLE VARIABILI LOCALI ---
                    if (battleHpPlayer <= 0) {
                        gameOver();
                    } else if (battleHpBoss <= 0) {
                        vittoria();
                    } else {
                        preparaProssimoTurno(target, attacker);
                    }
                }
        );
    }

    private void calcolaDanno(ImageView attackerSprite) {

        if (attackerSprite == playerSprite) {
            // IL PLAYER ATTACCA
            int atk = player.getAtk();
            int def = boss.getBossDef();

            int danno = Math.max(1, atk - def);

            // Modifichiamo la variabile LOCALE
            battleHpBoss -= danno;

            // Aggiorna Barra Boss (usando variabile locale)
            // Math.max(0, ...) serve per non mostrare barra negativa
            double progress = (double) Math.max(0, battleHpBoss) / maxHpBoss;
            bossHealthBar.setProgress(progress);

            System.out.println("Player infligge " + danno + ". Boss HP rimasti (Locali): " + battleHpBoss);

        } else {
            // IL BOSS ATTACCA
            int atk = boss.getBossAtk();
            int def = player.getDef();

            int danno = Math.max(1, atk - def);

            // Modifichiamo la variabile LOCALE
            battleHpPlayer -= danno;

            // Aggiorna Barra Player
            double progress = (double) Math.max(0, battleHpPlayer) / maxHpPlayer;
            playerHealthBar.setProgress(progress);

            System.out.println("Boss infligge " + danno + ". Player HP rimasti (Locali): " + battleHpPlayer);
        }
    }

    private void preparaProssimoTurno(ImageView nextAttacker, ImageView nextTarget) {
        PauseTransition pausa = new PauseTransition(Duration.seconds(1));
        pausa.setOnFinished(e -> eseguiAttaccoAutomatico(nextAttacker, nextTarget));
        pausa.play();
    }

    private void vittoria() {
        System.out.println("VITTORIA!");
        mostraRisultatoFinale(imgVittoria);
        idleTimelines.values().forEach(Timeline::stop);
    }

    private void gameOver() {
        System.out.println("GAME OVER.");
        mostraRisultatoFinale(imgSconfitta);
        idleTimelines.values().forEach(Timeline::stop);
    }

    private void mostraRisultatoFinale(Image immagineDaMostrare) {
        if (resultImageView != null && immagineDaMostrare != null) {
            // 1. Mostra l'immagine
            resultImageView.setImage(immagineDaMostrare);
            resultImageView.toFront(); // Importante se l'immagine è grande
            resultImageView.setOpacity(0);
            resultImageView.setVisible(true);

            // 2. Prepara i bottoni (rendili visibili ma trasparenti)
            exitButton.setOpacity(0);
            exitButton.setVisible(true);
            exitButton.toFront(); // Assicura che siano cliccabili

            restartButton.setOpacity(0);
            restartButton.setVisible(true);
            restartButton.toFront();

            // 3. Crea le animazioni di dissolvenza (Fade In)
            FadeTransition ftImg = new FadeTransition(Duration.seconds(1), resultImageView);
            ftImg.setToValue(1.0);

            FadeTransition ftExit = new FadeTransition(Duration.seconds(1), exitButton);
            ftExit.setToValue(1.0);

            FadeTransition ftRestart = new FadeTransition(Duration.seconds(1), restartButton);
            ftRestart.setToValue(1.0);

            // 4. Esegui le animazioni tutte insieme
            ParallelTransition pt = new ParallelTransition(ftImg, ftExit, ftRestart);
            pt.play();
        }
    }

    private void setupIdleAnimations() {
        idleTimelines.put(playerSprite, createIdleAnimation(playerSprite));
        idleTimelines.put(bossSprite, createIdleAnimation(bossSprite));
        idleTimelines.get(playerSprite).play();
        Timeline bossAnim = idleTimelines.get(bossSprite);
        bossAnim.setDelay(DURATA_PASSO.divide(2));
        bossAnim.play();
    }

    private Timeline createIdleAnimation(Node node) {
        Timeline timeline = new Timeline();
        KeyFrame frameIniziale = new KeyFrame(Duration.ZERO, new KeyValue(node.translateYProperty(), 0, Interpolator.DISCRETE));
        KeyFrame frameSu = new KeyFrame(DURATA_PASSO, new KeyValue(node.translateYProperty(), BALSELLO_Y, Interpolator.DISCRETE));
        KeyFrame frameGiu = new KeyFrame(DURATA_PASSO.multiply(2), new KeyValue(node.translateYProperty(), 0, Interpolator.DISCRETE));
        timeline.getKeyFrames().addAll(frameIniziale, frameSu, frameGiu);
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    @FXML
    public void backToBossScene() {
        if (bossScene != null) {
            Stage currentStage = (Stage) exitButton.getScene().getWindow();
            currentStage.setScene(bossScene);
        } else {
            System.err.println("⚠ Nessuna scena precedente disponibile!");
        }
    }

    @FXML
    public void restartBattle() {
        // Nascondiamo di nuovo i risultati
        resultImageView.setVisible(false);
        exitButton.setVisible(false);
        restartButton.setVisible(false);

        // Resettiamo e ripartiamo
        resettaEIniziaBattaglia();
    }


}