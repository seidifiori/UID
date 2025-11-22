package org.example.ProgettoUIDFinal;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    @FXML private javafx.scene.control.Label soldiLabel;
    @FXML private ImageView backgroundImageView;
    @FXML private ImageView profilePicImageView;

    // Questo è l'ImageView CHE STA NELLA HOME (quello del personaggio piccolo)
    @FXML private ImageView helloViewHatImage;

    private Scene previousScene;
    private HelloApplication mainApp;
    private Stage primaryStage;

    public void setMainApp(HelloApplication mainApp) { this.mainApp = mainApp; }
    public void setPrimaryStage(Stage primaryStage) { this.primaryStage = primaryStage; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Binding Soldi
        PlayerModel player = GameRepository.getInstance().getPlayer();
        if (soldiLabel != null) {
            soldiLabel.textProperty().bind(player.goldProperty().asString());
        }

        System.out.println("HOME vede il Player ID: " + System.identityHashCode(player));
        System.out.println("HOME vede immagine: " + player.avatarImageProperty());

        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }

        // 2. Gestione Background
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
        try {
            if (mainApp != null) mainApp.showAddTaskView();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void showShop(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Shop.fxml"));
        Parent shopRoot = loader.load();

        // ShopController deve implementare Initializable per gestire i dati da solo
        // Se ShopController ha setHomeScene:
        Object controller = loader.getController();
        try {
            controller.getClass().getMethod("setHomeScene", Scene.class)
                    .invoke(controller, shopButton.getScene());
        } catch(Exception e) { /* ignora se non c'è il metodo */ }

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        currentStage.setScene(new Scene(shopRoot));
    }

    public void showProfile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
        Parent profileRoot = loader.load();
        org.example.ProgettoUIDFinal.profileController pc = loader.getController();

        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        pc.setHomeScene(currentStage.getScene());
        currentStage.setScene(new Scene(profileRoot));
    }

    public void showBoss(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("boss.fxml"));
        Parent bossRoot = loader.load();
        org.example.ProgettoUIDFinal.bossController bc = loader.getController();

        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        bc.setHomeScene(currentStage.getScene());
        currentStage.setScene(new Scene(bossRoot));
    }

    // === NUOVA VERSIONE DI SHOW CLOSET ===
    @FXML
    public void showCloset(ActionEvent event) throws IOException {
        // 1. Carica il FXML (che ora usa ClosetController specificato nell'FXML)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ProgettoUIDFinal/Closet.fxml"));

        // NON chiamare loader.setController(this)! Lascia che usi ClosetController.
        Parent closetRoot = loader.load();

        // 2. Prendi il controller appena creato
        ClosetController closetController = loader.getController();

        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());

        // 3. Passa i dati necessari al nuovo controller
        if (closetController != null) {
            // Passiamo la scena corrente per poter tornare indietro
            closetController.setPreviousScene(currentStage.getScene());

            // Passiamo il riferimento all'immagine del cappello della HOME
            // così quando l'utente cambia cappello nel closet, si aggiorna anche qui
            closetController.setHomeHatImage(this.helloViewHatImage);
        }

        // 4. Cambia scena
        currentStage.setScene(new Scene(closetRoot));
    }

    // Metodo helper per lo sfondo della Home
    private void applyBackground(Region region, Image image) {
        if (region == null || image == null) return;
        BackgroundSize bs = new BackgroundSize(1.0, 1.0, true, true, false, true);
        BackgroundImage bi = new BackgroundImage(image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                bs);
        region.setBackground(new Background(bi));
    }

    private void applyBackground(ImageView imageView, Image image) {
        if (imageView != null && image != null) imageView.setImage(image);
    }
}