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

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class ClosetController implements Initializable {

    @FXML private StackPane closetRootPane;
    @FXML private StackPane centerHolder;

    @FXML private Button BackButton;
    @FXML private ImageView baseAvatarLayer;
    @FXML private ImageView hairLayer;
    @FXML private ImageView hatLayer;
    @FXML private ImageView armorLayer;
    @FXML private ImageView swordLayer;
    @FXML private ImageView shieldLayer;
    @FXML private ImageView backgroundLayer;

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

    private Scene previousScene;
    private PlayerModel player;

    private final Map<String, String> idToFxml = Map.of(
            "hatButton", "/org/example/ProgettoUIDFinal/page_crown.fxml",
            "armorButton", "/org/example/ProgettoUIDFinal/page_shirt.fxml",
            "hairButton", "/org/example/ProgettoUIDFinal/page_hair.fxml",
            "backgroundButton", "/org/example/ProgettoUIDFinal/page_art.fxml"
    );

    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MusicManager.getInstance().playMusic("closet.mp3");
        this.player = GameRepository.getInstance().getPlayer();

        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null) {
            applyBackground(closetRootPane, currentBg); // Sfondo globale
            if (backgroundLayer != null) {
                backgroundLayer.setImage(currentBg);    // Aggiorna l'immagine dietro l'avatar
            }
        }

        hatButton.setToggleGroup(toggleGroup);
        armorButton.setToggleGroup(toggleGroup);
        hairButton.setToggleGroup(toggleGroup);
        backgroundButton.setToggleGroup(toggleGroup);

        setCenterFromFxml(idToFxml.get("armorButton"));

        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.swordImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());

        bindLayer(hatIcon, player.hatIconProperty());   // helmetIcon -> Hat
        bindLayer(hairIcon,   player.hairIconProperty());  // hairIcon   -> Hair
        bindLayer(armorIcon,  player.armorIconProperty()); // armorIcon  -> Armor
        bindLayer(swordIcon,  player.swordIconProperty());// swordIcon  -> Weapon
        bindLayer(shieldIcon, player.shieldIconProperty());// shieldIcon -> Shield

        if (hairLayer != null) {
            hairLayer.visibleProperty().bind(player.isHairVisibleProperty());
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

            // LOGICA PER GESTIRE I FIGLI DELLO STACKPANE:

            // 1. Controlliamo se c'è già una pagina caricata sopra lo sfondo.
            // Lo sfondo è l'elemento 0. Se c'è un elemento 1, è la vecchia pagina (es. camicie)
            // e dobbiamo rimuoverla prima di mettere quella nuova (es. armature).
            if (centerHolder.getChildren().size() > 1) {
                centerHolder.getChildren().remove(1); // Rimuove l'elemento sopra lo sfondo
            }

            // 2. Aggiungiamo la nuova pagina sopra lo sfondo
            centerHolder.getChildren().add(page);

            // 3. Configuriamo i bottoni della nuova pagina caricata
            trovaEConfiguraBottoni(page);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ... tieni gli import e le variabili in alto uguali ...

    private void trovaEConfiguraBottoni(Parent page) {
        String[] possibleIds = {
                "cap1", "cap2", "cap3", "cap4", "cap5", "cap6",
                "dres1", "dres2", "dres3", "dres4",
                "har1", "har2", "har3", "har4", "har5", "har6", "har7",
                "btn1", "btn2", "btn3", "btn4", "btn5", "btn6", "btn7", "btn8", "btn9"
        };

        ToggleGroup group = new ToggleGroup();
        GameRepository repo = GameRepository.getInstance();

        for (String btnId : possibleIds) {
            Node node = page.lookup("#" + btnId);

            if (node instanceof ToggleButton btn) {
                btn.setToggleGroup(group);

                // Analizziamo l'ID
                String type = btnId.replaceAll("[0-9]", "").toLowerCase();
                String numStr = btnId.replaceAll("[^0-9]", "");
                int num = 0;
                try { num = Integer.parseInt(numStr); } catch (Exception e) {}

                ImageView btnIv = findImageView(btn.getGraphic());
                if (btnIv != null) btnIv.setMouseTransparent(true);

                // --- GESTIONE BACKGROUND ---
                if (type.startsWith("btn") || type.startsWith("bg")) {
                    configureBackgroundButton(btn, btnId, btnIv, group);
                    continue;
                }

                // --- GESTIONE EQUIPAGGIAMENTO ---

                // 1. DEFINIAMO L'ULTIMO BOTTONE (Tasto Rimuovi/X)
                int lastButtonIndex = 9;

                if (type.startsWith("cap")) {
                    lastButtonIndex = 6; // Cap6 è la X
                } else if (type.startsWith("dres") || type.startsWith("armor")){
                    lastButtonIndex = 4;
                } else if (type.startsWith("har")) {
                    lastButtonIndex = 7;
                }

                // CASO A: TASTO "RIMUOVI" (L'ultimo della serie)
                if (num == lastButtonIndex) {
                    configuraBottoneRimozione(btn, type, btnIv, group);
                }
                // CASO B: BOTTONI NORMALI
                else {
                    String itemId = type + num; // es: cap1, har1

                    // --- LOGICA DI POSSESSO MODIFICATA ---
                    boolean isOwned = false;

                    if (type.startsWith("har")) {
                        // I capelli sono sempre sbloccati
                        isOwned = true;
                    } else if (type.startsWith("cap")) {
                        // CAPPELLI: 4 e 5 sono GRATIS, gli altri check Repo
                        if (num == 4 || num == 5) {
                            isOwned = true;
                        } else {
                            isOwned = repo.isItemOwned(itemId);
                        }
                    } else {
                        // Vestiti e altro: check Repo standard
                        isOwned = repo.isItemOwned(itemId);
                    }

                    if (isOwned) {
                        // --- POSSEDUTO (O GRATIS) ---
                        btn.setDisable(false);
                        if (btnIv != null) {
                            btnIv.setOpacity(1.0);
                            btnIv.setEffect(null);
                        }

                        ItemModel item = repo.getItem(itemId);
                        if (item != null) {
                            try {
                                Image img = new Image(getClass().getResourceAsStream(item.getIconPath()));
                                if (btnIv != null) btnIv.setImage(img);

                                String layerPath = item.getLayerPath();
                                String iconPath = item.getIconPath();
                                btn.setOnAction(e -> gestisciClickBottone(type, layerPath, iconPath, group));

                            } catch (Exception e) {
                                System.err.println("Errore caricamento item: " + itemId);
                            }
                        }
                    } else {
                        // --- NON POSSEDUTO ---
                        btn.setDisable(true); // Disabilita click

                        // Effetto "Locked" (scuro/grigio)
                        if (btnIv != null) {
                            javafx.scene.effect.ColorAdjust darken = new javafx.scene.effect.ColorAdjust();
                            darken.setBrightness(-0.7);
                            darken.setSaturation(-1.0);
                            btnIv.setEffect(darken);
                            btnIv.setOpacity(0.5);

                            // Carica comunque l'icona per far vedere cosa ti perdi
                            ItemModel item = repo.getItem(itemId);
                            if (item != null) {
                                try {
                                    btnIv.setImage(new Image(getClass().getResourceAsStream(item.getIconPath())));
                                } catch (Exception e) {}
                            }
                        }
                    }
                }
            }
        }
    }

    private void configuraBottoneRimozione(ToggleButton btn, String type, ImageView btnIv, ToggleGroup group) {
        btn.setDisable(false); // Sempre cliccabile
        if (btnIv != null) btnIv.setOpacity(1.0);

        // CARICA ICONA "X" o "RIMUOVI"
        // Assicurati di avere un'immagine "remove_icon.png" o simile.
        // Se non ce l'hai, per ora lasciamo l'immagine che hai messo in SceneBuilder
        try {
            // Esempio: Image removeImg = new Image(getClass().getResourceAsStream("/org/example/ProgettoUIDFinal/imagini/remove_icon.png"));
            // if (btnIv != null) btnIv.setImage(removeImg);
        } catch (Exception e) { }

        // AZIONE: Passiamo null come path per rimuovere l'oggetto
        btn.setOnAction(e -> gestisciClickBottone(type, null, null, group));
    }

    private void gestisciClickBottone(String type, String layerPath, String iconPath, ToggleGroup group) {
        // Debug: controlla se il click arriva davvero
        System.out.println("Click ricevuto: Tipo=" + type + " Path=" + layerPath);

        MusicManager.getInstance().playSoundEffect("change_screen.mp3"); // Aggiunto feedback sonoro

        if (type.equals("cap")) {
            // Nota: Se layerPath è null (bottone rimozione), PlayerModel deve saperlo gestire!
            player.setHat(layerPath);
            player.setHatIcon(iconPath);
        }
        else if (type.equals("dres") || type.equals("armor")) {
            player.setArmor(layerPath);
            player.setArmorIcon(iconPath);
        }
        else if (type.equals("har")) {
            player.setHair(layerPath);
            player.setHairIcon(iconPath);
        }
    }

    private void configureBackgroundButton(ToggleButton btn, String btnId, ImageView btnIv, ToggleGroup group) {
        btn.setDisable(false);
        if (btnIv != null) btnIv.setOpacity(1.0);

        // Prendiamo l'immagine direttamente dall'icona del bottone
        Image bgImg = (btnIv != null) ? btnIv.getImage() : null;

        btn.setOnAction(e -> {
            if (bgImg != null) {
                // 1. Cambia lo sfondo globale del root (per riempire i bordi)
                applyBackground(closetRootPane, bgImg);

                // 2. Salva la scelta nel servizio globale (così rimane nelle altre schermate)
                BackgroundService.getInstance().setBackground(bgImg);

                // 3. Cambia immediatamente l'immagine dietro l'avatar
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
}