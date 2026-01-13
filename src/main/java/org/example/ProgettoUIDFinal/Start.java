package org.example.ProgettoUIDFinal;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import java.net.URL;

public class Start extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // MODIFICA: Carichiamo LoadingScreen invece di Home
        FXMLLoader fxmlLoader = new FXMLLoader(Start.class.getResource("LoadingScreen.fxml"));
        StackPane root = fxmlLoader.load();

        Scene scene = new Scene(root, 1080, 650);

        // GLOBAL CSS (opzionale qui, ma utile per i font)
        URL cssUrl = Start.class.getResource("/org/example/ProgettoUIDFinal/imagini/pixel-shop.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Avvio musica (opzionale, magari vuoi musica diversa nel loading)
        MusicManager.getInstance().playSoundEffect("Game_opening.mp3");

        stage.setTitle("Caricamento...");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}