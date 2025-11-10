package com.example.profile;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.util.prefs.Preferences;

public class profileController {

    @FXML private Canvas canvas;
    @FXML private Button profilePicButt;
    @FXML private StackPane rootStackPane;
    @FXML private GridPane mainContentPane;
    @FXML private ImageView profilePicImage;
    @FXML private ImageView profileBannerImage;

    private String currentAvatarUrl = "@images/chr_icon_1052.png";
    private String currentBannerUrl = "@images/Banner1.png";

    //Spiderchart
    private final String[] labels = {"Attacco", "Difesa", "Velocità"};
    private final double[] values = {80, 60, 50}; // 0–100

    Font minecraftFont = Font.loadFont(
            getClass().getResourceAsStream("/com/example/profile/Minecraft.ttf"), 13
    );

    @FXML
    public void initialize() {
        drawSpiderChart();
        loadUserAvatar();
        loadUserBanner();
    }

    private void loadUserAvatar() {
        Preferences prefs = Preferences.userNodeForPackage(profileController.class);
        String avatarToLoad = prefs.get("avatar_url", currentAvatarUrl);
        updateProfilePicture(avatarToLoad); // Imposta l'immagine e aggiorna il nostro campo 'currentAvatarUrl'
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

            profilePicChooserController chooserController = loader.getController();

            //Passa l'URL attuale al metodo initData
            chooserController.initData(this, mainContentPane, this.currentAvatarUrl, this.currentBannerUrl); //aggiungi currentBannerUrl

            GaussianBlur blur = new GaussianBlur(10);
            mainContentPane.setEffect(blur);
            mainContentPane.setDisable(true);

            rootStackPane.getChildren().add(profileView);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void updateProfilePicture(String imageUrl) {
        this.currentAvatarUrl = imageUrl;

        String resourceUrl = imageUrl;
        if (resourceUrl.startsWith("@")) {
            resourceUrl = resourceUrl.substring(1);
        }

        try {
            Image newPic = new Image(getClass().getResourceAsStream(resourceUrl));
            profilePicImage.setImage(newPic);
        } catch (Exception e) {
            System.err.println("Errore nel caricare l'immagine: " + resourceUrl);
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



    private void drawSpiderChart() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;

        // Calcolo dinamico del raggio (evita di sforare)
        double padding = 40; // spazio per le etichette
        double radius = Math.min(width, height) / 2 - padding;

        int n = values.length;
        double angleStep = 2 * Math.PI / n;

        // Sfondo
        gc.setFill(Color.rgb(240, 240, 255, 0));
        gc.fillRect(0, 0, width, height);

        // Griglia
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

        // Poligono dei valori
        gc.setStroke(Color.DODGERBLUE);
        gc.setFill(Color.rgb(30, 144, 255, 0.4));
        gc.setLineWidth(2);
        gc.beginPath();
        for (int i = 0; i < n; i++) {
            double r = radius * (values[i] / 100.0);
            double angle = i * angleStep;
            double x = centerX + r * Math.sin(angle);
            double y = centerY - r * Math.cos(angle);
            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.closePath();
        gc.fill();
        gc.stroke();

        // Etichette
        gc.setFill(Color.BLACK);
        gc.setFont(minecraftFont);
        for (int i = 0; i < n; i++) {
            double angle = i * angleStep;
            double labelRadius = radius + 20; // spazio tra bordo e testo
            double x = centerX + labelRadius * Math.sin(angle);
            double y = centerY - labelRadius * Math.cos(angle);
            gc.fillText(labels[i], x - 20, y);
        }
    }


}
