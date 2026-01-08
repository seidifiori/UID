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
        FXMLLoader fxmlLoader = new FXMLLoader(Start.class.getResource("Home.fxml"));
        StackPane root = fxmlLoader.load();

        Scene scene = new Scene(root, 1080, 650);

        // Caricamento CSS Globale
        URL cssUrl = Start.class.getResource("/org/example/ProgettoUIDFinal/css/pixel-shop.css");
        if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());

        MusicManager.getInstance().playMusic("background_music.mp3");

        stage.setTitle("Home");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}