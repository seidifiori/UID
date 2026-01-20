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
import org.example.ProgettoUIDFinal.Services.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * CONTROLLER PRINCIPALE (Home): Gestisce l'hub centrale del gioco.
 * Funge da orchestratore per il Data Binding tra il PlayerModel e la UI,
 * e gestisce lo smistamento (routing) verso le altre scene del gioco.
 */
public class HomeController implements Initializable {

    // --- ELEMENTI UI (Iniezione FXML) ---
    @FXML private StackPane rootStack;
    @FXML private BorderPane rootPane;
    @FXML private Button shopButton;
    @FXML private Label moneyLabel, playerName, levelLabel;
    @FXML private ImageView backgroundImageView, profilePicImageView;
    @FXML private ProgressBar xpBar;

    // Layer dell'avatar per l'anteprima in tempo reale nella dashboard
    @FXML private ImageView baseAvatarLayer, hairLayer, hatLayer, armorLayer, swordLayer, shieldLayer;

    /**
     * INIZIALIZZAZIONE: Configura i legami (binding) all'avvio della scena.
     * In JavaFX, il binding permette alla UI di reagire ai cambiamenti del modello
     * senza dover scrivere codice di aggiornamento manuale.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PlayerModel player = GameRepository.getInstance().getPlayer();

        // Utilizza lo StyleManager (Singleton) per il font-size globale
        Region mainRoot = (rootStack != null) ? rootStack : rootPane;
        StyleManager.getInstance().applyStyle(mainRoot);

        // UI BINDING: Collega le etichette alle Properties del giocatore
        moneyLabel.textProperty().bind(player.goldProperty().asString());
        levelLabel.textProperty().bind(player.levelProperty().asString());
        playerName.textProperty().bind(player.playerNameProperty());

        // Calcolo percentuale XP per la ProgressBar (diviso 100.0)
        xpBar.progressProperty().bind(player.xpProperty().divide(100.0));
        profilePicImageView.imageProperty().bind(player.avatarImageProperty());

        // BINDING DEI LAYER GRAFICI: Sovrapposizione dinamica degli sprite
        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.swordImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());

        // Logica di visibilità condizionale (capelli nascosti da elmi)
        if (hairLayer != null) {
            hairLayer.visibleProperty().bind(player.isHairVisibleProperty());
        }

        // GESTIONE BACKGROUND DINAMICO
        Image started = BackgroundService.getInstance().getBackground();
        if (started != null) {
            applyBackground(mainRoot, started);
            applyBackground(backgroundImageView, started);
        }
        javafx.application.Platform.runLater(() -> {
            if (rootStack != null) {
                rootStack.requestFocus();
            }
        });


        // Observer sullo sfondo: aggiorna la Home se lo sfondo cambia in altre scene
        BackgroundService.getInstance().backgroundProperty().addListener((obs, oldImg, newImg) -> {
            if (newImg != null) {
                applyBackground(mainRoot, newImg);
                applyBackground(backgroundImageView, newImg);
            }
        });
    }

    /**
     * Helper per legare una ImageView a una proprietà immagine osservabile.
     */
    private void bindLayer(ImageView view, javafx.beans.value.ObservableValue<? extends Image> prop) {
        if (view != null) view.imageProperty().bind(prop);
    }


    //  LOGICA DI NAVIGAZIONE (Scene Switching)

    /**
     * Esegue lo switch verso la scena dello SHOP.
     * Utilizza la Reflection per passare la scena corrente al nuovo controller
     * permettendo il ritorno alla Home (Back Navigation).
     */
    @FXML
    public void showShop(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Shop.fxml"));
        Parent shopRoot = loader.load();

        Object controller = loader.getController();
        try {
            // Passaggio della scena attuale al controller dello Shop
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

        ProfileController pc = loader.getController();
        StyleManager.getInstance().applyStyle((Region) profileRoot);

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        pc.setHomeScene(currentStage.getScene());
        currentStage.setScene(new Scene(profileRoot));
    }

    /**
     * Carica e visualizza la scena SETTINGS, iniettando i fogli di stile CSS
     * necessari per la corretta visualizzazione dei controlli.
     */
    @FXML
    public void showSettings(ActionEvent event) {
        try {
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ProgettoUIDFinal/Settings.fxml"));
            Parent settingsRoot = loader.load();

            SettingsController sc = loader.getController();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene currentScene = currentStage.getScene();

            sc.setHomeScene(currentScene);
            Scene newScene = new Scene(settingsRoot, currentScene.getWidth(), currentScene.getHeight());

            // Caricamento dei fogli di stile esterni (style.css)
            URL cssUrl = getClass().getResource("/org/example/ProgettoUIDFinal/style.css");
            if (cssUrl != null) {
                newScene.getStylesheets().add(cssUrl.toExternalForm());
            }

            StyleManager.getInstance().applyStyle(newScene);
            currentStage.setScene(newScene);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void showBoss(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("boss.fxml"));
        Parent bossRoot = loader.load();

        BossController bc = loader.getController();
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

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        Scene taskScene = new Scene(tasksRoot);
        StyleManager.getInstance().applyStyle(taskScene);
        currentStage.setScene(taskScene);
    }


    //  METODI DI RENDERING BACKGROUND
    /**
     * Applica un'immagine di sfondo a un contenitore Region
     * Utilizza BackgroundSize per assicurare che lo sfondo copra l'intera area (Cover).
     */
    private void applyBackground(Region region, Image image) {
        if (region == null || image == null) return;
        BackgroundSize bs = new BackgroundSize(1.0, 1.0, true, true, false, true);
        BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs);
        region.setBackground(new Background(bi));
    }

    private void applyBackground(ImageView imageView, Image image) {
        if (imageView != null && image != null) imageView.setImage(image);
    }
}