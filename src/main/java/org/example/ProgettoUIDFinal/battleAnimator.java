package org.example.ProgettoUIDFinal;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.shape.*;
import javafx.util.Duration;

public class battleAnimator {

    /**
     * Gestisce l'animazione del salto, l'impatto e il ritorno.
     * @param attacker Chi attacca
     * @param target Chi viene colpito
     * @param onHit Codice da eseguire ESATTAMENTE quando avviene l'impatto (es. togliere HP)
     * @param onFinish Codice da eseguire quando l'attaccante è tornato al posto (es. passare il turno)
     */
    public static void eseguiSaltoAttacco(Node attacker, Node target, Runnable onHit, Runnable onFinish) {

        // 1. Calcolo distanze
        double startX = attacker.getBoundsInParent().getMinX();
        double targetX = target.getBoundsInParent().getMinX();
        double distanceX = targetX - startX;

        // Altezza del salto (negativa perché in JavaFX Y va verso il basso)
        double piccoY = -150;

        // 2. Definizione Percorsi (Andata e Ritorno)
        Path pathAndata = new Path(new MoveTo(0,0), new QuadCurveTo(distanceX / 2, piccoY, distanceX, 0));
        Path pathRitorno = new Path(new MoveTo(distanceX, 0), new QuadCurveTo(distanceX / 2, piccoY, 0, 0));

        // 3. Transizioni
        PathTransition andata = new PathTransition(Duration.millis(400), pathAndata, attacker);
        PathTransition ritorno = new PathTransition(Duration.millis(500), pathRitorno, attacker);

        andata.setInterpolator(Interpolator.EASE_IN);  // Accelera mentre scende
        ritorno.setInterpolator(Interpolator.EASE_OUT); // Rallenta mentre atterra

        // 4. Gestione dell'impatto
        andata.setOnFinished(e -> {
            if (onHit != null) onHit.run(); // Esegue il danno qui!
        });

        // 5. Sequenza completa
        SequentialTransition seq = new SequentialTransition(andata, ritorno);

        seq.setOnFinished(e -> {
            // Reset di sicurezza
            attacker.setTranslateX(0);
            attacker.setTranslateY(0);

            if (onFinish != null) onFinish.run(); // Passa il turno qui!
        });

        seq.play();
    }

    // Metodo extra per far lampeggiare chi viene colpito
    public static void playHitEffect(Node target) {
        FadeTransition ft = new FadeTransition(Duration.millis(100), target);
        ft.setFromValue(1.0);
        ft.setToValue(0.5);
        ft.setCycleCount(4);
        ft.setAutoReverse(true);
        ft.play();
    }
}