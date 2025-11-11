package org.example.uididididii;

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

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class HelloController implements Initializable {

    @FXML private StackPane rootStack;
    @FXML private BorderPane rootPane;
    @FXML private Button shopButton;
    @FXML private Button ProfileButton;
    @FXML private Button closetButton;
    @FXML private Button bossButton;
    @FXML private javafx.scene.control.Label soldiLabel;
    @FXML private ImageView backgroundImageView;

    @FXML ToggleButton btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;
    @FXML ToggleButton Cap1, Cap2, Cap3, Cap4, Cap5, Cap6, Cap7, Cap8, Cap9;
    @FXML ToggleButton btn21, btn22, btn23, btn24, btn25, btn26, btn27, btn28, btn29;
    @FXML private Button BackButton;
    @FXML private BorderPane closetRootPane;

    private Scene previousScene;
    private Parent previousCenter;

    private final Map<String, Parent> pageCache = new HashMap<>();
    private Image currentBackgroundImage = null;
    private final Map<String, String> idToFxml = Map.of(
            "crownBtn", "/org/example/uididididii/page_crown.fxml",
            "shirtBtn", "/org/example/uididididii/page_shirt.fxml",
            "talkBtn", "/org/example/uididididii/page_hair.fxml",
            "artBtn", "/org/example/uididididii/page_art.fxml"
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
        // Applica sfondo iniziale se presente
        Image started = BackgroundService.getInstance().getBackground();
        if (started != null) {
            applyBackground(rootStack != null ? rootStack : rootPane, started);
            applyBackground(backgroundImageView, started);
            currentBackgroundImage = started;
        }

        // Ascolta cambiamenti del BackgroundService
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
            if (mainApp != null) {
                mainApp.showAddTaskView();
            } else {
                System.err.println("[DEBUG] mainApp è null in showAddTaskView()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showShop(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Shop.fxml"));
        Parent shopRoot = loader.load();

        var shopController = loader.getController();
        if (shopController != null) {
            try {
                shopController.getClass().getMethod("setSoldiLabel", javafx.scene.control.Label.class)
                        .invoke(shopController, soldiLabel);
                Stage currentStage = (Stage) shopButton.getScene().getWindow();
                shopController.getClass().getMethod("setHomeScene", Scene.class)
                        .invoke(shopController, currentStage.getScene());
            } catch (NoSuchMethodException ignored) {}
            catch (Exception ex) { ex.printStackTrace(); }
        }

        Stage currentStage = (Stage) shopButton.getScene().getWindow();
        currentStage.setScene(new Scene(shopRoot));
    }
    public void showProfile(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("profile.fxml"));
        Parent profileRoot = loader.load(); // carica UNA sola volta

        // Ottieni il controller vero del profilo (quello dichiarato in FXML)
        org.example.uididididii.profileController pc = loader.getController();

        // Salva la scena corrente come "home" e passala al controller del profilo
        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        Scene currentScene = currentStage.getScene();
        pc.setHomeScene(currentScene); // importante: passa la scena PRIMA di cambiarla

        // salva previousScene nel controller/qui se ti serve
        this.previousScene = currentScene;

        // cambia scena mostrando il profilo
        currentStage.setScene(new Scene(profileRoot));

        // se il profilo contiene pulsanti che vuoi gestire dal tuo HelloController,
        // collegali esplicitamente dopo il load (ma di solito non necessario)
        attachListenersToButtonsIfPresent();
    }
    public void showBoss(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("boss.fxml"));
        Parent profileRoot = loader.load(); // carica UNA sola volta

        // Ottieni il controller vero del profilo (quello dichiarato in FXML)
        org.example.uididididii.bossController pc = loader.getController();

        // Salva la scena corrente come "home" e passala al controller del profilo
        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        Scene currentScene = currentStage.getScene();
        pc.setHomeScene(currentScene); // importante: passa la scena PRIMA di cambiarla

        // salva previousScene nel controller/qui se ti serve
        this.previousScene = currentScene;

        // cambia scena mostrando il profilo
        currentStage.setScene(new Scene(profileRoot));

        // se il profilo contiene pulsanti che vuoi gestire dal tuo HelloController,
        // collegali esplicitamente dopo il load (ma di solito non necessario)
        attachListenersToButtonsIfPresent();
    }



    @FXML
    public void showCloset(ActionEvent event) throws IOException {
        // 1) Carica il Closet.fxml e assegna questo come controller (per gli onAction presenti in Closet.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/uididididii/Closet.fxml"));
        loader.setController(this);               // <<-- ESSENZIALE se Closet.fxml NON ha fx:controller
        Parent closetRoot = loader.load();

        // 2) Mostra la scena del closet
        Stage currentStage = (Stage) (rootStack != null ? rootStack.getScene().getWindow() : rootPane.getScene().getWindow());
        previousScene = currentStage.getScene();
        currentStage.setScene(new Scene(closetRoot));

        // 3) Carica la pagina crown (che ha il suo fx:controller CrownPageController)
        FXMLLoader crownLoader = new FXMLLoader(getClass().getResource("/org/example/uididididii/page_crown.fxml"));
        Parent crownPage = crownLoader.load();
        CrownPageController crownController = crownLoader.getController(); // dovrebbe essere non-null (page_crown.fxml ha fx:controller)

        // 4) Recupera il BorderPane del closet (se lo vuoi cercare via id oppure cast diretto)
        BorderPane closetBorder = null;
        // prova lookup (se il BorderPane ha fx:id="closetRootPane" nel FXML)
        try {
            Node found = closetRoot.lookup("#closetRootPane");
            if (found instanceof BorderPane) closetBorder = (BorderPane) found;
        } catch (Exception ignored) {}

        // fallback: se il root stesso è BorderPane
        if (closetBorder == null && closetRoot instanceof BorderPane) {
            closetBorder = (BorderPane) closetRoot;
        }

        if (closetBorder != null) {
            closetBorder.setCenter(crownPage);
        } else {
            System.err.println("Impossibile trovare il BorderPane 'closetRootPane' nel Closet.fxml");
        }

        // 5) Aggiorna le immagini dei cappelli posseduti — esegui dopo che la scena è impostata
        if (crownController != null) {
            Platform.runLater(() -> {
                crownController.updateCapImages();
            });
        } else {
            System.err.println("CrownPageController è null — controlla che page_crown.fxml abbia fx:controller.");
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

    private void attachListenersToButtonsIfPresent() {
        ToggleButton[] buttons = {btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9};
        for (ToggleButton tb : buttons) {
            if (tb == null) continue;

            // 🔹 Controlla se l'oggetto è acquistato e aggiorna immagine
            Image ownedImage = InventoryService.getInstance().getItemImage(tb.getId());
            if (ownedImage != null && tb.getGraphic() instanceof ImageView iv) {
                iv.setImage(ownedImage);
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
            Parent page = pageCache.get(resourcePath);
            if (page == null) {
                var resource = getClass().getResource(resourcePath);
                if (resource == null) {
                    System.err.println("FXML non trovato: " + resourcePath);
                    return;
                }
                FXMLLoader loader = new FXMLLoader(resource);
                page = loader.load();
                pageCache.put(resourcePath, page);
            }

            try {
                Node n;
                n = page.lookup("#btn1"); if (n instanceof ToggleButton) btn1 = (ToggleButton) n;
                n = page.lookup("#btn2"); if (n instanceof ToggleButton) btn2 = (ToggleButton) n;
                n = page.lookup("#btn3"); if (n instanceof ToggleButton) btn3 = (ToggleButton) n;
                n = page.lookup("#btn4"); if (n instanceof ToggleButton) btn4 = (ToggleButton) n;
                n = page.lookup("#btn5"); if (n instanceof ToggleButton) btn5 = (ToggleButton) n;
                n = page.lookup("#btn6"); if (n instanceof ToggleButton) btn6 = (ToggleButton) n;
                n = page.lookup("#btn7"); if (n instanceof ToggleButton) btn7 = (ToggleButton) n;
                n = page.lookup("#btn8"); if (n instanceof ToggleButton) btn8 = (ToggleButton) n;
                n = page.lookup("#btn9"); if (n instanceof ToggleButton) btn9 = (ToggleButton) n;
            } catch (Exception ex) { ex.printStackTrace(); }

            if (closetRootPane != null) closetRootPane.setCenter(page);
            else if (rootPane != null) rootPane.setCenter(page);

            attachListenersToButtonsIfPresent();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
