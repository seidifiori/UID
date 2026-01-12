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
import org.example.ProgettoUIDFinal.Services.BackgroundService;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.ItemModel;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * CONTROLLER DEL GUARDAROBA (Closet): Gestisce la personalizzazione del personaggio.
 * Implementa un sistema di anteprima in tempo reale e una logica di "undo" tramite snapshot
 * per permettere all'utente di annullare le modifiche uscendo senza salvare.
 */
public class ClosetController implements Initializable {

    // --- ELEMENTI UI (Iniezione FXML) ---
    @FXML private StackPane closetRootPane;
    @FXML private StackPane centerHolder;
    @FXML private Button BackButton;
    @FXML private Button genderButton;
    @FXML private ImageView genderImage;

    // Layer dell'Avatar: Gestiti tramite sovrapposizione in uno StackPane
    @FXML private ImageView baseAvatarLayer, hairLayer, hatLayer, armorLayer, swordLayer, shieldLayer, backgroundLayer;

    // Icone di riepilogo equipaggiamento
    @FXML private ImageView hatIcon, hairIcon, armorIcon, swordIcon, shieldIcon;

    // Pulsanti di navigazione categorie
    @FXML private ToggleButton hatButton, armorButton, hairButton, backgroundButton;
    private final ToggleGroup toggleGroup = new ToggleGroup();

    private Tooltip sharedTooltip;
    private Scene previousScene;
    private PlayerModel player;

    // --- SISTEMA DI SNAPSHOT ---
    // Memorizzano lo stato del giocatore all'ingresso nel guardaroba
    private String snapshotBody, snapshotHat, snapshotArmor, snapshotHair, snapshotSword, snapshotShield;
    private String snapshotHatIcon, snapshotArmorIcon, snapshotHairIcon, snapshotSwordIcon, snapshotShieldIcon;
    private String snapshotHatName, snapshotArmorName, snapshotHairName, snapshotSwordName, snapshotShieldName;
    private boolean snapshotIsMale;

    // Navigazione dinamica tra sotto-menu FXML
    private String currentFxmlPath = "/org/example/ProgettoUIDFinal/closet-armors.fxml";

    private final Map<String, String> idToFxml = Map.of(
            "hatButton", "/org/example/ProgettoUIDFinal/closet-hats.fxml",
            "armorButton", "/org/example/ProgettoUIDFinal/closet-armors.fxml",
            "hairButton", "/org/example/ProgettoUIDFinal/closet-styles.fxml",
            "backgroundButton", "/org/example/ProgettoUIDFinal/closet-background.fxml"
    );

    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Gestione Audio Ambientale
        MusicManager.getInstance().playMusic("closet.mp3");
        this.player = GameRepository.getInstance().getPlayer();

        // ESECUZIONE SNAPSHOT: Salvataggio dello stato attuale per eventuale ripristino
        this.snapshotHat = player.getHat();
        this.snapshotArmor = player.getArmor();
        this.snapshotHair = player.getHair();
        this.snapshotHatIcon = player.getHatIcon();
        this.snapshotArmorIcon = player.getArmorIcon();
        this.snapshotHairIcon = player.getHairIcon();
        this.snapshotHatName = player.getHatName();
        this.snapshotArmorName = player.getArmorName();
        this.snapshotHairName = player.getHairName();
        this.snapshotSword = player.getSword();
        this.snapshotSwordIcon = player.getSwordIcon();
        this.snapshotSwordName = player.getSwordName();
        this.snapshotShield = player.getShield();
        this.snapshotShieldIcon = player.getShieldIcon();
        this.snapshotShieldName = player.getShieldName();
        this.snapshotBody = player.bodyPathProperty().get();
        this.snapshotIsMale = player.isMale();

        // Inizializzazione Sfondo tramite Service centralizzato
        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null) {
            applyBackground(closetRootPane, currentBg);
            if (backgroundLayer != null) backgroundLayer.setImage(currentBg);
        }

        // Configurazione Navigazione Categorie
        hatButton.setToggleGroup(toggleGroup);
        armorButton.setToggleGroup(toggleGroup);
        hairButton.setToggleGroup(toggleGroup);
        backgroundButton.setToggleGroup(toggleGroup);

        setCenterFromFxml(idToFxml.get("armorButton"));
        updateGenderButtonUI();

        // DATA BINDING: Collega le ImageView alle Properties del PlayerModel
        // Ogni modifica al modello si riflette automaticamente sulla grafica
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

        // Inizializzazione Tooltip Dinamici per i nomi degli oggetti
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

    /**
     * GENDER SWAP: Esegue il toggle del sesso nel modello e aggiorna la UI locale.
     * Ricarica il sotto-FXML corrente per aggiornare i pulsanti degli oggetti (male/female).
     */
    @FXML
    public void switchGender(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        player.toggleGender();
        updateGenderButtonUI();
        setCenterFromFxml(this.currentFxmlPath);
    }

    private void updateGenderButtonUI() {
        if (genderImage == null) return;
        String iconPath = player.isMale()
                ? "/org/example/ProgettoUIDFinal/imagini/Sprite-button-male.png"
                : "/org/example/ProgettoUIDFinal/imagini/Sprite-button-female.png";
        try {
            genderImage.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void bindLayer(ImageView view, ObservableValue<? extends Image> prop) {
        if (view != null) view.imageProperty().bind(prop);
    }

    @FXML
    private void handleMenu(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        if (!(event.getSource() instanceof Node node)) return;
        String id = node.getId();
        if (idToFxml.containsKey(id)) {
            this.currentFxmlPath = idToFxml.get(id);
            setCenterFromFxml(this.currentFxmlPath);
        }
    }

    /**
     * ANNULLA MODIFICHE (Home): Ripristina lo stato del giocatore tramite lo snapshot
     * salvato all'avvio del controller.
     */
    @FXML
    public void Home(ActionEvent event) {
        MusicManager.getInstance().playMusic("background_music.mp3");
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");

        player.isMaleProperty().set(snapshotIsMale);
        player.setBody(snapshotBody);
        player.setHat(snapshotHat);
        player.setHatIcon(snapshotHatIcon);
        player.setHatName(snapshotHatName);
        player.setArmor(snapshotArmor);
        player.setArmorIcon(snapshotArmorIcon);
        player.setArmorName(snapshotArmorName);
        player.setHair(snapshotHair);
        player.setHairIcon(snapshotHairIcon);
        player.setHairName(snapshotHairName);
        player.setSword(snapshotSword);
        player.setSwordIcon(snapshotSwordIcon);
        player.setSwordName(snapshotSwordName);
        player.setShield(snapshotShield);
        player.setShieldIcon(snapshotShieldIcon);
        player.setShieldName(snapshotShieldName);

        Stage currentStage = (Stage) BackButton.getScene().getWindow();
        if (currentStage != null && previousScene != null) currentStage.setScene(previousScene);
    }

    /**
     * INIEZIONE DINAMICA FXML: Carica una porzione di UI (categorie oggetti)
     * all'interno del contenitore centrale centerHolder.
     */
    private void setCenterFromFxml(String resourcePath) {
        this.currentFxmlPath = resourcePath;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent page = loader.load();
            if (centerHolder.getChildren().size() > 1) centerHolder.getChildren().remove(1);
            centerHolder.getChildren().add(page);
            trovaEConfiguraBottoni(page);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * CONFIGURAZIONE PULSANTI: Scansiona il nodo caricato e configura i listener per ogni
     * oggetto disponibile, gestendo disponibilità, icone e sesso.
     */
    private void trovaEConfiguraBottoni(Parent page) {
        String[] possibleIds = {
                "cap1", "cap2", "cap3", "cap4", "cap5", "cap6",
                "dres1", "dres2", "dres3", "dres4", "dres5", "dres6",
                "har1", "har2", "har3", "har4", "har5", "har6", "har7", "har8", "har9",
                "btn1", "btn2", "btn3", "btn4", "btn5", "btn6", "btn7", "btn8", "btn9"
        };

        ToggleGroup group = new ToggleGroup();
        GameRepository repo = GameRepository.getInstance();

        for (String btnId : possibleIds) {
            Node node = page.lookup("#" + btnId);
            if (node instanceof ToggleButton btn) {
                btn.setToggleGroup(group);
                String type = btnId.replaceAll("[0-9]", "").toLowerCase();
                String numStr = btnId.replaceAll("[^0-9]", "");
                int num = Integer.parseInt(numStr.isEmpty() ? "0" : numStr);

                ImageView btnIv = findImageView(btn.getGraphic());
                if (btnIv != null) btnIv.setMouseTransparent(true);

                // Gestione specifica per la categoria Sfondi
                if (type.startsWith("btn") || type.startsWith("bg")) {
                    configureBackgroundButton(btn, btnId, btnIv, group);
                    continue;
                }

                // Determinazione Indice di Rimozione (Ogni categoria ha un tasto "Nessuno")
                int lastButtonIndex = (type.startsWith("cap") || type.startsWith("dres") || type.startsWith("armor")) ? 6 : 9;

                String layerPath = null, iconPath = null, itemName = "Oggetto Sconosciuto";

                if (num != lastButtonIndex) {
                    ItemModel item = repo.getItem(type + num);
                    if (item != null) {
                        layerPath = item.getLayerPath(player.isMale());
                        iconPath = item.getIconPath();
                        itemName = item.getName();
                    }
                }

                // LOGICA DI EQUIPAGGIAMENTO: Attiva il toggle se l'oggetto è già addosso
                String currentEquippedPath = type.startsWith("cap") ? player.getHat() :
                        (type.startsWith("dres") ? player.getArmor() : player.getHair());
                if (isEquipped(layerPath, currentEquippedPath)) btn.setSelected(true);

                if (num == lastButtonIndex) {
                    configuraBottoneRimozione(btn, type, btnIv, group);
                } else {
                    // Controllo possesso oggetto (Inventario)
                    boolean isOwned = type.startsWith("har") || (type.startsWith("dres") && num <= 5) || repo.isItemOwned(type + num);

                    if (isOwned) {
                        if (btnIv != null) btnIv.setOpacity(1.0);
                        String finalLayer = layerPath, finalIcon = iconPath, finalName = itemName;
                        btn.setOnAction(e -> gestisciClickBottone(type, finalLayer, finalIcon, finalName, group));
                    } else {
                        // Effetto grafico per oggetti non posseduti (Disabilitati)
                        btn.setDisable(true);
                        if (btnIv != null) {
                            javafx.scene.effect.ColorAdjust darken = new javafx.scene.effect.ColorAdjust();
                            darken.setSaturation(-1.0);
                            btnIv.setEffect(darken);
                            btnIv.setOpacity(0.5);
                        }
                    }
                }
            }
        }
    }

    private boolean isEquipped(String buttonPath, String playerPath) {
        if (buttonPath == null) return playerPath == null || playerPath.trim().isEmpty();
        return buttonPath.equals(playerPath);
    }

    private void configuraBottoneRimozione(ToggleButton btn, String type, ImageView btnIv, ToggleGroup group) {
        btn.setDisable(false);
        String emptyName = type.equals("cap") ? "Nessun Elmo" : (type.equals("har") ? "Nessuna Acconciatura" : "Nessuna Armatura");
        String finalIconPath = "/org/example/ProgettoUIDFinal/imagini/Icon-" + (type.equals("cap") ? "helmet" : (type.equals("har") ? "hair" : "armor")) + ".png";
        btn.setOnAction(e -> gestisciClickBottone(type, null, finalIconPath, emptyName, group));
    }

    private void gestisciClickBottone(String type, String layerPath, String iconPath, String itemName, ToggleGroup group) {
        MusicManager.getInstance().playSoundEffect("dress-up.mp3");
        String safeName = (itemName != null) ? itemName : "Nessuno";
        if (type.equals("cap")) { player.setHat(layerPath); player.setHatIcon(iconPath); player.setHatName(safeName); }
        else if (type.equals("dres") || type.equals("armor")) { player.setArmor(layerPath); player.setArmorIcon(iconPath); player.setArmorName(safeName); }
        else if (type.equals("har")) { player.setHair(layerPath); player.setHairIcon(iconPath); player.setHairName(safeName); }
    }

    /**
     * GESTORE BACKGROUND: Utilizza il BackgroundService per cambiare il tema visivo
     * globale dell'applicazione.
     */
    private void configureBackgroundButton(ToggleButton btn, String btnId, ImageView btnIv, ToggleGroup group) {
        btn.setDisable(false);
        ItemModel item = GameRepository.getInstance().getItem(btnId);
        String bgPath = (item != null) ? item.getLayerPathFemale() : null;

        btn.setOnAction(e -> {
            if (bgPath != null) {
                BackgroundService.getInstance().setBackgroundByPath(bgPath);
                Image newBg = BackgroundService.getInstance().getBackground();
                applyBackground(closetRootPane, newBg);
                if (backgroundLayer != null) backgroundLayer.setImage(newBg);
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
        region.setBackground(new Background(new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs)));
    }

    private void initTooltipSystem() {
        sharedTooltip = new Tooltip();
        sharedTooltip.setShowDelay(Duration.ZERO);
        sharedTooltip.getStyleClass().add("tooltip-custom");
    }

    private void setupTooltip(ImageView target, ObservableValue<String> textProperty) {
        if (target == null) return;
        target.setOnMouseEntered(event -> {
            sharedTooltip.textProperty().bind(textProperty);
            sharedTooltip.show(target, event.getScreenX() + 15, event.getScreenY() + 15);
        });
        target.setOnMouseExited(event -> {
            sharedTooltip.hide();
            sharedTooltip.textProperty().unbind();
        });
    }

    /**
     * CONFERMA E SALVA: Rende permanenti le modifiche scrivendo lo stato sul file JSON.
     */
    @FXML
    public void Confirm(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("background_music.mp3");
        GameRepository.getInstance().saveGameToJSON();
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (currentStage != null && previousScene != null) currentStage.setScene(previousScene);
    }
}