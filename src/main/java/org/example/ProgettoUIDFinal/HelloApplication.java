package org.example.ProgettoUIDFinal;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private Stage primaryStage;
    private HomeController mainController;



    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("Home.fxml"));
        StackPane root = fxmlLoader.load(); // Root is StackPane (for background)

        mainController = fxmlLoader.getController();
        mainController.setMainApp(this);
        mainController.setPrimaryStage(stage);

        Scene scene = new Scene(root, 1080, 650);
        MusicManager.getInstance().playMusic("background_music.mp3");

        // --- CARICAMENTO STYLESHEET GLOBALE ---
        // Uso percorso assoluto nella resources (consigliato)
        URL cssUrl = HelloApplication.class.getResource("/org/example/ProgettoUIDFinal/pixel-shop.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("ATTENZIONE: pixel-shop.css non trovato in /org/example/uididididii/ (controlla src/main/resources).");
        }

        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());

        // Scaling based on scene size
        double baseWidth = 1080.0;
        double baseHeight = 650.0;

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double scale = newVal.doubleValue() / baseWidth;
            root.setScaleX(scale);
            root.setScaleY(scale);
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double scale = newVal.doubleValue() / baseHeight;
            root.setScaleX(scale);
            root.setScaleY(scale);
        });

        stage.setTitle("Home");
        stage.setScene(scene);
        stage.setFullScreen(false);
        stage.setResizable(false);
        stage.show();
    }


    public HomeController getMainController() {
        return this.mainController;
    }



    public static void main(String[] args) {
        launch(args);
    }
}


