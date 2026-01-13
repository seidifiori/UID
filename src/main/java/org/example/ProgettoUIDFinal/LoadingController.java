package org.example.ProgettoUIDFinal;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;
import org.example.ProgettoUIDFinal.Services.GameRepository;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class LoadingController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private VBox loadingBox;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    @FXML private VBox newUserBox;
    @FXML private TextField usernameField;
    @FXML private Button confirmButton;
    @FXML private Label errorLabel;

    // Variabili per gestire l'animazione
    private double progress = 0.0;
    private Timeline timeline;

    // Frasi casuali per rendere il caricamento più divertente
    private final String[] loadingMessages = {
            "Caricamento texture...",
            "Generazione mondo...",
            "Lucidando le spade...",
            "Nutrendo i boss...",
            "Contando le monete...",
            "Invocando la magia...",
            "Preparando lo shop..."
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StyleManager.getInstance().applyStyle(rootPane);
        confirmButton.setDefaultButton(true);
        MusicManager.getInstance().playSoundEffect("Game_opening.mp3");
        boolean saveExists = GameRepository.getInstance().hasSaveFile();

        if (saveExists) {
            showLoadingView();
            startFakeLoading();
        } else {
            showNewUserView();
        }
    }

    private void showLoadingView() {
        loadingBox.setVisible(true);
        newUserBox.setVisible(false);
    }

    private void showNewUserView() {
        MusicManager.getInstance().playMusic("loadingmusic.mp3");
        loadingBox.setVisible(false);
        newUserBox.setVisible(true);
    }

    @FXML
    private void handleCreateUser() {
        String name = usernameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setVisible(true);
            return;
        }
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        GameRepository.getInstance().createNewUser(name);
        showLoadingView();
        startFakeLoading();
    }

    /**
     * Simula un caricamento realistico incrementando la barra
     * di una quantità casuale ad intervalli regolari.
     */
    private void startFakeLoading() {
        // Resetta la barra
        progress = 0.0;
        progressBar.setProgress(0);
        statusLabel.setText("Avvio sistema...");

        // Creiamo una Timeline che scatta ogni 0.2 secondi (per fluidità)
        // Se vuoi che scatti esattamente ogni secondo, cambia in Duration.seconds(1)
        timeline = new Timeline(new KeyFrame(Duration.seconds(0.2), event -> {

            // 1. Genera un incremento casuale tra 1% e 15% (0.01 - 0.15)
            double randomJump = Math.random() * 0.10;

            // A volte facciamo un salto più grosso per simulare un caricamento veloce
            if (Math.random() > 0.1) randomJump += 0.1;

            progress += randomJump;

            // 2. Aggiorna la grafica
            progressBar.setProgress(progress);

            // Cambia la scritta casualmente ogni tanto (30% di probabilità ad ogni tick)
            if (Math.random() < 0.5) {
                int randomIndex = new Random().nextInt(loadingMessages.length);
                statusLabel.setText(loadingMessages[randomIndex]);
            }

            // 3. Controllo fine caricamento
            if (progress >= 1.0) {
                timeline.stop(); // Ferma il loop
                progressBar.setProgress(1.0); // Assicura che sia piena visivamente
                statusLabel.setText("Pronto!");

                // Ritardo piccolissimo finale prima di cambiare scena
                goToHome();
            }
        }));

        // Imposta il ciclo su INDEFINITE finché non lo fermiamo noi manualmente
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Home.fxml"));
            MusicManager.getInstance().playMusic("background_music.mp3");
            StackPane homeRoot = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(homeRoot, 1080, 650);

            URL cssUrl = getClass().getResource("/org/example/ProgettoUIDFinal/imagini/pixel-shop.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Errore critico nel caricamento!");
        }
    }
}