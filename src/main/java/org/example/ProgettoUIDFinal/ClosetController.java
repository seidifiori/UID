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
import org.example.ProgettoUIDFinal.Services.GameRepository;
import org.example.ProgettoUIDFinal.model.ItemModel;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * SERVICE CONTROLLER - CLOSET SYSTEM: Gestisce la personalizzazione estetica del personaggio.
 * Implementa il caricamento dinamico di sotto-viste FXML e la logica di snapshot per il ripristino dei dati.
 */
public class ClosetController implements Initializable {

    // --- ELEMENTI UI (Iniezione FXML) ---
    @FXML private StackPane closetRootPane;
    @FXML private StackPane centerHolder;

    @FXML private Button BackButton;
    @FXML private Button genderButton;
    @FXML private ImageView genderImage;
    @FXML private  Button ConfirmButton;

    // Layer grafici sovrapposti per la composizione dinamica dello sprite
    @FXML private ImageView baseAvatarLayer;
    @FXML private ImageView hairLayer;
    @FXML private ImageView hatLayer;
    @FXML private ImageView armorLayer;
    @FXML private ImageView swordLayer;
    @FXML private ImageView shieldLayer;
    @FXML private ImageView backgroundLayer;
    @FXML private Scene homeScene;

    // Slot per le icone di riepilogo equipaggiamento
    @FXML private ImageView hatIcon;
    @FXML private ImageView hairIcon;
    @FXML private ImageView armorIcon;
    @FXML private ImageView swordIcon;
    @FXML private ImageView shieldIcon;

    // Controlli di navigazione tra categorie
    @FXML private ToggleButton hatButton;
    @FXML private ToggleButton armorButton;
    @FXML private ToggleButton hairButton;
    @FXML private ToggleButton backgroundButton;
    private final ToggleGroup toggleGroup = new ToggleGroup();

    private Tooltip sharedTooltip;
    private Scene previousScene;
    private PlayerModel player;

    // --- SNAPSHOT SYSTEM ---
    // Variabili per la memorizzazione temporanea dello stato iniziale
    private String snapshotBody;
    private String snapshotHat, snapshotArmor, snapshotHair, snapshotSword, snapshotShield;
    private String snapshotHatIcon, snapshotArmorIcon, snapshotHairIcon, snapshotSwordIcon, snapshotShieldIcon;
    private String snapshotHatName, snapshotArmorName, snapshotHairName, snapshotSwordName, snapshotShieldName;
    private boolean snapshotIsMale;
    private String snapshotBackgroundPath;

    // Percorso corrente del modulo caricato (Persistenza durante il cambio gender)
    private String currentFxmlPath = "/org/example/ProgettoUIDFinal/ClosetArmors.fxml";

    // Mapping ID componenti -> Risorsa FXML corrispondente
    private final Map<String, String> idToFxml = Map.of(
            "hatButton", "/org/example/ProgettoUIDFinal/ClosetHats.fxml",
            "armorButton", "/org/example/ProgettoUIDFinal/ClosetArmors.fxml",
            "hairButton", "/org/example/ProgettoUIDFinal/ClosetStyles.fxml",
            "backgroundButton", "/org/example/ProgettoUIDFinal/ClosetBackground.fxml"
    );

    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    /**
     * LIFE-CYCLE INITIALIZE: Configura i flussi audio, inizializza lo snapshot e attiva i binding.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MusicManager.getInstance().playMusic("closet.mp3");
        this.player = GameRepository.getInstance().getPlayer();

        // Backup dello stato del player per eventuale ripristino (Home)
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

        this.snapshotBackgroundPath = BackgroundService.getInstance().getCurrentBackgroundPath();
        if (this.snapshotBackgroundPath == null) {
            ItemModel defaultBg = GameRepository.getInstance().getItem("btn5");
            if (defaultBg == null) {
                defaultBg = GameRepository.getInstance().getItem("bg5");
            }
            if (defaultBg != null) {
                this.snapshotBackgroundPath = defaultBg.getLayerPathFemale();
                BackgroundService.getInstance().setBackgroundByPath(this.snapshotBackgroundPath);
            }
        }

        // CARICA IL LAYER CORRISPONDENTE AL BACKGROUND ATTUALE
        String currentBgPath = BackgroundService.getInstance().getCurrentBackgroundPath();
        if (currentBgPath != null && backgroundLayer != null) {
            // Cerca l'item con il percorso corrente
            for (int i = 1; i <= 9; i++) {
                String btnId = "btn" + i;
                ItemModel item = GameRepository.getInstance().getItem(btnId);

                if (item != null) {
                    String itemBgPath = item.getLayerPathFemale();
                    // Confronta i percorsi (pulendo eventuali virgolette)
                    String cleanCurrent = currentBgPath.replace("\"", "").trim();
                    String cleanItem = (itemBgPath != null) ? itemBgPath.replace("\"", "").trim() : "";

                    if (cleanCurrent.equals(cleanItem)) {
                        // Trovato l'item, ora cerca il layer corrispondente
                        String num = String.valueOf(i);
                        ItemModel layerItem = GameRepository.getInstance().getItem("layer" + num);

                        if (layerItem != null) {
                            String layerPath = layerItem.getBackgroundLayerPath();
                            if (layerPath != null && !layerPath.isEmpty()) {
                                try {
                                    InputStream is = getClass().getResourceAsStream(layerPath);
                                    if (is != null) {
                                        Image layerImage = new Image(is);
                                        backgroundLayer.setImage(layerImage);
                                        backgroundLayer.setPreserveRatio(true);
                                        backgroundLayer.setSmooth(true);
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error loading initial layer: " + e.getMessage());
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        // Applica lo sfondo completo al closetRootPane
        Image currentBgImage = BackgroundService.getInstance().getBackground();
        if (currentBgImage != null) {
            applyBackground(closetRootPane, currentBgImage);
        }

        hatButton.setToggleGroup(toggleGroup);
        armorButton.setToggleGroup(toggleGroup);
        hairButton.setToggleGroup(toggleGroup);
        backgroundButton.setToggleGroup(toggleGroup);
        addPreventDeselectionListener(toggleGroup);

        // Caricamento modulo UI predefinito
        setCenterFromFxml(idToFxml.get("hatButton"));
        hatButton.setSelected(true);

        updateGenderButtonUI();

        // DATA BINDING REATTIVO: Collega i layer grafici alle proprietà  del modello
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

        // Brevi descrizioni  informative basate sulle proprietà dei nomi oggetti
        setupTooltip(hatIcon, player.hatNameProperty());
        setupTooltip(hairIcon, player.hairNameProperty());
        setupTooltip(armorIcon, player.armorNameProperty());
        setupTooltip(swordIcon, player.swordNameProperty());
        setupTooltip(shieldIcon, player.shieldNameProperty());

        // Binding condizionale per la visibilità dei capelli (Occlusion logic)
        if (hairLayer != null) {
            hairLayer.visibleProperty().bind(player.isHairVisibleProperty());
        }
        BackButton.setCancelButton(true);
        ConfirmButton.setDefaultButton(true);
    }

    /**
     * GENDER SWAP LOGIC: Alterna l'identità di genere del player e aggiorna dinamicamente la view.
     */
    @FXML
    public void switchGender(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        player.toggleGender();
        updateGenderButtonUI();
        setCenterFromFxml(this.currentFxmlPath);
    }

    /**
     * UI REFRESH: Aggiorna l'asset grafico dell'icona del pulsante gender.
     */
    private void updateGenderButtonUI() {
        if (genderImage == null) return;

        String iconPath = player.isMale()
                ? "/org/example/ProgettoUIDFinal/imagini/Sprite-button-male.png"
                : "/org/example/ProgettoUIDFinal/imagini/Sprite-button-female.png";

        try {
            genderImage.setImage(new Image(getClass().getResourceAsStream(iconPath)));
        } catch (Exception e) {
            System.err.println("Impossibile caricare icona: " + iconPath);
        }
    }

    /**
     * Helper per l'implementazione del binding unidirezionale su ImageView.
     */
    private void bindLayer(ImageView view, javafx.beans.value.ObservableValue<? extends Image> prop) {
        if (view != null) view.imageProperty().bind(prop);
    }

    /**
     * EVENT HANDLER - CATEGORY MENU: Gestisce il caricamento dei sotto-moduli FXML.
     */
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
     * ROLLBACK NAVIGATION: Ripristina lo stato salvato nello snapshot e torna alla Home.
     */
    @FXML
    public void Home(ActionEvent event) {
        MusicManager.getInstance().playMusic("background_music.mp3");
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");

        // RIPRISTINO STATO ATOMICO: Sincronizzazione forzata con i dati dello snapshot
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

        if (this.snapshotBackgroundPath != null) {
            BackgroundService.getInstance().setBackgroundByPath(this.snapshotBackgroundPath);
        }

        Stage currentStage = (Stage) BackButton.getScene().getWindow();
        if (currentStage != null && previousScene != null) {
            currentStage.setScene(previousScene);
        }
    }

    /**
     * DYNAMIC FXML INJECTION: Carica un componente FXML esterno nel contenitore centrale.
     */
    private void setCenterFromFxml(String resourcePath) {
        this.currentFxmlPath = resourcePath;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            Parent page = loader.load();

            // Gestione della sovrapposizione nodi nello StackPane
            if (centerHolder.getChildren().size() > 1) {
                centerHolder.getChildren().remove(1);
            }

            centerHolder.getChildren().add(page);

            // Inizializzazione logica dei nodi
            findAndConfigureButtons(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * COMPONENT SCANNER: Identifica i componenti ToggleButton nel sotto-modulo e ne configura la logica.
     * Gestisce la disponibilità degli item (inventario), il sesso e il rendering delle icone.
     */
    private void findAndConfigureButtons(Parent page) {
        String[] possibleIds = {
                "cap1", "cap2", "cap3", "cap4", "cap5", "cap6",
                "dres1", "dres2", "dres3", "dres4", "dres5", "dres6",
                "har1", "har2", "har3", "har4", "har5", "har6", "har7", "har8", "har9",
                "btn1", "btn2", "btn3", "btn4", "btn5", "btn6", "btn7", "btn8", "btn9"
        };

        ToggleGroup group = new ToggleGroup();
        addPreventDeselectionListener(group); //Assicura che ci sia sempre almeno un bottone selezionato

        GameRepository repo = GameRepository.getInstance();
        PlayerModel player = repo.getPlayer();

        for (String btnId : possibleIds) {
            Node node = page.lookup("#" + btnId);

            if (node instanceof ToggleButton btn) {
                btn.setToggleGroup(group);

                // Parsing ID per determinare categoria e indice
                String type = btnId.replaceAll("[0-9]", "").toLowerCase();
                String numStr = btnId.replaceAll("[^0-9]", "");
                int num = 0;
                try { num = Integer.parseInt(numStr); } catch (Exception e) {}

                //Questo serve per evitare problemi di click
                ImageView btnIv = findImageView(btn.getGraphic());
                if (btnIv != null) btnIv.setMouseTransparent(true);

                // Gestione specifica per la categoria Backgrounds
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
                        // Recupera il path corretto in base al sesso attuale
                        layerPath = item.getLayerPath(player.isMale());

                        iconPath = item.getIconPath();
                        itemName = item.getName();
                    }
                }

                btn.setUserData(layerPath);

                // Sincronizzazione stato selezionato con l'attuale equipaggiamento del player
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
                    configureRemovalButton(btn, type, btnIv, group);
                } else {
                    String itemId = type + num;
                    boolean isOwned = false;

                    // Logica di validazione possesso item (Default vs Acquistati)
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

                        btn.setOnAction(e -> handleButtonClick(type, finalLayer, finalIcon, finalName, group));

                    } else {
                        // Applica filtri grafici per item non posseduti
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

    /**
     * Metodo helper per impedire la deselezione di un ToggleGroup.
     * Se l'utente clicca sul bottone già selezionato, questo rimane attivo.
     */
    private void addPreventDeselectionListener(ToggleGroup group) {
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
        });
    }

    /**
     * Helper per il confronto tra path risorsa (Null-safe).
     */
    private boolean isEquipped(String buttonPath, String playerPath) {
        if (buttonPath == null) {
            return playerPath == null || playerPath.trim().isEmpty();
        }
        if (playerPath == null) {
            return false;
        }
        return buttonPath.equals(playerPath);
    }

    /**
     * CONFIGURA RESET SLOT: Imposta il pulsante di rimozione equipaggiamento per categoria.
     */
    private void configureRemovalButton(ToggleButton btn, String type, ImageView btnIv, ToggleGroup group) {
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

        btn.setOnAction(e -> handleButtonClick(type, null, finalIconPath, finalEmptyName, group));
    }

    /**
     * DRESS-UP DISPATCHER: Aggiorna il modello del player con il nuovo asset selezionato.
     */
    private void handleButtonClick(String type, String layerPath, String iconPath, String itemName, ToggleGroup group) {
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

    /**
     * BACKGROUND DISPATCHER: Gestisce il cambio dello sfondo globale tramite BackgroundService.
     */
    private void configureBackgroundButton(ToggleButton btn, String btnId, ImageView btnIv, ToggleGroup group) {
        btn.setDisable(false);
        if (btnIv != null) btnIv.setOpacity(1.0);

        // Ottieni l'item per lo sfondo completo
        ItemModel item = GameRepository.getInstance().getItem(btnId);

        // Estrai il numero dal btnId
        String num = btnId.replaceAll("[^0-9]", "");

        // Cerca l'item per il layer (ID "layer" + numero)
        ItemModel layerItem = GameRepository.getInstance().getItem("layer" + num);

        String bgPath = (item != null) ? item.getLayerPathFemale() : null;
        String layerPath = (layerItem != null) ? layerItem.getBackgroundLayerPath() : null;

        String currentGlobalPath = BackgroundService.getInstance().getCurrentBackgroundPath();

        if ((currentGlobalPath == null || currentGlobalPath.isEmpty())) {
            if (btnId.equals("btn5")) {
                btn.setSelected(true);
            }
        } else if (isEquipped(bgPath, currentGlobalPath)) {
            // Se c'è un percorso salvato, usiamo la logica standard
            btn.setSelected(true);
        }

        btn.setOnAction(e -> {
            if (bgPath != null) {
                // 1. Aggiorna lo sfondo globale
                BackgroundService.getInstance().setBackgroundByPath(bgPath);

                // AGGIORNA IL MODELLO
                player.setBackgroundPath(bgPath);

                // 2. Carica il layer
                if (layerPath != null && backgroundLayer != null) {
                    try {
                        InputStream is = getClass().getResourceAsStream(layerPath);
                        if (is != null) {
                            Image layerImage = new Image(is);
                            backgroundLayer.setImage(layerImage);
                            backgroundLayer.setPreserveRatio(true);
                            backgroundLayer.setSmooth(true);
                        } else {
                            // Prova percorso alternativo
                            String altPath = "/org/example/ProgettoUIDFinal/imagini/Backgrounds/layers/" +
                                    bgPath.substring(bgPath.lastIndexOf("/") + 1);
                            InputStream altIs = getClass().getResourceAsStream(altPath);
                            if (altIs != null) {
                                backgroundLayer.setImage(new Image(altIs));
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Error: " + ex.getMessage());
                    }
                }

                // 3. Mantieni lo sfondo del closetRootPane
                Image newBg = BackgroundService.getInstance().getBackground();
                applyBackground(closetRootPane, newBg);
            }
        });
    }

    /**
     * RECURSIVE SEARCH: Cerca un'istanza di ImageView all'interno di un nodo (utile se annidato).
     */
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

    /**
     * UI RENDERING: Applica programmaticamente lo sfondo a un contenitore Region.
     */
    private void applyBackground(Region region, Image image) {
        if (region == null || image == null) return;
        BackgroundSize bs = new BackgroundSize(1.0, 1.0, true, true, false, true);
        BackgroundImage bi = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, bs);
        region.setBackground(new Background(bi));
    }

    /**
     * TOOLTIP BOOTSTRAP: Inizializza il sistema di tooltip personalizzati.
     */
    private void initTooltipSystem() {
        sharedTooltip = new Tooltip();
        sharedTooltip.setShowDelay(Duration.ZERO);
        sharedTooltip.setHideDelay(Duration.ZERO);
        sharedTooltip.getStyleClass().add("tooltip-custom");
    }
    /**
     * TOOLTIP BINDING: Collega gli eventi mouse di un'icona alla visualizzazione del tooltip.
     */
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
    /**
     * DATA PERSISTENCE - CONFIRM: Rende permanenti le modifiche via JSON e torna alla Home.
     */
    @FXML
    public void Confirm(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("background_music.mp3");

        GameRepository.getInstance().saveGameToJSON();

        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (currentStage != null && previousScene != null) {
            currentStage.setScene(previousScene);
        }
    }

    @FXML
    public void removeAll(ActionEvent e) {
        String basePath = "/org/example/ProgettoUIDFinal/imagini/";

        player.setHat("");
        player.setHatName("Nessun Elmo");
        player.setHatIcon(basePath + "Icon-helmet.png");

        player.setArmor("");
        player.setArmorName("Nessuna Armatura");
        player.setArmorIcon(basePath + "Icon-armor.png");

        player.setHair("");
        player.setHairName("Nessuna Acconciatura");
        player.setHairIcon(basePath + "Icon-hair.png");

        setCenterFromFxml(this.currentFxmlPath);
    }

    @FXML
    public void removeWeapons(ActionEvent e) {
        GameRepository repo = GameRepository.getInstance();

        int idSword = repo.getPowCounts("sword");
        int idShield = repo.getPowCounts("shield");

        String basePath = "/org/example/ProgettoUIDFinal/imagini/";
        if (player.getSword() != null && !player.getSword().isEmpty()) {
            player.setSword("");
            player.setSwordName("Nessuna Spada");
            player.setSwordIcon(basePath + "Icon-sword.png");
        } else {
            ItemModel swordItem = repo.getItem("sword" + idSword);

            player.setSword(swordItem.getLayerPath(player.isMale()));
            player.setSwordName(swordItem.getName());
            player.setSwordIcon(swordItem.getIconPath());
        }

        if (player.getShield() != null && !player.getShield().isEmpty()) {
            player.setShield("");
            player.setShieldName("Nessuno Scudo");
            player.setShieldIcon(basePath + "Icon-shield.png");
        } else {
            ItemModel shieldItem = repo.getItem("shield" + idShield);

            player.setShield(shieldItem.getLayerPath(player.isMale()));
            player.setShieldName(shieldItem.getName());
            player.setShieldIcon(shieldItem.getIconPath());
        }
    }

    @FXML
    public void equipRandomCurrentCategory(ActionEvent event) {
        String prefix = "";
        int maxItems = 0;

        // 1. Identifica la categoria corrente
        if (hatButton.isSelected()) {
            prefix = "cap";
            maxItems = 6;
        } else if (armorButton.isSelected()) {
            prefix = "dres";
            maxItems = 6;
        } else if (hairButton.isSelected()) {
            prefix = "har";
            maxItems = 9;
        } else {
            return;
        }

        // 2. Costruisci la lista degli oggetti posseduti
        List<ItemModel> validItems = new ArrayList<>();
        GameRepository repo = GameRepository.getInstance();

        for (int i = 1; i <= maxItems; i++) {
            String id = prefix + i;
            // Logica di possesso (i capelli e i default sono sempre inclusi)
            boolean isOwned = id.startsWith("har") || repo.isItemOwned(id);
            if ((id.equals("cap4") || id.equals("cap5") || id.equals("dres4") || id.equals("dres5"))) {
                isOwned = true;
            }

            if (isOwned) {
                ItemModel item = repo.getItem(id);
                if (item != null) validItems.add(item);
            }
        }

        int randomIndex = new Random().nextInt(validItems.size() + 1);

        if (randomIndex == validItems.size()) {
            // CASO "NESSUNO": Disequipaggia l'oggetto della categoria corrente
            if (prefix.equals("cap")) {
                player.setHat(null);
                player.setHatName("Nessun Elmo");
                player.setHatIcon("/org/example/ProgettoUIDFinal/imagini/Icon-helmet.png");
                setCenterFromFxml(this.currentFxmlPath);

            } else if (prefix.equals("dres")) {
                player.setArmor(null);
                player.setArmorName("Nessuna Armatura");
                player.setArmorIcon("/org/example/ProgettoUIDFinal/imagini/Icon-armor.png");
                setCenterFromFxml(this.currentFxmlPath);

            } else if (prefix.equals("har")) {
                player.setHair(null);
                player.setHairName("Calvo");
                player.setHairIcon("/org/example/ProgettoUIDFinal/imagini/Icon-hair.png");
                setCenterFromFxml(this.currentFxmlPath);

            }
            System.out.println("Randomizzato: NESSUNO");

        } else {
            // CASO NORMALE: Equipaggia l'oggetto estratto
            ItemModel randomItem = validItems.get(randomIndex);

            if (prefix.equals("cap")) {
                player.setHat(randomItem.getLayerPath(player.isMale()));
                player.setHatName(randomItem.getName());
                player.setHatIcon(randomItem.getIconPath());
                setCenterFromFxml(this.currentFxmlPath);

            } else if (prefix.equals("dres")) {
                player.setArmor(randomItem.getLayerPath(player.isMale()));
                player.setArmorName(randomItem.getName());
                player.setArmorIcon(randomItem.getIconPath());

                setCenterFromFxml(this.currentFxmlPath);

            } else if (prefix.equals("har")) {
                player.setHair(randomItem.getLayerPath(player.isMale()));
                player.setHairName(randomItem.getName());
                player.setHairIcon(randomItem.getIconPath());
                setCenterFromFxml(this.currentFxmlPath);
            }
            System.out.println("Randomizzato: " + randomItem.getName());
        }

        MusicManager.getInstance().playSoundEffect("dress-up.mp3");
    }

    public void randomWeapon() {
        int rand = new Random().nextInt(4);

        GameRepository repo = GameRepository.getInstance();
        ItemModel randSword = repo.getItem("sword" + rand);
        ItemModel randShield = repo.getItem("shield" + rand);

        player.setSword(randSword.getLayerPath(player.isMale()));
        player.setSwordIcon(randSword.getIconPath());
        player.setSwordName(randSword.getName());

        player.setShield(randShield.getLayerPath(player.isMale()));
        player.setShieldIcon(randShield.getIconPath());
        player.setShieldName(randShield.getName());

    }

    /*
    @FXML
    public void handleUnlockAll(ActionEvent event) {
        // 1. Sblocca i dati a livello di Repository
        GameRepository.getInstance().unlockAllItems();
        setCenterFromFxml(this.currentFxmlPath);
    }*/

}