package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    @FXML private StackPane rootStack;
    @FXML private BorderPane rootPane;
    @FXML private Button shopButton;
    @FXML private Label moneyLabel;
    @FXML private Label playerName;
    @FXML private ImageView backgroundImageView;
    @FXML private ImageView profilePicImageView;
    @FXML private ProgressBar xpBar;

    @FXML private ImageView HatImage;
    @FXML private ImageView DressImage;

    private Scene previousScene;
    private HelloApplication mainApp;
    private Stage primaryStage;

    public void setMainApp(HelloApplication mainApp) { this.mainApp = mainApp; }
    public void setPrimaryStage(Stage primaryStage) { this.primaryStage = primaryStage; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PlayerModel player = GameRepository.getInstance().getPlayer();

        if (rootStack != null) {
            StyleManager.getInstance().applyStyle(rootStack);
        } else if (rootPane != null) {
            StyleManager.getInstance().applyStyle(rootPane);
        }

        if (moneyLabel != null) {
            moneyLabel.textProperty().bind(player.goldProperty().asString());
        }

        if (playerName != null) {
            playerName.textProperty().bind(player.playerNameProperty());
        }
        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }

        if (xpBar != null) {
            xpBar.progressProperty().bind(player.xpProperty().divide(100.0));
        }

        if (HatImage != null) {
            HatImage.imageProperty().bind(player.hatImageProperty());
        } else {
            System.err.println("HelloController: HatImage è null! Controlla fx:id nel FXML.");
        }

        if (DressImage != null) {
            DressImage.imageProperty().bind(player.armorImageProperty());
        }

        Image started = BackgroundService.getInstance().getBackground();
        if (started != null) {
            applyBackground(rootStack != null ? rootStack : rootPane, started);
            applyBackground(backgroundImageView, started);
        }

        BackgroundService.getInstance().backgroundProperty().addListener((obs, oldImg, newImg) -> {
            if (newImg != null) {
                Region target = (rootStack != null) ? rootStack : rootPane;
                applyBackground(target, newImg);
                applyBackground(backgroundImageView, newImg);
            }
        });
    }

    @FXML
    private void showAddTaskView(ActionEvent event) {
        try { if (mainApp != null) mainApp.showAddTaskView(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void showShop(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Shop.fxml"));
        Parent shopRoot = loader.load();
        Object controller = loader.getController();
        try {
            controller.getClass().getMethod("setHomeScene", Scene.class)
                    .invoke(controller, shopButton.getScene());
        } catch(Exception e) { }
        StyleManager.getInstance().applyStyle((Region) shopRoot);

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        currentStage.setScene(new Scene(shopRoot));

    }

    public void showProfile(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
        Parent profileRoot = loader.load();
        org.example.ProgettoUIDFinal.profileController pc = loader.getController();
        StyleManager.getInstance().applyStyle((Region) profileRoot);
        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        pc.setHomeScene(currentStage.getScene());
        currentStage.setScene(new Scene(profileRoot));

    }
    public void showSettings(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("settings.fxml"));
        Parent settingsRoot = loader.load();
        SettingsController sc = loader.getController();
        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        Scene currentScene = currentStage.getScene();
        sc.setHomeScene(currentScene);
        Scene newScene = new Scene(settingsRoot, currentScene.getWidth(), currentScene.getHeight());
        String cssPath = getClass().getResource("style.css").toExternalForm();
        newScene.getStylesheets().add(cssPath);
        StyleManager.getInstance().applyStyle(newScene);
        currentStage.setScene(newScene);
    }

    public void showBoss(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("boss.fxml"));
        Parent bossRoot = loader.load();
        org.example.ProgettoUIDFinal.bossController bc = loader.getController();
        StyleManager.getInstance().applyStyle((Region) bossRoot);

        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        bc.setHomeScene(currentStage.getScene());
        currentStage.setScene(new Scene(bossRoot));
    }

    @FXML
    public void showCloset(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ProgettoUIDFinal/Closet.fxml"));
        Parent closetRoot = loader.load();

        ClosetController closetController = loader.getController();

        StyleManager.getInstance().applyStyle((Region) closetRoot);

        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());

        if (closetController != null) {
            closetController.setPreviousScene(currentStage.getScene());
        }
        currentStage.setScene(new Scene(closetRoot));
    }

    private void applyBackground(Region region, Image image) {
        if (region == null || image == null) return;
        BackgroundSize bs = new BackgroundSize(1.0, 1.0, true, true, false, true);
        BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs);
        region.setBackground(new Background(bi));
    }

    private void applyBackground(ImageView imageView, Image image) {
        if (imageView != null && image != null) imageView.setImage(image);
    }
}