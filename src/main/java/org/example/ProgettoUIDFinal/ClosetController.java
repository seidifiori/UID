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
import org.example.ProgettoUIDFinal.model.PlayerModel; // IMPORTANTE

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

    // Riferimento al Modello (Il cervello centrale)
    private PlayerModel player;

    private final Map<String, String> idToFxml = Map.of(
            "crownBtn", "/org/example/ProgettoUIDFinal/page_crown.fxml",
            "shirtBtn", "/org/example/ProgettoUIDFinal/page_shirt.fxml",
            "talkBtn", "/org/example/ProgettoUIDFinal/page_hair.fxml",
            "artBtn", "/org/example/ProgettoUIDFinal/page_art.fxml"
    );

    public void setPreviousScene(Scene scene) { this.previousScene = scene; }

    // NOTA BENE: Ho cancellato setHomeHatImage. NON SERVE PIÙ.
    // Il binding fa tutto il lavoro sporco.

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MusicManager.getInstance().playMusic("closet.mp3");
        // 1. Recuperiamo il player dal repository
        this.player = GameRepository.getInstance().getPlayer();

        // 2. Sfondo (ok, va bene)
        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null) {
            applyBackground(closetRootPane, currentBg);
        }

        // 3. Carichiamo la prima pagina
        setCenterFromFxml(idToFxml.get("crownBtn"));

        // 4. BINDING FONDAMENTALE
        // Diciamo alle immagini: "Qualunque cosa succeda nel Modello, copiala qui."
        if (HatImage != null) {
            HatImage.imageProperty().bind(player.hatImageProperty());
        }

        // Se hai anche un'armatura/vestito
        if (HelmetImage != null) {
            HelmetImage.imageProperty().bind(player.armorImageProperty());
        }
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

        // Array degli ID (rimane uguale)
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

                // Definiamo itemId QUI, così è visibile per tutto il blocco if
                String itemId = btnId.toLowerCase();

                ImageView btnIv = findImageView(btn.getGraphic());
                if (btnIv != null) btnIv.setMouseTransparent(true);

                boolean isBackground = itemId.startsWith("btn") || itemId.startsWith("bg");
                boolean isUnlocked = isBackground || repo.isItemOwned(itemId);

                if (isUnlocked) {
                    btn.setDisable(false);
                    if (btnIv != null) btnIv.setOpacity(1.0);

                    // --- LOGICA OGGETTI (CAPPELLI/ARMARTURE) ---
                    ItemModel item = repo.getItem(itemId);

                    if (item != null && item.getImagePath() != null) {
                        try {
                            Image img = new Image(getClass().getResourceAsStream(item.getImagePath()));
                            if (btnIv != null) btnIv.setImage(img);

                            // Definiamo il path qui per passarlo alla lambda
                            String path = item.getImagePath();

                            // === LA CORREZIONE È QUI ===
                            // 1. Usiamo le variabili itemId, path, img che sono definite in questo scope.
                            // 2. Passiamo 'group' come QUARTO argomento.
                            btn.setOnAction(e -> gestisciClickBottone(itemId, path, img, group));

                        } catch (Exception e) {
                            System.err.println("Errore caricamento immagine item: " + itemId);
                        }
                    }
                    // --- LOGICA BACKGROUND ---
                    else if (isBackground) {
                        // Per i background non abbiamo un ItemModel o un path nello stesso modo.
                        // Passiamo null come path, ma passiamo il gruppo.
                        // NOTA: Se 'gestisciClickBottone' si aspetta un'immagine per lo sfondo,
                        // devi assicurarti di caricarla qui o che 'img' non sia null.

                        // Tentativo di caricare l'immagine se è nel bottone
                        Image bgImg = (btnIv != null) ? btnIv.getImage() : null;

                        btn.setOnAction(e -> gestisciClickBottone(itemId, null, bgImg, group));
                    }

                } else {
                    // --- BLOCCATO ---
                    btn.setDisable(true);
                    if (btnIv != null) btnIv.setOpacity(0.5);
                }
            }
        }
    }



    // Ho cambiato la firma del metodo per accettare il PATH
    // Nota: Ho aggiunto il parametro ToggleGroup group alla fine
    private void gestisciClickBottone(String itemId, String itemPath, Image img, ToggleGroup group) {

        if (itemId.startsWith("cap")) {
            // 1. Controlliamo se stiamo cliccando sul cappello che ABBIAMO GIÀ
            String currentHat = player.hatPathProperty().get();

            if (currentHat != null && currentHat.equals(itemPath)) {
                // CASO "TOGLI": Clicco su quello che ho già -> Lo rimuovo
                player.setHat(null);

                // Visivamente deselezioniamo il bottone nel gruppo
                if (group.getSelectedToggle() != null) {
                    group.getSelectedToggle().setSelected(false);
                }
                System.out.println("Cappello rimosso.");
            } else {
                // CASO "METTI": È un cappello diverso -> Lo indosso
                player.setHat(itemPath);
            }
        }
        else if (itemId.startsWith("dres") || itemId.startsWith("armor")) {
            // Stessa logica per l'armatura
            String currentArmor = player.armorPathProperty().get();

            if (currentArmor != null && currentArmor.equals(itemPath)) {
                player.setArmor(null); // Togli
                if (group.getSelectedToggle() != null) {
                    group.getSelectedToggle().setSelected(false);
                }
            } else {
                player.setArmor(itemPath); // Metti
            }
        }
        else if (itemId.startsWith("btn") || itemId.startsWith("art") || itemId.startsWith("bg")) {
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
        BackgroundImage bi = new BackgroundImage(image,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                bs);
        region.setBackground(new Background(bi));
    }
}