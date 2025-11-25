package org.example.ProgettoUIDFinal;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.model.BossModel;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;
// Assicurati di importare BattleAnimator se è in un altro package, es:
// import org.example.ProgettoUIDFinal.view.BattleAnimator;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class bossBattleController implements Initializable {

    @FXML private ImageView playerSprite, bossSprite;
    @FXML private ProgressBar playerHealthBar, bossHealthBar;

    @FXML private  ImageView resultImageView;

    private Image imgVittoria;
    private Image imgSconfitta;

    private final double BALSELLO_Y = -10.0;
    private final Duration DURATA_PASSO = Duration.millis(500);

    private Map<Node, Timeline> idleTimelines = new HashMap<>();

    private PlayerModel player;
    private BossModel boss;

    // Variabili per ricordare la vita massima iniziale (visto che non c'è nel model)
    private double maxHpPlayer;
    private double maxHpBoss;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        caricaImmaginiRisultato();
        if (resultImageView != null) {
            resultImageView.setVisible(false); // Nascondiamo l'immagine all'inizio
        }

        // 1. Recupero i dati
        player = GameRepository.getInstance().getPlayer();
        boss = GameRepository.getInstance().getBoss();

        // 2. Setup Grafico
        if (bossSprite != null) {
            bossSprite.imageProperty().bind(boss.bossSpriteProperty());
        }
        // Nota: Assumo che lo sprite del player sia già settato o bindato altrove
        // se no dovresti fare: playerSprite.imageProperty().bind(player.avatarImageProperty());

        // 3. Setup Vita: Salviamo il valore iniziale come "Massimo"
        maxHpPlayer = player.getHp();
        maxHpBoss = boss.getBossHp();

        // Settiamo le barre piene visivamente
        playerHealthBar.setProgress(1.0);
        bossHealthBar.setProgress(1.0);

        // Colori barre (Opzionale)
        playerHealthBar.setStyle("-fx-accent: green;");
        bossHealthBar.setStyle("-fx-accent: red;");

        // 4. Avvio animazioni
        setupIdleAnimations();

        // 5. Inizio Battaglia
        gestisciInizioBattagliaAutomatico();
    }

    private void caricaImmaginiRisultato() {
        try {
            String basePath = "/org/example/ProgettoUIDFinal/images/";
            imgVittoria = new Image(getClass().getResourceAsStream(basePath + "victory.png"));
            imgSconfitta = new Image(getClass().getResourceAsStream(basePath + "defeat.png"));
        } catch (Exception e) {
            System.err.println("ERRORE: Impossibile caricare le immagini di vittoria/sconfitta. Controlla i percorsi.");
            e.printStackTrace();
        }
    }

    private void gestisciInizioBattagliaAutomatico() {
        PauseTransition pausaIniziale = new PauseTransition(Duration.seconds(1.5));
        pausaIniziale.setOnFinished(e -> {
            if (player == null || boss == null) return;

            // CONFRONTO VELOCITÀ (dai tuoi Model)
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
        // Pausa Balsello
        if (idleTimelines.containsKey(attacker)) idleTimelines.get(attacker).pause();
        attacker.setTranslateY(0);

        battleAnimator.eseguiSaltoAttacco(
                attacker,
                target,
                // --- ON HIT (Logica Danno) ---
                () -> {
                    battleAnimator.playHitEffect(target);
                    calcolaDanno(attacker); // Calcola danno e aggiorna barre
                },
                // --- ON FINISH (Logica Turno) ---
                () -> {
                    // Riprendi Balsello
                    if (idleTimelines.containsKey(attacker)) idleTimelines.get(attacker).play();

                    // CONTROLLO VITA
                    if (player.getHp() <= 0) {
                        gameOver();
                    } else if (boss.getBossHp() <= 0) {
                        vittoria();
                    } else {
                        // SCAMBIO RUOLI
                        preparaProssimoTurno(target, attacker);
                    }
                }
        );
    }

    // --- LOGICA MATEMATICA DEL DANNO ---
    private void calcolaDanno(ImageView attackerSprite) {

        if (attackerSprite == playerSprite) {
            // IL PLAYER ATTACCA IL BOSS
            int atk = player.getAtk();
            int def = boss.getBossDef();

            // Calcolo danno (minimo 1.0)
            int danno = Math.max(1, atk - def);

            // Applica al Boss
            int nuovaVita = boss.getBossHp() - danno;
            boss.setBossHp(nuovaVita);

            // Aggiorna Barra Boss (VitaAttuale / VitaMassimaIniziale)
            bossHealthBar.setProgress(nuovaVita / maxHpBoss);

            System.out.println("Player infligge " + danno + ". Boss HP rimasti: " + nuovaVita);

        } else {
            // IL BOSS ATTACCA IL PLAYER
            int atk = boss.getBossAtk();
            int def = player.getDef();

            // Calcolo danno
            int danno = Math.max(1, atk - def);

            // Applica al Player
            int nuovaVita = player.getHp() - danno;
            player.setHp(nuovaVita);

            // Aggiorna Barra Player
            playerHealthBar.setProgress(nuovaVita / maxHpPlayer);

            System.out.println("Boss infligge " + danno + ". Player HP rimasti: " + nuovaVita);
        }
    }

    private void preparaProssimoTurno(ImageView nextAttacker, ImageView nextTarget) {
        PauseTransition pausa = new PauseTransition(Duration.seconds(1));
        pausa.setOnFinished(e -> eseguiAttaccoAutomatico(nextAttacker, nextTarget));
        pausa.play();
    }


    // --- MODIFICATO: Logica Vittoria ---
    private void vittoria() {
        System.out.println("VITTORIA!");
        mostraRisultatoFinale(imgVittoria);

        // Ferma le animazioni idle per pulizia
        idleTimelines.values().forEach(Timeline::stop);
    }

    // --- MODIFICATO: Logica Game Over ---
    private void gameOver() {
        System.out.println("GAME OVER.");
        mostraRisultatoFinale(imgSconfitta);

        // Ferma le animazioni idle
        idleTimelines.values().forEach(Timeline::stop);
    }

    private void mostraRisultatoFinale(Image immagineDaMostrare) {
        if (resultImageView != null && immagineDaMostrare != null) {
            resultImageView.setImage(immagineDaMostrare);
            resultImageView.toFront(); // Porta l'immagine sopra a tutto (sprite, barre, etc.)
            resultImageView.setOpacity(0);
            resultImageView.setVisible(true);

            // Effetto Fade In elegante
            FadeTransition ft = new FadeTransition(Duration.seconds(1), resultImageView);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    // --- Animazione Idle (Copiata dal tuo codice) ---
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
}