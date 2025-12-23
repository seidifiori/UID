package org.example.ProgettoUIDFinal;

import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.ItemModel;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ClosetController implements Initializable {

    @FXML private StackPane closetRootPane;
    @FXML private StackPane centerHolder;

    @FXML private Button BackButton;
    // 1. NUOVO: Il bottone per cambiare sesso (assicurati di averlo nel FXML)
    @FXML private Button genderButton;

    @FXML private ImageView baseAvatarLayer;
    @FXML private ImageView hairLayer;
    @FXML private ImageView hatLayer;
    @FXML private ImageView armorLayer;
    @FXML private ImageView swordLayer;
    @FXML private ImageView shieldLayer;
    @FXML private ImageView backgroundLayer;
    @FXML private Scene homeScene;

    @FXML private ImageView hatIcon;
    @FXML private ImageView hairIcon;
    @FXML private ImageView armorIcon;
    @FXML private ImageView swordIcon;
    @FXML private ImageView shieldIcon;

    @FXML private ToggleButton hatButton;
    @FXML private ToggleButton armorButton;
    @FXML private ToggleButton hairButton;
    @FXML private ToggleButton backgroundButton;
    private final ToggleGroup toggleGroup = new ToggleGroup();

    private Tooltip sharedTooltip;
    private Scene previousScene;
    private PlayerModel player;

    // 2. NUOVO: Teniamo traccia della pagina aperta per ricaricarla dopo il cambio sesso
    private String currentFxmlPath = "/org/example/ProgettoUIDFinal/closet-armors.fxml";

    private final Map<String, String> idToFxml = Map.of(
            "hatButton", "/org/example/ProgettoUIDFinal/closet-hats.fxml",
            "armorButton", "/org/example/ProgettoUIDFinal/closet-armors.fxml",
            "hairButton", "/org/example/ProgettoUIDFinal/closet-styles.fxml",
            "backgroundButton", "/org/example/ProgettoUIDFinal/closet-background.fxml"
    );

    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    @FXML
    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MusicManager.getInstance().playMusic("closet.mp3");
        this.player = GameRepository.getInstance().getPlayer();

        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null) {
            applyBackground(closetRootPane, currentBg);
            if (backgroundLayer != null) {
                backgroundLayer.setImage(currentBg);
            }
        }

        hatButton.setToggleGroup(toggleGroup);
        armorButton.setToggleGroup(toggleGroup);
        hairButton.setToggleGroup(toggleGroup);
        backgroundButton.setToggleGroup(toggleGroup);

        // Carica la pagina iniziale
        setCenterFromFxml(idToFxml.get("armorButton"));

        // Imposta il testo del bottone gender all'avvio
        updateGenderButtonUI();

        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.swordImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());

        bindLayer(hatIcon, player.hatIconProperty());
        bindLayer(hairIcon, player.hairIconProperty());
        bindLayer(armorIcon, player.armorIconProperty());
        bindLayer(swordIcon, player.swordIconProperty());
        bindLayer(shieldIcon, player.shieldIconProperty());

        initTooltipSystem();

        setupTooltip(hatIcon, player.hatNameProperty());
        setupTooltip(hairIcon, player.hairNameProperty());
        setupTooltip(armorIcon, player.armorNameProperty());
        setupTooltip(swordIcon, player.swordNameProperty());
        setupTooltip(shieldIcon, player.shieldNameProperty());

        if (hairLayer != null) {
            hairLayer.visibleProperty().bind(player.isHairVisibleProperty());
        }
    }

    // 3. NUOVO: Metodo per l'azione del bottone
    @FXML
    public void switchGender(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");

        // Cambia i dati nel modello (questo aggiorna l'avatar visivo automaticamente)
        player.toggleGender();

        // Aggiorna la grafica del bottone (♂ / ♀)
        updateGenderButtonUI();

        // Ricarica la pagina centrale (Armature, Cappelli, ecc.)
        // Questo serve perché i bottoni devono ricaricare i "Path" corretti (Maschio/Femmina)
        setCenterFromFxml(this.currentFxmlPath);
    }

    private void updateGenderButtonUI() {
        if (genderButton != null) {
            genderButton.setText(player.isMale() ? "♂" : "♀");
            // Opzionale: puoi cambiare colore o stile qui
        }
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
            // Aggiorniamo il path corrente
            this.currentFxmlPath = idToFxml.get(id);
            setCenterFromFxml(this.currentFxmlPath);
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
        // Salviamo il path per sicurezza
        this.currentFxmlPath = resourcePath;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent page = loader.load();

            if (centerHolder.getChildren().size() > 1) {
                centerHolder.getChildren().remove(1);
            }

            centerHolder.getChildren().add(page);

            trovaEConfiguraBottoni(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void trovaEConfiguraBottoni(Parent page) {
        String[] possibleIds = {
                "cap1", "cap2", "cap3", "cap4", "cap5", "cap6",
                "dres1", "dres2", "dres3", "dres4", "dres5", "dres6",
                "har1", "har2", "har3", "har4", "har5", "har6", "har7", "har8", "har9",
                "btn1", "btn2", "btn3", "btn4", "btn5", "btn6", "btn7", "btn8", "btn9"
        };

        ToggleGroup group = new ToggleGroup();
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null && oldVal != null) {
                oldVal.setSelected(true);
            }
        });

        GameRepository repo = GameRepository.getInstance();
        PlayerModel player = repo.getPlayer();

        for (String btnId : possibleIds) {
            Node node = page.lookup("#" + btnId);

            if (node instanceof ToggleButton btn) {
                btn.setToggleGroup(group);

                String type = btnId.replaceAll("[0-9]", "").toLowerCase();
                String numStr = btnId.replaceAll("[^0-9]", "");
                int num = 0;
                try { num = Integer.parseInt(numStr); } catch (Exception e) {}

                ImageView btnIv = findImageView(btn.getGraphic());
                if (btnIv != null) btnIv.setMouseTransparent(true);

                if (type.startsWith("btn") || type.startsWith("bg")) {
                    configureBackgroundButton(btn, btnId, btnIv, group);
                    continue;
                }

                int lastButtonIndex = 9;
                if (type.startsWith("cap") || type.startsWith("dres") || type.startsWith("armor")) lastButtonIndex = 6;

                String layerPath = null;
                String iconPath = null;
                String itemName = "Oggetto Sconosciuto";

                if (num != lastButtonIndex) {
                    String itemId = type + num;
                    ItemModel item = repo.getItem(itemId);
                    if (item != null) {
                        // 4. MODIFICA CRUCIALE: Richiediamo il path in base al sesso attuale
                        layerPath = item.getLayerPath(player.isMale());

                        iconPath = item.getIconPath();
                        itemName = item.getName();
                    }
                }

                btn.setUserData(layerPath);

                String currentEquippedPath = null;
                if (type.startsWith("cap")) {
                    currentEquippedPath = player.hatPathProperty().get();
                } else if (type.startsWith("dres") || type.startsWith("armor")) {
                    currentEquippedPath = player.armorPathProperty().get();
                } else if (type.startsWith("har")) {
                    currentEquippedPath = player.hairPathProperty().get();
                }

                if (isEquipped(layerPath, currentEquippedPath)) {
                    btn.setSelected(true);
                }

                if (num == lastButtonIndex) {
                    configuraBottoneRimozione(btn, type, btnIv, group);
                } else {
                    String itemId = type + num;
                    boolean isOwned = false;

                    if (type.startsWith("har")) isOwned = true;
                    else if (type.startsWith("dres") && (num == 4 || num == 5) ) isOwned = true;
                    else if (type.startsWith("cap")) isOwned = (num == 4 || num == 5) || repo.isItemOwned(itemId);
                    else isOwned = repo.isItemOwned(itemId);

                    if (isOwned) {
                        btn.setDisable(false);
                        if (btnIv != null) {
                            btnIv.setOpacity(1.0);
                            btnIv.setEffect(null);
                            try {
                                if (iconPath != null) btnIv.setImage(new Image(getClass().getResourceAsStream(iconPath)));
                            } catch (Exception e) {}
                        }

                        String finalLayer = layerPath;
                        String finalIcon = iconPath;
                        String finalName = itemName;

                        btn.setOnAction(e -> gestisciClickBottone(type, finalLayer, finalIcon, finalName, group));

                    } else {
                        btn.setDisable(true);
                        if (btnIv != null) {
                            javafx.scene.effect.ColorAdjust darken = new javafx.scene.effect.ColorAdjust();
                            darken.setBrightness(-0.7);
                            darken.setSaturation(-1.0);
                            btnIv.setEffect(darken);
                            btnIv.setOpacity(0.5);
                            try {
                                if (iconPath != null) btnIv.setImage(new Image(getClass().getResourceAsStream(iconPath)));
                            } catch (Exception e) {}
                        }
                    }
                }
            }
        }
    }

    // ... (Il resto dei metodi helper: isEquipped, configuraBottoneRimozione, etc. rimangono uguali) ...
    // Li copio per completezza

    private boolean isEquipped(String buttonPath, String playerPath) {
        if (buttonPath == null) {
            return playerPath == null || playerPath.trim().isEmpty();
        }
        if (playerPath == null) {
            return false;
        }
        return buttonPath.equals(playerPath);
    }

    private void configuraBottoneRimozione(ToggleButton btn, String type, ImageView btnIv, ToggleGroup group) {
        btn.setDisable(false);
        if (btnIv != null) btnIv.setOpacity(1.0);
        String emptyName = "Nessuno";
        String basePath = "/org/example/ProgettoUIDFinal/imagini/";
        String defaultIconPath = null;

        if (type.equals("cap")) {
            emptyName = "Nessun Elmo";
            defaultIconPath = basePath + "Icon-helmet.png";
        }
        else if (type.equals("dres") || type.equals("armor")) {
            emptyName = "Nessuna Armatura";
            defaultIconPath = basePath + "Icon-armor.png";
        }
        else if (type.equals("har")) {
            emptyName = "Nessuna Acconciatura";
            defaultIconPath = basePath + "Icon-hair.png";
        }

        String finalEmptyName = emptyName;
        String finalIconPath = defaultIconPath;

        btn.setOnAction(e -> gestisciClickBottone(type, null, finalIconPath, finalEmptyName, group));
    }

    private void gestisciClickBottone(String type, String layerPath, String iconPath, String itemName, ToggleGroup group) {
        System.out.println("Click ricevuto: Tipo=" + type + " Path=" + layerPath);
        MusicManager.getInstance().playSoundEffect("dress-up.mp3");

        String safeName = (itemName != null && !itemName.isEmpty()) ? itemName : "Nessuno";
        if (type.equals("cap")) {
            player.setHat(layerPath);
            player.setHatIcon(iconPath);
            player.setHatName(safeName);
        }
        else if (type.equals("dres") || type.equals("armor")) {
            player.setArmor(layerPath);
            player.setArmorIcon(iconPath);
            player.setArmorName(safeName);
        }
        else if (type.equals("har")) {
            player.setHair(layerPath);
            player.setHairIcon(iconPath);
            player.setHairName(safeName);
        }
    }

    private void configureBackgroundButton(ToggleButton btn, String btnId, ImageView btnIv, ToggleGroup group) {
        btn.setDisable(false);
        if (btnIv != null) btnIv.setOpacity(1.0);
        Image bgImg = (btnIv != null) ? btnIv.getImage() : null;
        btn.setOnAction(e -> {
            if (bgImg != null) {
                applyBackground(closetRootPane, bgImg);
                BackgroundService.getInstance().setBackground(bgImg);
                if (backgroundLayer != null) {
                    backgroundLayer.setImage(bgImg);
                }
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
        BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs);
        region.setBackground(new Background(bi));
    }

    private void initTooltipSystem() {
        sharedTooltip = new Tooltip();
        sharedTooltip.setShowDelay(Duration.ZERO);
        sharedTooltip.setHideDelay(Duration.ZERO);
        sharedTooltip.getStyleClass().add("tooltip-custom");
    }

    private void setupTooltip(ImageView target, ObservableValue<String> textProperty) {
        if (target == null) return;
        target.setOnMouseEntered(event -> {
            sharedTooltip.textProperty().bind(textProperty);
            sharedTooltip.show(target, event.getScreenX() + 15, event.getScreenY() + 15);
        });
        target.setOnMouseMoved(event -> {
            sharedTooltip.setX(event.getScreenX() + 15);
            sharedTooltip.setY(event.getScreenY() + 15);
        });
        target.setOnMouseExited(event -> {
            sharedTooltip.hide();
            sharedTooltip.textProperty().unbind();
        });
    }
}