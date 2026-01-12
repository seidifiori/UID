package org.example.ProgettoUIDFinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import java.net.URL;

/**
 * ENTRY POINT: Classe principale che avvia il ciclo di vita dell'applicazione JavaFX.
 * Estende 'Application' e funge da punto di ingresso per il caricamento dello Stage
 * (la finestra) e della prima Scene (il contenuto).
 */
public class Start extends Application {

    /**
     * Metodo di avvio del thread grafico.
     * Gestisce il bootstrapping iniziale: caricamento FXML, iniezione CSS e avvio musica.
     */
    @Override
    public void start(Stage stage) throws Exception {
        // FXMLLoader: Carica la gerarchia dei nodi definiti nel file FXML
        FXMLLoader fxmlLoader = new FXMLLoader(Start.class.getResource("Home.fxml"));
        StackPane root = fxmlLoader.load();

        // SCENE: Definisce le dimensioni della finestra di gioco (1080x650)
        Scene scene = new Scene(root, 1080, 650);

        /**
         * GLOBAL CSS: Caricamento del foglio di stile principale dal classpath.
         * Utilizzato per definire l'estetica coerente (Pixel Art Shop) in tutta l'applicazione.
         */
        URL cssUrl = Start.class.getResource("/org/example/ProgettoUIDFinal/imagini/pixel-shop.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // INITIAL SERVICES: Avvio della colonna sonora tramite il Singleton MusicManager
        MusicManager.getInstance().playMusic("background_music.mp3");

        // CONFIGURAZIONE STAGE (Window): Imposta titolo, scena e blocca il ridimensionamento
        stage.setTitle("Home");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show(); // Rende visibile la finestra all'utente
    }

    /**
     * Metodo main standard Java.
     * Esegue il lancio interno del framework JavaFX tramite il metodo statico launch().
     */
    public static void main(String[] args) {
        launch(args);
    }
}