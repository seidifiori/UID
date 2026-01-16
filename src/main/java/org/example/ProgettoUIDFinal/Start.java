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

        stage.setTitle("WellQuest");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} /**/