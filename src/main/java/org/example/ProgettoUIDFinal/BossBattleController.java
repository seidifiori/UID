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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.BattleAnimator;
import org.example.ProgettoUIDFinal.model.BossModel;
import org.example.ProgettoUIDFinal.Services.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller JavaFX che gestisce il combattimento contro il Boss.
 * Implementa:
 * - Logica turn-based automatica
 * - Animazioni di idle e attacco
 * - Calcolo danni Player/Boss
 * - Gestione UI (barre HP, risultati, skip, restart)
 * - Gestione audio (musica + effetti)
 */
public class BossBattleController implements Initializable {

    /* =======================
       COMPONENTI UI PLAYER
       ======================= */
    @FXML private StackPane playerContainer;

    @FXML private ImageView baseAvatarLayer;
    @FXML private ImageView hairLayer;
    @FXML private ImageView hatLayer;
    @FXML private ImageView armorLayer;
    @FXML private ImageView swordLayer;
    @FXML private ImageView shieldLayer;

    /* =======================
       COMPONENTI UI BOSS
       ======================= */
    @FXML private ImageView bossSprite;
    @FXML private ImageView arenaImage;
    @FXML private ProgressBar playerHealthBar, bossHealthBar;

    /* =======================
       COMPONENTI RISULTATO
       ======================= */
    @FXML private ImageView resultImageView;
    @FXML private ImageView frameBossBattle;

    @FXML private Button exitButton;
    @FXML private Button restartButton;
    @FXML private Button skipButton;

    @FXML private Scene bossScene;

    /* =======================
       RISORSE IMMAGINI
       ======================= */
    private Image imgVittoria;
    private Image imgSconfitta;

    /* =======================
       COSTANTI ANIMAZIONI
       ======================= */
    private final double BALSELLO_Y = -10.0;
    private final Duration DURATA_PASSO = Duration.millis(500);

    /* Animazioni idle associate ai nodi */
    private Map<Node, Timeline> idleTimelines = new HashMap<>();

    /* Transizione tra turni */
    private PauseTransition turnTransition;

    /* =======================
       MODELLI DI GIOCO
       ======================= */
    private PlayerModel player;
    private BossModel boss;

    /* HP locali usati solo durante la battaglia */
    private int battleHpPlayer;
    private int battleHpBoss;

    private double maxHpPlayer;
    private double maxHpBoss;

    /* Stato della battaglia */
    private boolean isBattleRunning = true;

    /* Controller della lobby boss (schermata precedente) */
    private BossController lobbyController;

    /* =======================
       SETTER DI SUPPORTO
       ======================= */
    public void setBossScene(Scene scene) { this.bossScene = scene; }
    public void setLobbyController(BossController controller) { this.lobbyController = controller; }

    /**
     * Inizializza la battaglia:
     * - Carica risorse
     * - Recupera modelli
     * - Collega UI ↔ Model
     * - Avvia musica e animazioni
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        showResult();

        /* Stato iniziale UI */
        resultImageView.setVisible(false);
        frameBossBattle.setVisible(false);
        exitButton.setVisible(false);
        restartButton.setVisible(false);

        /* Recupero modelli dal repository */
        player = GameRepository.getInstance().getPlayer();
        boss = GameRepository.getInstance().getBoss();

        /* Avvio musica del boss */
        if (boss != null && boss.getMusicPath() != null) {
            MusicManager.getInstance().playMusic(boss.getMusicPath());
        } else {
            MusicManager.getInstance().playMusic("Battle_theme.mp3");
        }

        /* Binding immagini boss e arena */
        bossSprite.imageProperty().bind(boss.bossSpriteProperty());
        arenaImage.imageProperty().bind(boss.arenaProperty());

        /* Salvataggio HP massimi */
        maxHpPlayer = player.getHp();
        maxHpBoss = boss.getBossHp();

        /* Binding layer avatar player */
        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.swordImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());

        hairLayer.visibleProperty().bind(player.isHairVisibleProperty());

        resetBattle();
    }

    /**
     * Collega in sicurezza un ImageView a una proprietà Image.
     */
    private void bindLayer(ImageView view, javafx.beans.value.ObservableValue<? extends Image> prop) {
        if (view != null) view.imageProperty().bind(prop);
    }

    /**
     * Reset completo dello stato della battaglia e avvio automatico.
     */
    private void resetBattle() {
        battleHpPlayer = (int) maxHpPlayer;
        battleHpBoss = (int) maxHpBoss;
        isBattleRunning = true;

        playerHealthBar.setProgress(1.0);
        bossHealthBar.setProgress(1.0);

        skipButton.setVisible(true);
        skipButton.setDisable(true);

        PauseTransition delaySkip = new PauseTransition(Duration.seconds(1));
        delaySkip.setOnFinished(e -> {
            if (isBattleRunning) skipButton.setDisable(false);
        });
        delaySkip.play();

        setupIdleAnimations();
        StartBattle();
    }

    /**
     * Carica le immagini di vittoria e sconfitta.
     */
    private void showResult() {
        String basePath = "/org/example/ProgettoUIDFinal/imagini/Boss/";
        imgVittoria = new Image(getClass().getResourceAsStream(basePath + "boss-victory.png"));
        imgSconfitta = new Image(getClass().getResourceAsStream(basePath + "boss-defeat.png"));
    }

    /**
     * Decide chi attacca per primo in base alla velocità.
     */
    private void StartBattle() {
        PauseTransition pausa = new PauseTransition(Duration.seconds(1.5));
        pausa.setOnFinished(e -> {
            if (player.getVel() >= boss.getBossVel())
                Attack(playerContainer, bossSprite);
            else
                Attack(bossSprite, playerContainer);
        });
        pausa.play();
    }

    /**
     * Esegue un turno di attacco animato e gestisce il flusso del combattimento.
     */
    private void Attack(Node attacker, Node target) {

        if (!isBattleRunning) return;

        idleTimelines.get(attacker).pause();

        BattleAnimator.jumpAndAttack(
                attacker,
                target,

                /* ON HIT */
                () -> {
                    if (!isBattleRunning) return;

                    if (attacker == playerContainer)
                        MusicManager.getInstance().playSoundEffect("playerattack.mp3");
                    else
                        MusicManager.getInstance().playSoundEffect("enemyattack.mp3");

                    BattleAnimator.playHitEffect(target);
                    calculateDamage(attacker);
                },

                /* ON FINISH */
                () -> {
                    idleTimelines.get(attacker).play();

                    if (battleHpPlayer <= 0) gameOver();
                    else if (battleHpBoss <= 0) Victory();
                    else NextTurn(target, attacker);
                }
        );
    }

    /**
     * Calcola il danno applicando una formula base ATK - DEF.
     */
    private void calculateDamage(Node attacker) {

        if (attacker == playerContainer) {
            int danno = Math.max(1, player.getAtk() - boss.getBossDef());
            battleHpBoss -= danno;
            bossHealthBar.setProgress(Math.max(0, battleHpBoss) / maxHpBoss);
        } else {
            int danno = Math.max(1, boss.getBossAtk() - player.getDef());
            battleHpPlayer -= danno;
            playerHealthBar.setProgress(Math.max(0, battleHpPlayer) / maxHpPlayer);
        }
    }

    /**
     * Inserisce una pausa tra un turno e l’altro.
     */
    private void NextTurn(Node attacker, Node target) {
        PauseTransition pausa = new PauseTransition(Duration.seconds(1));
        pausa.setOnFinished(e -> Attack(attacker, target));
        pausa.play();
    }

    /**
     * Gestisce la vittoria del player.
     */
    private void Victory() {
        MusicManager.getInstance().playMusic("victory.mp3");
        ShowResults(imgVittoria);
        idleTimelines.values().forEach(Timeline::stop);

        // ----------------------------

        if (!player.isDefeated()) {
            player.setGold(player.getGold() + 1000);
            player.increaseXp(400);
            player.setDefeated(true);
            MusicManager.getInstance().playSoundEffect("xp_gain.mp3");
            if (boss != null) {
                GameRepository.getInstance().markBossAsDefeated(boss.getBossName());
                GameRepository.getInstance().saveGameToJSON();
            }
            System.out.println("il trofeo '"+boss.getBossName()+"' è stato aggiunto alla collezione!");


        }
    }

    /**
     * Gestisce la sconfitta del player.
     */
    private void gameOver() {
        MusicManager.getInstance().playMusic("defeat.mp3");
        ShowResults(imgSconfitta);
        idleTimelines.values().forEach(Timeline::stop);
    }

    /**
     * Mostra schermata finale con animazioni FadeIn.
     */
    private void ShowResults(Image img) {
        skipButton.setVisible(false);

        frameBossBattle.setVisible(true);
        resultImageView.setImage(img);
        resultImageView.setOpacity(0);
        resultImageView.setVisible(true);

        exitButton.setOpacity(0);
        exitButton.setVisible(true);

        restartButton.setOpacity(0);
        restartButton.setVisible(true);

        ParallelTransition pt = new ParallelTransition(
                new FadeTransition(Duration.seconds(1), resultImageView),
                new FadeTransition(Duration.seconds(1), exitButton),
                new FadeTransition(Duration.seconds(1), restartButton)
        );

        pt.getChildren().forEach(t -> ((FadeTransition)t).setToValue(1));
        pt.play();
    }

    /**
     * Inizializza le animazioni idle di player e boss.
     */
    private void setupIdleAnimations() {
        idleTimelines.put(playerContainer, createIdleAnimation(playerContainer));
        idleTimelines.put(bossSprite, createIdleAnimation(bossSprite));

        idleTimelines.get(playerContainer).play();
        Timeline bossAnim = idleTimelines.get(bossSprite);
        bossAnim.setDelay(DURATA_PASSO.divide(2));
        bossAnim.play();
    }

    /**
     * Crea una semplice animazione oscillante (idle).
     */
    private Timeline createIdleAnimation(Node node) {
        Timeline timeline = new Timeline();
        KeyFrame frameIniziale = new KeyFrame(Duration.ZERO, new KeyValue(node.translateYProperty(), 0, Interpolator.DISCRETE));
        KeyFrame frameSu = new KeyFrame(DURATA_PASSO, new KeyValue(node.translateYProperty(), BALSELLO_Y, Interpolator.DISCRETE));
        KeyFrame frameGiu = new KeyFrame(DURATA_PASSO.multiply(2), new KeyValue(node.translateYProperty(), 0, Interpolator.DISCRETE));
        timeline.getKeyFrames().addAll(frameIniziale, frameSu, frameGiu);
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    /**
     * Salta la battaglia ed esegue il calcolo istantaneo dei turni.
     */
    @FXML
    public void handleSkip() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        if (!isBattleRunning) return;

        isBattleRunning = false;
        idleTimelines.values().forEach(Timeline::stop);

        while (battleHpPlayer > 0 && battleHpBoss > 0) {
            calculateDamage(playerContainer);
            if (battleHpBoss <= 0) break;
            calculateDamage(bossSprite);
        }

        if (battleHpPlayer <= 0) gameOver();
        else Victory();
    }

    /**
     * Ritorna alla lobby boss.
     */
    @FXML
    public void backToBossScene() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("background_music.mp3");

        if (lobbyController != null) {
            lobbyController.onReturnFromBattle();
        }

        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.setScene(bossScene);
    }

    /**
     * Riavvia completamente la battaglia.
     */
    @FXML
    public void restartBattle() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("Battle_theme.mp3");

        resultImageView.setVisible(false);
        frameBossBattle.setVisible(false);
        exitButton.setVisible(false);
        restartButton.setVisible(false);

        resetBattle();
    }
}
