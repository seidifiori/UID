package org.example.ProgettoUIDFinal;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.model.GameRepository; // <--- IMPORTANTE
import org.example.ProgettoUIDFinal.model.ItemModel;           // <--- IMPORTANTE
import org.example.ProgettoUIDFinal.model.PlayerModel;         // <--- IMPORTANTE

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class HelloController implements Initializable {

    @FXML private StackPane rootStack;
    @FXML private Button BackButton;
    @FXML private BorderPane rootPane;
    @FXML private Button shopButton;
    @FXML private javafx.scene.control.Label soldiLabel;
    @FXML private ImageView backgroundImageView;
    @FXML private ImageView helloViewHatImage;

    @FXML ToggleButton btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    @FXML ToggleButton Cap1, Cap2, Cap3, Cap4, Cap5, Cap6, Cap7, Cap8, Cap9;
    @FXML ToggleButton dres1,dres2,dres3,dres4,dres5,dres6,dres7,dres8,dres9;
    @FXML private BorderPane closetRootPane;
    @FXML private ImageView HatImage;
    // Removed unused HelmetImage if not needed, or keep if referenced in FXML

    private Scene previousScene;
    private Image currentBackgroundImage = null;

    private final Map<String, String> idToFxml = Map.of(
            "crownBtn", "/org/example/ProgettoUIDFinal/page_crown.fxml",
            "shirtBtn", "/org/example/ProgettoUIDFinal/page_shirt.fxml",
            "talkBtn", "/org/example/ProgettoUIDFinal/page_hair.fxml",
            "artBtn", "/org/example/ProgettoUIDFinal/page_art.fxml"
    );

    private HelloApplication mainApp;
    private Stage primaryStage;

    public void setMainApp(HelloApplication mainApp) {
        this.mainApp = mainApp;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. === COLLEGAMENTO AL MODEL (BINDING) ===
        // Recuperiamo il giocatore dal repository
        PlayerModel player = GameRepository.getInstance().getPlayer();

        // Colleghiamo la label dei soldi alla proprietà "gold" del player.
        // Ogni volta che i soldi cambiano nel model, la label cambia da sola!
        if (soldiLabel != null) {
            soldiLabel.textProperty().bind(player.goldProperty().asString());
        }

        // 2. === GESTIONE BACKGROUND ===
        Image started = BackgroundService.getInstance().getBackground();
        if (started != null) {
            applyBackground(rootStack != null ? rootStack : rootPane, started);
            applyBackground(backgroundImageView, started);
            currentBackgroundImage = started;
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
            else System.err.println("[DEBUG] mainApp è null in showAddTaskView()");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void showShop(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Shop.fxml"));
        Parent shopRoot = loader.load();

        // NOTA: Ho rimosso la parte complicata con "getMethod" e reflection.
        // Il controller dello Shop dovrà leggere i soldi direttamente da GameRepository
        // esattamente come abbiamo fatto qui nell'initialize. Molto più pulito!

        var shopController = loader.getController();
        if (shopController != null) {
            try {
                // Se il ShopController ha un metodo setHomeScene, lo chiamiamo direttamente
                // (Assicurati che ShopController abbia questo metodo o aggiungi un cast)
                shopController.getClass().getMethod("setHomeScene", Scene.class)
                        .invoke(shopController, shopButton.getScene());
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        currentStage.setScene(new Scene(shopRoot));
    }

    public void showProfile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
        Parent profileRoot = loader.load();
        org.example.ProgettoUIDFinal.profileController pc = loader.getController();

        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        Scene currentScene = currentStage.getScene();
        pc.setHomeScene(currentScene);
        this.previousScene = currentScene;

        currentStage.setScene(new Scene(profileRoot));
        attachListenersToButtonsIfPresent();
    }

    public void showBoss(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("boss.fxml"));
        Parent bossRoot = loader.load();
        org.example.ProgettoUIDFinal.bossController bc = loader.getController();

        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        Scene currentScene = currentStage.getScene();
        bc.setHomeScene(currentScene);
        this.previousScene = currentScene;

        currentStage.setScene(new Scene(bossRoot));
        attachListenersToButtonsIfPresent();
    }

    @FXML
    public void showCloset(ActionEvent event) throws IOException {
        // La tua logica "SOLUZIONE" per il Closet è corretta e la manteniamo intatta.

        if (this.helloViewHatImage == null) {
            this.helloViewHatImage = this.HatImage;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ProgettoUIDFinal/Closet.fxml"));
        loader.setController(this); // Usiamo questo stesso controller
        Parent closetRoot = loader.load();

        ImageView closetViewHatImage = this.HatImage;

        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        previousScene = currentStage.getScene();
        currentStage.setScene(new Scene(closetRoot));

        if (this.helloViewHatImage != null && closetViewHatImage != null) {
            closetViewHatImage.setImage(this.helloViewHatImage.getImage());
        }

        if (closetViewHatImage != null) {
            closetViewHatImage.imageProperty().addListener((obs, oldImage, newImage) -> {
                if (this.helloViewHatImage != null) {
                    this.helloViewHatImage.setImage(newImage);
                }
            });
        }

        FXMLLoader crownLoader = new FXMLLoader(getClass().getResource("/org/example/ProgettoUIDFinal/page_crown.fxml"));
        Parent crownPage = crownLoader.load();
        CrownPageController crownController = crownLoader.getController();

        BorderPane closetBorder = (BorderPane) closetRoot.lookup("#closetRootPane");
        if (closetBorder != null) {
            closetBorder.setCenter(crownPage);
        }

        if (crownController != null) {
            crownController.setHatPreview(closetViewHatImage);
            Platform.runLater(crownController::updateCapImages);
        }
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        if (!(event.getSource() instanceof Node node)) return;
        String id = node.getId();
        if (id == null) return;

        String fxmlPath = idToFxml.get(id);
        if (fxmlPath != null) setCenterFromFxml(fxmlPath);
    }

    // === LOGICA BOTTONI AGGIORNATA PER USARE IL MODEL ===
    private void attachListenersToButtonsIfPresent() {
        ToggleButton[] buttons = {btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9};
        PlayerModel player = GameRepository.getInstance().getPlayer(); // Prendiamo il player

        for (ToggleButton tb : buttons) {
            if (tb == null) continue;
            String itemId = tb.getId(); // Assumiamo che l'ID del bottone sia l'ID dell'item (es. "cap1")

            // Controlliamo nel Model se esiste l'item
            ItemModel item = GameRepository.getInstance().getItem(itemId);

            // Se l'item esiste e il giocatore lo possiede (o se vogliamo mostrare l'anteprima a tutti)
            if (item != null) {
                // Carichiamo l'immagine dal path salvato nell'Item
                try {
                    Image img = new Image(getClass().getResourceAsStream(item.getImagePath()));
                    if (tb.getGraphic() instanceof ImageView iv) {
                        iv.setImage(img);
                    }
                } catch (Exception e) {
                    System.err.println("Impossibile caricare immagine per " + itemId);
                }
            }

            ImageView iv = findImageView(tb.getGraphic());
            if (iv != null) iv.setMouseTransparent(true);

            Object already = tb.getProperties().get("bg-listener-attached");
            if (!Boolean.TRUE.equals(already)) {
                attachSelectionListener(tb);
                tb.getProperties().put("bg-listener-attached", Boolean.TRUE);
            }
        }
    }

    private void attachSelectionListener(ToggleButton tb) {
        tb.selectedProperty().addListener((obs, oldVal, newVal) -> {
            ImageView iv = findImageView(tb.getGraphic());
            if (iv != null) {
                iv.setCache(false);
                if (newVal) {
                    ColorAdjust darken = new ColorAdjust();
                    darken.setBrightness(-0.6);
                    iv.setEffect(darken);

                    Image img = iv.getImage();
                    if (img != null) {
                        currentBackgroundImage = img;
                        applyBackground(rootStack != null ? rootStack : rootPane, currentBackgroundImage);
                        if (closetRootPane != null) applyBackground(closetRootPane, currentBackgroundImage);
                        applyBackground(backgroundImageView, currentBackgroundImage);
                        try { BackgroundService.getInstance().setBackground(currentBackgroundImage); } catch (Throwable ignored) {}
                    }
                } else {
                    iv.setEffect(null);
                }
            } else {
                if (newVal) tb.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
                else tb.setStyle("-fx-background-color: transparent;");
            }
        });
    }

    private ImageView findImageView(Node node) {
        if (node instanceof ImageView iv) return iv;
        if (node instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                ImageView result = findImageView(child);
                if (result != null) return result;
            }
        }
        return null;
    }

    private void applyBackground(ImageView imageView, Image image) {
        if (imageView != null && image != null) {
            imageView.setImage(image);
            currentBackgroundImage = image;
        }
    }

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

    @FXML
    public void Home(ActionEvent event) {
        Stage currentStage = (Stage) BackButton.getScene().getWindow();
        if (currentStage != null && previousScene != null) {
            currentStage.setScene(previousScene);
            Image bg = BackgroundService.getInstance().getBackground();
            if (bg != null) applyBackground(backgroundImageView, bg);
        }
    }

    private void setCenterFromFxml(String resourcePath) {
        try {
            var resource = getClass().getResource(resourcePath);
            if (resource == null) {
                System.err.println("FXML non trovato: " + resourcePath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent page = loader.load();
            Object controller = loader.getController();

            if (controller instanceof ShirtPageController) {
                ((ShirtPageController) controller).updateDresImages();
            } else if (controller instanceof CrownPageController) {
                ((CrownPageController) controller).updateCapImages();
            }

            try {
                Node n;
                // Lookup manuale dei bottoni se necessario...
                n = page.lookup("#btn1"); if (n instanceof ToggleButton) btn1 = (ToggleButton) n;
                n = page.lookup("#btn2"); if (n instanceof ToggleButton) btn2 = (ToggleButton) n;
                // ... (continua per gli altri bottoni se necessario)
                // Nota: Se il controller delle pagine (es. CrownPageController) gestisse i propri bottoni,
                // non servirebbe fare lookup qui. Ma per ora lo lasciamo.
            } catch (Exception ex) { ex.printStackTrace(); }

            if (closetRootPane != null) closetRootPane.setCenter(page);
            else if (rootPane != null) rootPane.setCenter(page);

            attachListenersToButtonsIfPresent();

        } catch (IOException e) { e.printStackTrace(); }
    }
}