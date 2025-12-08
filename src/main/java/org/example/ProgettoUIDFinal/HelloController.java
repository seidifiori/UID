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

    // --- NUOVI LAYERS (Corretti) ---
    @FXML private ImageView baseAvatarLayer;
    @FXML private ImageView hairLayer;
    @FXML private ImageView hatLayer;
    @FXML private ImageView armorLayer;
    @FXML private ImageView swordLayer;
    @FXML private ImageView shieldLayer;

    // Ho rimosso HatImage e DressImage. Sono obsoleti. Non piangere.

    private Scene previousScene;
    private HelloApplication mainApp;
    private Stage primaryStage;

    public void setMainApp(HelloApplication mainApp) { this.mainApp = mainApp; }
    public void setPrimaryStage(Stage primaryStage) { this.primaryStage = primaryStage; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PlayerModel player = GameRepository.getInstance().getPlayer();

        // Gestione stili
        if (rootStack != null) {
            StyleManager.getInstance().applyStyle(rootStack);
        } else if (rootPane != null) {
            StyleManager.getInstance().applyStyle(rootPane);
        }

        // Binding Testi e Barre
        if (moneyLabel != null) {
            moneyLabel.textProperty().bind(player.goldProperty().asString());
        }
        if (playerName != null) {
            playerName.textProperty().bind(player.playerNameProperty());
        }
        if (xpBar != null) {
            xpBar.progressProperty().bind(player.xpProperty().divide(100.0));
        }

        // Binding Icona Profilo (quella piccola in alto)
        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }

        // --- BINDING DEI LAYER VISIVI (Avatar Centrale) ---
        // Questo chiama il metodo definito SOTTO, non dentro.
        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.swordImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());

        // Gestione Background
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

    // --- METODO SPOSTATO FUORI DA INITIALIZE (Dove dovrebbe stare) ---
    private void bindLayer(ImageView view, javafx.beans.value.ObservableValue<? extends javafx.scene.image.Image> prop) {
        if (view != null) {
            view.imageProperty().bind(prop);
        } else {
            // Questo errore apparirà se nel FXML hai dimenticato fx:id="hatLayer" ecc.
            System.err.println("GLaDOS: Attenzione. Un layer grafico manca nel FXML ma è richiesto dal codice.");
        }
    }




    @FXML
    public void showShop(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Shop.fxml"));
        Parent shopRoot = loader.load();

        // Reflection per passare la scena (metodo barbaro ma funzionale)
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

        // Gestione CSS
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
    @FXML
    public void showTask(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ProgettoUIDFinal/Tasks.fxml"));
        Parent tasksRoot = loader.load();

        // Get the controller and set the home scene
        TaskController tasksController = loader.getController();
        tasksController.setHomeScene(shopButton.getScene());

        // Set up the initial view
        tasksController.setBackButtonVisible(false);
        tasksController.setDailyTasksButtonVisible(true);

        // Create and show the scene
        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        Scene taskScene = new Scene(tasksRoot);
        currentStage.setScene(taskScene);
        StyleManager.getInstance().applyStyle(taskScene);
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