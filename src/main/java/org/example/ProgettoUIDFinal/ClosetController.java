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
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ClosetController implements Initializable {

    @FXML private BorderPane closetRootPane;
    @FXML private Button BackButton;

    @FXML private ImageView baseAvatarLayer;
    @FXML private ImageView hairLayer;
    @FXML private ImageView hatLayer;
    @FXML private ImageView armorLayer;
    @FXML private ImageView swordLayer;
    @FXML private ImageView shieldLayer;

    private Scene previousScene;
    private PlayerModel player;

    private final Map<String, String> idToFxml = Map.of(
            "crownBtn", "/org/example/ProgettoUIDFinal/page_crown.fxml",
            "shirtBtn", "/org/example/ProgettoUIDFinal/page_shirt.fxml",
            "talkBtn", "/org/example/ProgettoUIDFinal/page_hair.fxml",
            "artBtn", "/org/example/ProgettoUIDFinal/page_art.fxml"
    );

    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MusicManager.getInstance().playMusic("closet.mp3");
        this.player = GameRepository.getInstance().getPlayer();

        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null) {
            applyBackground(closetRootPane, currentBg);
        }

        setCenterFromFxml(idToFxml.get("crownBtn"));

        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.weaponImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());
    }

    private void bindLayer(ImageView view, javafx.beans.value.ObservableValue<? extends Image> prop) {
        if (view != null) view.imageProperty().bind(prop);
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        if (!(event.getSource() instanceof Node node)) return;
        String id = node.getId();
        if (idToFxml.containsKey(id)) {
            setCenterFromFxml(idToFxml.get(id));
        }
    }

    @FXML
    public void Home(ActionEvent event) {
        MusicManager.getInstance().playMusic("background_music.mp3");
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
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
                "btn1", "btn2", "btn3", "btn4", "btn5", "btn6", "btn7", "btn8", "btn9",
                "har1", "har2", "har3", "har4", "har5", "har6", "har7", "har8", "har9"
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
                boolean isHair = itemId.startsWith("har"); // Rileviamo se è un capello

                // === MODIFICA QUI ===
                // Se è un background O un capello O lo possiedi -> SBLOCCATO
                boolean isUnlocked = isBackground || isHair || repo.isItemOwned(itemId);

                if (isUnlocked) {
                    btn.setDisable(false);
                    if (btnIv != null) btnIv.setOpacity(1.0);

                    ItemModel item = repo.getItem(itemId);

                    if (item != null && item.getIconPath() != null) {
                        try {
                            Image img = new Image(getClass().getResourceAsStream(item.getIconPath()));
                            if (btnIv != null) btnIv.setImage(img);
                            String path = item.getLayerPath();
                            btn.setOnAction(e -> gestisciClickBottone(itemId, path, img, group));
                        } catch (Exception e) {
                            System.err.println("Errore caricamento immagine item: " + itemId);
                        }
                    } else if (isBackground) {
                        Image bgImg = (btnIv != null) ? btnIv.getImage() : null;
                        btn.setOnAction(e -> gestisciClickBottone(itemId, null, bgImg, group));
                    }
                } else {
                    btn.setDisable(true);
                    if (btnIv != null) btnIv.setOpacity(0.5);
                }
            }
        }
    }

    private void gestisciClickBottone(String itemId, String itemPath, Image img, ToggleGroup group) {
// Recuperiamo l'oggetto completo dal Repository per avere accesso al percorso dell'icona
        GameRepository repo = GameRepository.getInstance();
        ItemModel item = repo.getItem(itemId);

        // Se l'oggetto esiste, prendiamo il percorso dell'icona, altrimenti null
        String iconPath = (item != null) ? item.getIconPath() : null;

        // 1. Logica CAPPELLI (Hat)
        if (itemId.startsWith("cap")) {
            String currentHat = player.hatPathProperty().get();

            // Se clicco su quello che indosso già -> Lo tolgo (toggle off)
            if (currentHat != null && currentHat.equals(itemPath)) {
                player.setHat(null);     // Toglie il layer
                player.setHatIcon(null); // Toglie l'icona dall'inventario/profilo
                if (group.getSelectedToggle() != null) group.getSelectedToggle().setSelected(false);
            } else {
                // Indossa nuovo cappello
                player.setHat(itemPath);    // Mette il layer
                player.setHatIcon(iconPath);// Mette l'icona
            }
        }
        // 2. Logica ARMATURE (Dres/Armor)
        else if (itemId.startsWith("dres") || itemId.startsWith("armor")) {
            String currentArmor = player.armorPathProperty().get();

            if (currentArmor != null && currentArmor.equals(itemPath)) {
                player.setArmor(null);     // Toglie il layer
                player.setArmorIcon(null); // Toglie l'icona
                if (group.getSelectedToggle() != null) group.getSelectedToggle().setSelected(false);
            } else {
                player.setArmor(itemPath);
                player.setArmorIcon(iconPath);
            }
        }
        // 3. Logica CAPELLI (Hair)
        else if (itemId.startsWith("har")) {
            // I capelli si sostituiscono sempre, non si "tolgono"
            player.setHair(itemPath);
            player.setHairIcon(iconPath);
        }
        // 4. Logica SFONDI (Backgrounds)
        else if (itemId.startsWith("btn") || itemId.startsWith("bg")) {
            if (img != null) {
                applyBackground(closetRootPane, img);
                BackgroundService.getInstance().setBackground(img);
            }
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
        BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs);
        region.setBackground(new Background(bi));
    }
}