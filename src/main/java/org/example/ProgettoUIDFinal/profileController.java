package org.example.ProgettoUIDFinal;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.io.IOException;
import java.util.prefs.Preferences;

public class profileController {

    @FXML private Canvas canvas;
    @FXML private StackPane rootStackPane;
    @FXML private GridPane mainContentPane;
    @FXML private ImageView profilePicImageView, profileBannerImage;
    @FXML private Button BackButton;
    @FXML private Label moneyLabel, playerName, levelLabel, DaysLabel, TaskCompletedLabel;
    @FXML private ProgressBar xpBar, atkBar, defBar, velBar;

    // Icone Equipaggiamento
    @FXML private ImageView hatIcon, hairIcon, armorIcon, swordIcon, shieldIcon;
    // Layer Avatar
    @FXML private ImageView baseAvatarLayer, hairLayer, hatLayer, armorLayer, swordLayer, shieldLayer;

    private Scene homeScene;
    private Tooltip sharedTooltip;
    private String currentBannerUrl = "@images/Banner1.png";
    private final String[] labels = {"Attacco", "Difesa", "Velocità"};

    @FXML public void setHomeScene(Scene scene) { this.homeScene = scene; }

    @FXML
    public void initialize() {
        PlayerModel player = GameRepository.getInstance().getPlayer();

        // 1. Setup Estetico e Grafico
        loadUserBanner();
        drawSpiderChart(player);
        initTooltipSystem();
        if (rootStackPane != null) StyleManager.getInstance().applyStyle(rootStackPane);

        // 2. Binding Testi e Icona Profilo
        bindText(playerName, player.playerNameProperty());
        bindText(levelLabel, player.levelProperty().asString());
        bindText(moneyLabel, player.goldProperty().asString());
        bindText(DaysLabel, player.daysNumberProperty().asString());
        bindText(TaskCompletedLabel, player.taskCompletedProperty().asString());
        if (profilePicImageView != null) profilePicImageView.imageProperty().bind(player.avatarImageProperty());

        // 3. Binding Progress Bars
        double MAX = 100.0;
        if (xpBar != null) xpBar.progressProperty().bind(player.xpProperty().divide(MAX));
        if (atkBar != null) atkBar.progressProperty().bind(player.atkProperty().divide(MAX));
        if (defBar != null) defBar.progressProperty().bind(player.defProperty().divide(MAX));
        if (velBar != null) velBar.progressProperty().bind(player.velProperty().divide(MAX));

        // 4. Binding Avatar Layers (Manichino)
        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.swordImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());
        if (hairLayer != null) hairLayer.visibleProperty().bind(player.isHairVisibleProperty());

        // 5. Binding Icone Equipaggiamento + Tooltips
        setupIcon(hatIcon, player.hatIconProperty(), player.hatNameProperty());
        setupIcon(hairIcon, player.hairIconProperty(), player.hairNameProperty());
        setupIcon(armorIcon, player.armorIconProperty(), player.armorNameProperty());
        setupIcon(swordIcon, player.swordIconProperty(), player.swordNameProperty());
        setupIcon(shieldIcon, player.shieldIconProperty(), player.shieldNameProperty());
    }

    // --- HELPER METODS (Snellimento) ---
    private void bindText(Label l, ObservableValue<String> p) { if (l != null) l.textProperty().bind(p); }

    private void bindLayer(ImageView v, ObservableValue<? extends Image> p) { if (v != null) v.imageProperty().bind(p); }

    private void setupIcon(ImageView iv, ObservableValue<Image> imgP, ObservableValue<String> nameP) {
        if (iv != null) {
            iv.imageProperty().bind(imgP);
            setupTooltip(iv, nameP);
        }
    }

    private void loadUserBanner() {
        Preferences prefs = Preferences.userNodeForPackage(profileController.class);
        updateBannerPicture(prefs.get("banner_url", currentBannerUrl));
    }

    public void updateBannerPicture(String imageUrl) {
        this.currentBannerUrl = imageUrl;
        String path = imageUrl.startsWith("@") ? imageUrl.substring(1) : imageUrl;
        try {
            Image newPic = new Image(getClass().getResourceAsStream(path));
            if (profileBannerImage != null) profileBannerImage.setImage(newPic);
        } catch (Exception e) { System.err.println("Errore caricamento banner: " + path); }
    }

    @FXML
    protected void handleProfilePicClick(ActionEvent event) {
        if (rootStackPane.lookup("#picChooserPane") != null) return;
        try {
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("profilePicChooser.fxml"));
            Parent view = loader.load();
            ((org.example.ProgettoUIDFinal.profilePicChooserController)loader.getController()).initData(this, mainContentPane, this.currentBannerUrl);
            mainContentPane.setEffect(new GaussianBlur(10));
            mainContentPane.setDisable(true);
            rootStackPane.getChildren().add(view);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void drawSpiderChart(PlayerModel player) {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        double cx = w / 2, cy = h / 2, radius = Math.min(w, h) / 2 - 40;
        double[] stats = { (double) player.getAtk(), (double) player.getDef(), (double) player.getVel() };
        double angleStep = 2 * Math.PI / 3;

        gc.clearRect(0, 0, w, h);
        gc.setStroke(Color.LIGHTGRAY);
        for (int i = 1; i <= 3; i++) {
            double r = radius * i / 3;
            gc.beginPath();
            for (int j = 0; j < 3; j++) {
                double x = cx + r * Math.sin(j * angleStep), y = cy - r * Math.cos(j * angleStep);
                if (j == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
            }
            gc.closePath(); gc.stroke();
        }

        gc.setStroke(Color.DODGERBLUE); gc.setFill(Color.rgb(30, 144, 255, 0.4));
        gc.beginPath();
        for (int i = 0; i < 3; i++) {
            double r = radius * Math.min(1.0, stats[i] / 100.0);
            double x = cx + r * Math.sin(i * angleStep), y = cy - r * Math.cos(i * angleStep);
            if (i == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
        }
        gc.closePath(); gc.fill(); gc.stroke();

        gc.setFill(Color.BLACK);
        for (int i = 0; i < 3; i++) {
            double x = cx + (radius + 25) * Math.sin(i * angleStep), y = cy - (radius + 25) * Math.cos(i * angleStep);
            gc.fillText(labels[i] + " " + (int)stats[i], x - 20, y + 5);
        }
    }

    private void initTooltipSystem() {
        sharedTooltip = new Tooltip();
        sharedTooltip.setShowDelay(Duration.ZERO);
        sharedTooltip.setHideDelay(Duration.ZERO);
        sharedTooltip.getStyleClass().add("tooltip-custom");
    }

    private void setupTooltip(ImageView target, ObservableValue<String> textProperty) {
        target.setOnMouseEntered(e -> {
            sharedTooltip.textProperty().bind(textProperty);
            sharedTooltip.show(target, e.getScreenX() + 15, e.getScreenY() + 15);
        });
        target.setOnMouseMoved(e -> {
            sharedTooltip.setX(e.getScreenX() + 15);
            sharedTooltip.setY(e.getScreenY() + 15);
        });
        target.setOnMouseExited(e -> {
            sharedTooltip.hide();
            sharedTooltip.textProperty().unbind();
        });
    }

    @FXML
    public void Home() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        if (homeScene != null) ((Stage) BackButton.getScene().getWindow()).setScene(homeScene);
    }
}