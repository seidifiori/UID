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
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.ItemModel;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ClosetController implements Initializable {

    @FXML private BorderPane closetRootPane;
    @FXML private ImageView HatImage; // L'immagine NELL'armadio
    @FXML private Button BackButton;

    // Riferimenti ai bottoni che potrebbero essere caricati nelle sottopagine
    private ToggleButton btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9;

    private Scene previousScene; // Per tornare alla Home
    private ImageView homeHatImage; // Riferimento all'immagine della Home (per sincronizzarla)
    private Image currentBackgroundImage = null;

    private final Map<String, String> idToFxml = Map.of(
            "crownBtn", "/org/example/ProgettoUIDFinal/page_crown.fxml",
            "shirtBtn", "/org/example/ProgettoUIDFinal/page_shirt.fxml",
            "talkBtn", "/org/example/ProgettoUIDFinal/page_hair.fxml",
            "artBtn", "/org/example/ProgettoUIDFinal/page_art.fxml"
    );

    // === METODI PER PASSARE DATI DA HELLOCONTROLLER ===

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }

    public void setHomeHatImage(ImageView imgView) {
        this.homeHatImage = imgView;
        // Se l'immagine della home ha già qualcosa, impostala anche qui
        if (homeHatImage != null && homeHatImage.getImage() != null) {
            this.HatImage.setImage(homeHatImage.getImage());
        }

        // BINDING: Se cambia l'immagine qui nel Closet, cambia anche nella Home
        if (this.HatImage != null && this.homeHatImage != null) {
            this.HatImage.imageProperty().addListener((obs, oldImg, newImg) -> {
                this.homeHatImage.setImage(newImg);
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Applica lo sfondo corrente
        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null) {
            applyBackground(closetRootPane, currentBg);
        }

        // Carica la pagina di default (es. Crown)
        setCenterFromFxml(idToFxml.get("crownBtn"));
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        if (!(event.getSource() instanceof Node node)) return;
        String id = node.getId();
        if (id == null) return;

        String fxmlPath = idToFxml.get(id);
        if (fxmlPath != null) setCenterFromFxml(fxmlPath);
    }

    @FXML
    public void Home(ActionEvent event) {
        Stage currentStage = (Stage) BackButton.getScene().getWindow();
        if (currentStage != null && previousScene != null) {
            currentStage.setScene(previousScene);
            // Ripristina background nella home se necessario
            // (Nota: BackgroundService gestisce già i dati, quindi la Home si aggiornerà da sola o tramite listener)
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

            // Aggiorna le immagini in base al controller caricato
            if (controller instanceof ShirtPageController) {
                ((ShirtPageController) controller).updateDresImages();
            } else if (controller instanceof CrownPageController) {
                // Passiamo l'anteprima al controller della pagina
                ((CrownPageController) controller).setHatPreview(this.HatImage);
                ((CrownPageController) controller).updateCapImages();
            }

            // Lookup manuale dei bottoni nella pagina appena caricata
            // (Serve per attachListenersToButtonsIfPresent)
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

            closetRootPane.setCenter(page);
            attachListenersToButtonsIfPresent();

        } catch (IOException e) { e.printStackTrace(); }
    }

    private void attachListenersToButtonsIfPresent() {
        ToggleButton[] buttons = {btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9};

        for (ToggleButton tb : buttons) {
            if (tb == null) continue;
            String itemId = tb.getId();

            ItemModel item = GameRepository.getInstance().getItem(itemId);
            if (item != null) {
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
                        applyBackground(closetRootPane, currentBackgroundImage);
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
}