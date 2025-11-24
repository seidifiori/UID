package org.example.ProgettoUIDFinal;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.model.BossModel;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class bossBattleController implements Initializable {

    @FXML private ImageView playerSprite, bossSprite;
    @FXML private Button enemyButton, playerButton;

    private final double BALSELLO_Y = -10.0;
    private final Duration DURATA_PASSO = Duration.millis(500);

    private Map<Node, Timeline> idleTimelines = new HashMap<>();

    private PlayerModel player;
    private BossModel boss;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        player = GameRepository.getInstance().getPlayer();
        boss = GameRepository.getInstance().getBoss();

        if (bossSprite != null) {
            bossSprite.imageProperty().bind(boss.bossSpriteProperty());
        }

        // 3. Avvio animazioni Idle (il "balsello")
        setupIdleAnimations();

        // 4. Avvio la logica di combattimento automatica
        gestisciInizioBattagliaAutomatico();
    }

    private void setupIdleAnimations() {
        idleTimelines.put(playerSprite, createIdleAnimation(playerSprite));
        idleTimelines.put(bossSprite, createIdleAnimation(bossSprite));

        idleTimelines.get(playerSprite).play();
        // Sfasiamo leggermente il boss per non farli muovere all'unisono
        Timeline bossAnim = idleTimelines.get(bossSprite);
        bossAnim.setDelay(DURATA_PASSO.divide(2));
        bossAnim.play();
    }

    private void gestisciInizioBattagliaAutomatico() {
        // Pausa iniziale di 1.5 secondi prima che succeda qualcosa
        PauseTransition pausaIniziale = new PauseTransition(Duration.seconds(1.5));

        pausaIniziale.setOnFinished(e -> {
            // Controllo di sicurezza se i modelli non sono caricati
            if (player == null || boss == null) return;

            double velPlayer = player.getVel();
            double velBoss = boss.getBossVel();

            System.out.println("Check Velocità -> Player: " + velPlayer + " vs Boss: " + velBoss);

            if (velPlayer >= velBoss) {
                System.out.println("Il PLAYER è più veloce! Attacco automatico.");
                // Il Player attacca il Boss
                eseguiAttaccoAutomatico(playerSprite, bossSprite);
            } else {
                System.out.println("Il BOSS è più veloce! Attacco automatico.");
                // Il Boss attacca il Player
                eseguiAttaccoAutomatico(bossSprite, playerSprite);
            }
        });

        pausaIniziale.play();
    }

    // Metodo semplificato: non servono più i bottoni come parametro
    private void eseguiAttaccoAutomatico(ImageView attacker, ImageView target) {
        // 1. Calcolo dinamico della distanza
        double startX = attacker.getBoundsInParent().getMinX();
        double targetX = target.getBoundsInParent().getMinX();
        double distanceX = targetX - startX;

        // 2. Pausa dell'animazione idle per evitare conflitti
        if (idleTimelines.containsKey(attacker)) {
            idleTimelines.get(attacker).pause();
        }
        attacker.setTranslateY(0); // Reset posizione Y

        // 3. Creazione del percorso di salto
        double piccoY = -150; // Altezza del salto
        Path pathAndata = new Path(new MoveTo(0,0), new QuadCurveTo(distanceX / 2, piccoY, distanceX, 0));
        Path pathRitorno = new Path(new MoveTo(distanceX, 0), new QuadCurveTo(distanceX / 2, piccoY, 0, 0));

        PathTransition andata = new PathTransition(Duration.millis(400), pathAndata, attacker);
        PathTransition ritorno = new PathTransition(Duration.millis(500), pathRitorno, attacker);
        andata.setInterpolator(Interpolator.EASE_IN);
        ritorno.setInterpolator(Interpolator.EASE_OUT);

        SequentialTransition seq = new SequentialTransition(andata, ritorno);

        // 4. Cosa succede quando l'attacco finisce
        seq.setOnFinished(e -> {
            // Resetta posizioni
            attacker.setTranslateX(0);
            attacker.setTranslateY(0);

            // Riprendi il "balsello"
            if (idleTimelines.containsKey(attacker)) {
                idleTimelines.get(attacker).play();
            }

            // QUI andrà la logica per il turno successivo!
            System.out.println("Attacco iniziale terminato. Ora toccherebbe all'altro.");
        });

        seq.play();
    }

    // Animazione sprite in stile 8-bit (DISCRETE)
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