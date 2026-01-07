package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.Services.BackgroundService;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML private StackPane rootStack;
    @FXML private BorderPane rootPane;
    @FXML private Button shopButton;
    @FXML private Label moneyLabel, playerName, levelLabel;
    @FXML private ImageView backgroundImageView, profilePicImageView;
    @FXML private ProgressBar xpBar;

    @FXML private ImageView baseAvatarLayer, hairLayer, hatLayer, armorLayer, swordLayer, shieldLayer;

    // ELIMINATE: previousScene, mainApp, primaryStage e i relativi SETTER

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PlayerModel player = GameRepository.getInstance().getPlayer();

        // Applica lo stile al contenitore principale
        Region mainRoot = (rootStack != null) ? rootStack : rootPane;
        StyleManager.getInstance().applyStyle(mainRoot);

        // Binding Testi e Barre
        moneyLabel.textProperty().bind(player.goldProperty().asString());
        levelLabel.textProperty().bind(player.levelProperty().asString());
        playerName.textProperty().bind(player.playerNameProperty());
        xpBar.progressProperty().bind(player.xpProperty().divide(100.0));
        profilePicImageView.imageProperty().bind(player.avatarImageProperty());

        // Binding Layer Avatar
        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.swordImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());

        if (hairLayer != null) {
            hairLayer.visibleProperty().bind(player.isHairVisibleProperty());
        }

        // Gestione Background
        Image started = BackgroundService.getInstance().getBackground();
        if (started != null) {
            applyBackground(mainRoot, started);
            applyBackground(backgroundImageView, started);
        }

        BackgroundService.getInstance().backgroundProperty().addListener((obs, oldImg, newImg) -> {
            if (newImg != null) {
                applyBackground(mainRoot, newImg);
                applyBackground(backgroundImageView, newImg);
            }
        });
    }

    private void bindLayer(ImageView view, javafx.beans.value.ObservableValue<? extends Image> prop) {
        if (view != null) view.imageProperty().bind(prop);
    }

    // --- NAVIGAZIONE (Ripristinata come l'originale ma senza variabili esterne) ---

    @FXML
    public void showShop(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Shop.fxml"));
        Parent shopRoot = loader.load();

        // Passaggio scena tramite reflection (come avevi tu)
        Object controller = loader.getController();
        try {
            controller.getClass().getMethod("setHomeScene", Scene.class)
                    .invoke(controller, shopButton.getScene());
        } catch(Exception e) { }

        StyleManager.getInstance().applyStyle((Region) shopRoot);
        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        currentStage.setScene(new Scene(shopRoot));
    }

    @FXML
    public void showProfile(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
        Parent profileRoot = loader.load();

        org.example.ProgettoUIDFinal.profileController pc = loader.getController();
        StyleManager.getInstance().applyStyle((Region) profileRoot);

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        pc.setHomeScene(currentStage.getScene());
        currentStage.setScene(new Scene(profileRoot));
    }

    @FXML
    public void showSettings(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Settings.fxml"));
        Parent settingsRoot = loader.load();

        SettingsController sc = loader.getController();
        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        Scene currentScene = currentStage.getScene();

        sc.setHomeScene(currentScene);
        Scene newScene = new Scene(settingsRoot, currentScene.getWidth(), currentScene.getHeight());

        String cssPath = getClass().getResource("style.css").toExternalForm();
        newScene.getStylesheets().add(cssPath);
        StyleManager.getInstance().applyStyle(newScene);

        currentStage.setScene(newScene);
    }

    @FXML
    public void showBoss(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("boss.fxml"));
        Parent bossRoot = loader.load();

        org.example.ProgettoUIDFinal.bossController bc = loader.getController();
        StyleManager.getInstance().applyStyle((Region) bossRoot);

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
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

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        if (closetController != null) {
            closetController.setPreviousScene(currentStage.getScene());
        }
        currentStage.setScene(new Scene(closetRoot));
    }

    @FXML
    public void showTask(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ProgettoUIDFinal/Tasks.fxml"));
        Parent tasksRoot = loader.load();

        TaskController tasksController = loader.getController();
        tasksController.setHomeScene(shopButton.getScene());
        tasksController.setBackButtonVisible(false);
        tasksController.setDailyTasksButtonVisible(true);

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        Scene taskScene = new Scene(tasksRoot);
        StyleManager.getInstance().applyStyle(taskScene);
        currentStage.setScene(taskScene);
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