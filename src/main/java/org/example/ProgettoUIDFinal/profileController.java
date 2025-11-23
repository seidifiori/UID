package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.io.IOException;
import java.util.prefs.Preferences;


public class profileController {

    @FXML private Canvas canvas;
    @FXML private StackPane rootStackPane;
    @FXML private GridPane mainContentPane;
    @FXML private ImageView profilePicImageView;
    @FXML private ImageView profileBannerImage;
    @FXML private Button BackButton;
    @FXML private Scene homeScene;
    @FXML private Label moneyLabel;
    @FXML private Label playerName;

    @FXML private ProgressBar xpBar;
    @FXML private ProgressBar atkBar;
    @FXML private ProgressBar defBar;
    @FXML private ProgressBar velBar;

    private String currentBannerUrl = "@images/Banner1.png";

    //Spiderchart
    private final String[] labels = {"Attacco", "Difesa", "Velocità"};

    Font minecraftFont = Font.loadFont(
            getClass().getResourceAsStream("/com/example/profile/Minecraft.ttf"), 13
    );
    @FXML
    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }

    @FXML
    public void initialize() {
        PlayerModel player = GameRepository.getInstance().getPlayer();
        loadUserBanner();
        drawSpiderChart(player);
        fillProgressBar(player);

        if(playerName != null) {
            playerName.textProperty().bind(player.playerNameProperty());
        }

        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }

        if (moneyLabel != null) {
            moneyLabel.textProperty().bind(player.goldProperty().asString());
        }
    }

    private void loadUserBanner() {
        Preferences prefs = Preferences.userNodeForPackage(profileController.class);
        String bannerToLoad = prefs.get("banner_url", currentBannerUrl);
        updateBannerPicture(bannerToLoad); // Imposta l'immagine e aggiorna il nostro campo 'currentAvatarUrl'
    }

    @FXML
    protected void handleProfilePicClick(ActionEvent event) {
        if (rootStackPane.lookup("#picChooserPane") != null) {
            System.out.println("La finestra di scelta è già aperta."); // Se esiste, significa che la finestra è già aperta.
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("profilePicChooser.fxml"));
            Parent profileView = loader.load();

            org.example.ProgettoUIDFinal.profilePicChooserController chooserController = loader.getController();

            //Passa l'URL attuale al metodo initData
            chooserController.initData(this, mainContentPane, this.currentBannerUrl); //aggiungi currentBannerUrl

            GaussianBlur blur = new GaussianBlur(10);
            mainContentPane.setEffect(blur);
            mainContentPane.setDisable(true);

            rootStackPane.getChildren().add(profileView);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateBannerPicture(String imageUrl) {
        this.currentBannerUrl = imageUrl;

        String resourceUrl = imageUrl;
        if (resourceUrl.startsWith("@")) {
            resourceUrl = resourceUrl.substring(1);
        }

        try {
            Image newPic = new Image(getClass().getResourceAsStream(resourceUrl));
            profileBannerImage.setImage(newPic);
        } catch (Exception e) {
            System.err.println("Errore nel caricare l'immagine: " + resourceUrl);
        }
    }

    private void fillProgressBar(PlayerModel player) {

        xpBar.progressProperty().bind(player.xpProperty());
        atkBar.progressProperty().bind(player.atkProperty());
        defBar.progressProperty().bind(player.defProperty());
        velBar.progressProperty().bind(player.velProperty());
    }

    private void drawSpiderChart(PlayerModel player) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;

        double[] stats = {
                player.getAtk(),
                player.getDef(),
                player.getVel()
        };

        double padding = 40;
        double radius = Math.min(width, height) / 2 - padding;
        int n = labels.length;
        double angleStep = 2 * Math.PI / n;

        gc.clearRect(0, 0, width, height);

        // 1. Griglia (Ragnatela) - Invariata
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);
        for (int i = 1; i <= 3; i++) {
            double r = radius * i / 3;
            gc.beginPath();
            for (int j = 0; j < n; j++) {
                double angle = j * angleStep;
                double x = centerX + r * Math.sin(angle);
                double y = centerY - r * Math.cos(angle);
                if (j == 0) gc.moveTo(x, y);
                else gc.lineTo(x, y);
            }
            gc.closePath();
            gc.stroke();
        }

        // 2. Poligono dei Valori
        gc.setStroke(Color.DODGERBLUE);
        gc.setFill(Color.rgb(30, 144, 255, 0.4));
        gc.setLineWidth(2);
        gc.beginPath();

        for (int i = 0; i < n; i++) {
            double val = stats[i]; // Es. 0.8

            // --- MODIFICA QUI ---
            // Se val è 0.8, non dividiamo per 100. Lo usiamo direttamente come percentuale (0.8 = 80%)
            // Aggiungiamo un controllo di sicurezza (clamp) tra 0.0 e 1.0
            if (val > 1.0) val = 1.0;
            if (val < 0.0) val = 0.0;

            double r = radius * val;
            // --------------------

            double angle = i * angleStep;
            double x = centerX + r * Math.sin(angle);
            double y = centerY - r * Math.cos(angle);

            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.closePath();
        gc.fill();
        gc.stroke();

        // 3. Etichette
        gc.setFill(Color.BLACK);
        if (minecraftFont != null) gc.setFont(minecraftFont);

        for (int i = 0; i < n; i++) {
            double angle = i * angleStep;
            double labelRadius = radius + 20;
            double x = centerX + labelRadius * Math.sin(angle);
            double y = centerY - labelRadius * Math.cos(angle);

            // --- MODIFICA QUI PER IL TESTO ---
            // Se vuoi mostrare il numero intero (es. "80") invece di "Attacco" o insieme ad esso:
            int valoreIntero = (int) (stats[i] * 100); // Trasforma 0.8 -> 80
            String testoLabel = labels[i] + " " + valoreIntero;
            // ---------------------------------

            gc.fillText(testoLabel, x - 20, y + 5);
        }
    }
    @FXML
    public void Home() {
        if (homeScene != null) {
            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        } else {
            System.err.println("⚠ Nessuna scena Home disponibile!");
        }
    }
}