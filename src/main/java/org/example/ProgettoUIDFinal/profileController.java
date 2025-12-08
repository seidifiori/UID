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

    @FXML private ImageView hatIcon;
    @FXML private ImageView hairIcon;
    @FXML private ImageView armorIcon;
    @FXML private ImageView swordIcon;
    @FXML private ImageView shieldIcon;

    @FXML private ImageView baseAvatarLayer;
    @FXML private ImageView hairLayer;
    @FXML private ImageView hatLayer;
    @FXML private ImageView armorLayer;
    @FXML private ImageView swordLayer;
    @FXML private ImageView shieldLayer;

    private String currentBannerUrl = "@images/Banner1.png";

    // Spiderchart
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

        if (rootStackPane != null) {
            StyleManager.getInstance().applyStyle(rootStackPane);
        }

        // Binding Testi
        if(playerName != null) {
            playerName.textProperty().bind(player.playerNameProperty());
        }
        if (moneyLabel != null) {
            moneyLabel.textProperty().bind(player.goldProperty().asString());
        }

        // Binding Icona Tonda (Profilo)
        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }

        // --- BINDING AVATAR COMPLETO (Il manichino) ---
        // Questo farà vedere i vestiti attuali anche nel profilo
        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.swordImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());

        bindLayer(hatIcon, player.hatIconProperty());   // helmetIcon -> Hat
        bindLayer(hairIcon,   player.hairIconProperty());  // hairIcon   -> Hair
        bindLayer(armorIcon,  player.armorIconProperty()); // armorIcon  -> Armor
        bindLayer(swordIcon,  player.swordIconProperty());// swordIcon  -> Weapon
        bindLayer(shieldIcon, player.shieldIconProperty());// shieldIcon -> Shield

        if (hairLayer != null) {
            hairLayer.visibleProperty().bind(player.isHairVisibleProperty());
        }
    }

    // Helper per collegare le immagini in sicurezza
    private void bindLayer(ImageView view, javafx.beans.value.ObservableValue<? extends Image> prop) {
        if (view != null) {
            view.imageProperty().bind(prop);
        }
    }

    private void loadUserBanner() {
        Preferences prefs = Preferences.userNodeForPackage(profileController.class);
        String bannerToLoad = prefs.get("banner_url", currentBannerUrl);
        updateBannerPicture(bannerToLoad);
    }

    @FXML
    protected void handleProfilePicClick(ActionEvent event) {
        if (rootStackPane.lookup("#picChooserPane") != null) {
            System.out.println("La finestra di scelta è già aperta.");
            return;
        }

        try {
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("profilePicChooser.fxml"));
            Parent profileView = loader.load();

            org.example.ProgettoUIDFinal.profilePicChooserController chooserController = loader.getController();
            chooserController.initData(this, mainContentPane, this.currentBannerUrl);

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
            if (profileBannerImage != null) profileBannerImage.setImage(newPic);
        } catch (Exception e) {
            System.err.println("Errore nel caricare l'immagine: " + resourceUrl);
        }
    }

    private void fillProgressBar(PlayerModel player) {
        double MAX_STAT = 100.0;
        if (xpBar != null) xpBar.progressProperty().bind(player.xpProperty().divide(MAX_STAT));
        if (atkBar != null) atkBar.progressProperty().bind(player.atkProperty().divide(MAX_STAT));
        if (defBar != null) defBar.progressProperty().bind(player.defProperty().divide(MAX_STAT));
        if (velBar != null) velBar.progressProperty().bind(player.velProperty().divide(MAX_STAT));
    }

    private void drawSpiderChart(PlayerModel player) {
        if (canvas == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;

        final double MAX_STAT = 100.0;

        double[] stats = {
                (double) player.getAtk(),
                (double) player.getDef(),
                (double) player.getVel()
        };

        int n = (labels != null) ? labels.length : stats.length;
        double padding = 40;
        double radius = Math.min(width, height) / 2 - padding;
        double angleStep = 2 * Math.PI / n;

        gc.clearRect(0, 0, width, height);

        // 2. DISEGNO GRIGLIA
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

        // 3. DISEGNO POLIGONO VALORI
        gc.setStroke(Color.DODGERBLUE);
        gc.setFill(Color.rgb(30, 144, 255, 0.4));
        gc.setLineWidth(2);
        gc.beginPath();

        for (int i = 0; i < n; i++) {
            double rawVal = stats[i];
            double percentage = rawVal / MAX_STAT;
            if (percentage > 1.0) percentage = 1.0;
            if (percentage < 0.0) percentage = 0.0;

            double r = radius * percentage;
            double angle = i * angleStep;
            double x = centerX + r * Math.sin(angle);
            double y = centerY - r * Math.cos(angle);

            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.closePath();
        gc.fill();
        gc.stroke();

        // 4. ETICHETTE
        gc.setFill(Color.BLACK);
        for (int i = 0; i < n; i++) {
            double angle = i * angleStep;
            double labelRadius = radius + 25;
            double x = centerX + labelRadius * Math.sin(angle);
            double y = centerY - labelRadius * Math.cos(angle);

            int valIntero = (int) stats[i];
            String labelName = (labels != null && i < labels.length) ? labels[i] : "";
            String testoLabel = labelName + " " + valIntero;

            gc.fillText(testoLabel, x - 20, y + 5);
        }
    }

    @FXML
    public void Home() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        if (homeScene != null) {
            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        } else {
            System.err.println("⚠ Nessuna scena Home disponibile!");
        }
    }
}