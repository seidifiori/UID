package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.ItemModel;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ClosetController implements Initializable {

    @FXML private BorderPane closetRootPane;
    @FXML private ImageView HatImage;
    @FXML private ImageView HelmetImage;
    @FXML private Button BackButton;

    private Scene previousScene;
    private ImageView homeHatImage;

    private final Map<String, String> idToFxml = Map.of(
            "crownBtn", "/org/example/ProgettoUIDFinal/page_crown.fxml",
            "shirtBtn", "/org/example/ProgettoUIDFinal/page_shirt.fxml",
            "talkBtn", "/org/example/ProgettoUIDFinal/page_hair.fxml",
            "artBtn", "/org/example/ProgettoUIDFinal/page_art.fxml"
    );

    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    public void setHomeHatImage(ImageView imgView) {
        this.homeHatImage = imgView;
        if (homeHatImage != null && homeHatImage.getImage() != null) {
            this.HatImage.setImage(homeHatImage.getImage());
        }
        if (this.HatImage != null && this.homeHatImage != null) {
            this.HatImage.imageProperty().addListener((obs, oldImg, newImg) -> {
                this.homeHatImage.setImage(newImg);
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null) {
            applyBackground(closetRootPane, currentBg);
        }
        setCenterFromFxml(idToFxml.get("crownBtn"));
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        if (!(event.getSource() instanceof Node node)) return;
        String id = node.getId();
        if (idToFxml.containsKey(id)) {
            setCenterFromFxml(idToFxml.get(id));
        }
    }

    @FXML
    public void Home(ActionEvent event) {
        Stage currentStage = (Stage) BackButton.getScene().getWindow();
        if (currentStage != null && previousScene != null) {
            currentStage.setScene(previousScene);
        }
    }

    private void setCenterFromFxml(String resourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent page = loader.load();
            closetRootPane.setCenter(page);
            trovaEConfiguraBottoni(page);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void trovaEConfiguraBottoni(Parent page) {
        String[] possibleIds = {
                "Cap1", "Cap2", "Cap3", "Cap4", "Cap5", "Cap6", "Cap7", "Cap8", "Cap9",
                "Dres1", "Dres2", "Dres3", "Dres4", "Dres5", "Dres6", "Dres7", "Dres8", "Dres9",
                "btn1", "btn2", "btn3", "btn4", "btn5", "btn6", "btn7", "btn8", "btn9"
        };

        ToggleGroup group = new ToggleGroup();
        GameRepository repo = GameRepository.getInstance();

        for (String btnId : possibleIds) {
            Node node = page.lookup("#" + btnId);

            if (node instanceof ToggleButton btn) {
                btn.setToggleGroup(group);
                String itemId = btnId.toLowerCase();

                ImageView btnIv = findImageView(btn.getGraphic());
                if (btnIv != null) btnIv.setMouseTransparent(true);

                boolean isBackground = itemId.startsWith("btn") || itemId.startsWith("bg");
                boolean isUnlocked = isBackground || repo.isItemOwned(itemId);

                if (isUnlocked) {
                    // --- SBLOCCATO ---
                    btn.setDisable(false);
                    if (btnIv != null) btnIv.setOpacity(1.0);

                    ItemModel item = repo.getItem(itemId);

                    if (item != null && item.getImagePath() != null) {
                        try {
                            Image img = new Image(getClass().getResourceAsStream(item.getImagePath()));

                            // === QUESTA È LA CORREZIONE ===
                            // Prima era commentata. Ora DEVE essere attiva.
                            // Questo sostituisce l'immagine "Locked" (del FXML) con l'immagine vera (Witch, ecc.)
                            if (btnIv != null) {
                                btnIv.setImage(img);
                            }
                            // ==============================

                            btn.setOnAction(e -> gestisciClickBottone(itemId, img));

                        } catch (Exception e) {
                            System.err.println("Errore caricamento immagine per: " + itemId);
                        }
                    }
                } else {
                    // --- BLOCCATO ---
                    // Se vuoi che quando è bloccato si veda il lucchetto (HatLocked.png)
                    // assicurati che nel tuo FXML l'immagine di default sia proprio il lucchetto.
                    btn.setDisable(true);

                    // Se è bloccato, lo facciamo un po' trasparente
                    if (btnIv != null) btnIv.setOpacity(0.5);
                }
            }
        }
    }

    private void gestisciClickBottone(String itemId, Image img) {
        if (itemId.startsWith("cap")) {
            if (HatImage != null) HatImage.setImage(img);
        }
        else if (itemId.startsWith("dres") || itemId.startsWith("armor")) {
            if (HelmetImage != null) HelmetImage.setImage(img);
        }
        else if (itemId.startsWith("btn") || itemId.startsWith("art") || itemId.startsWith("bg")) {
            applyBackground(closetRootPane, img);
            BackgroundService.getInstance().setBackground(img);
        }
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