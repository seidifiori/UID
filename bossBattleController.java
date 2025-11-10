package com.example.profile;

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

import java.net.URL;
import java.util.ResourceBundle;

public class bossBattleController implements Initializable {

    @FXML private ImageView sprite1, sprite2;
    @FXML private Button enemyButton, playerButton;

    private final double BALSELLO_Y = -10.0;
    private final Duration DURATA_PASSO = Duration.millis(500);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Timeline anim1 = Animazione(sprite1);
        Timeline anim2 = Animazione(sprite2);
        anim2.setDelay(DURATA_PASSO);

        anim1.play();
        anim2.play();
    }

    private void eseguiAttacco(ImageView sprite, double distanzaX, Button bottone) {
        double piccoY = -150;
        Path pathAndata = new Path(new MoveTo(0,0),
                new QuadCurveTo(distanzaX / 2, piccoY, distanzaX, 0));
        Path pathRitorno = new Path(new MoveTo(distanzaX, 0),
                new QuadCurveTo(distanzaX / 2, piccoY, 0, 0));

        PathTransition andata = new PathTransition(Duration.millis(400), pathAndata, sprite);
        PathTransition ritorno = new PathTransition(Duration.millis(500), pathRitorno, sprite);
        andata.setInterpolator(Interpolator.EASE_IN);
        ritorno.setInterpolator(Interpolator.EASE_OUT);

        SequentialTransition seq = new SequentialTransition(andata, ritorno);
        bottone.setDisable(true);
        seq.setOnFinished(e -> {
            bottone.setDisable(false);
            sprite.setTranslateX(0);
            sprite.setTranslateY(0);
        });
        seq.play();
    }

    @FXML
    void handleAttackBoss(ActionEvent e) {
        eseguiAttacco(sprite2, -450, enemyButton);
    }

    @FXML
    void handleAttackPlayer(ActionEvent e) {
        eseguiAttacco(sprite1, 650, playerButton);
    }

    // Animazione sprite
    private Timeline Animazione(Node nodo) {
        Timeline timeline = new Timeline();

        KeyFrame frameIniziale = new KeyFrame(Duration.ZERO,
                new KeyValue(nodo.translateYProperty(), 0, Interpolator.DISCRETE));
        KeyFrame frameSu = new KeyFrame(DURATA_PASSO,
                new KeyValue(nodo.translateYProperty(), BALSELLO_Y, Interpolator.DISCRETE));
        KeyFrame frameGiu = new KeyFrame(DURATA_PASSO.multiply(2),
                new KeyValue(nodo.translateYProperty(), 0, Interpolator.DISCRETE));

        timeline.getKeyFrames().addAll(frameIniziale, frameSu, frameGiu);
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }
}