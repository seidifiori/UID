package org.example.ProgettoUIDFinal;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.model.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ShopController implements Initializable {

    @FXML private StackPane centerHolder;
    @FXML private Label goldLabel, cartTotalLabel, dialogueLabel;
    @FXML private Button backButton;
    @FXML private ToggleButton hatButton, armorButton, powerUpsButton;

    private final ToggleGroup categoryGroup = new ToggleGroup();
    private Scene homeScene;

    // Lista centralizzata dei Power-Up per gestire la logica dei livelli (1, 2, 3)
    private final List<String> POWER_UPS = List.of("sword", "shield", "boots");

    private final Map<String, String> idToFxml = Map.of(
            "hatButton", "/org/example/ProgettoUIDFinal/shop-hats.fxml",
            "armorButton", "/org/example/ProgettoUIDFinal/shop-armors.fxml",
            "powerUpsButton", "/org/example/ProgettoUIDFinal/shop-powerUps.fxml"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (url != null && url.getPath().contains("Shop.fxml")) {
            MusicManager.getInstance().playMusic("shop.mp3");
            PlayerModel player = GameRepository.getInstance().getPlayer();

            if (goldLabel != null) goldLabel.textProperty().bind(player.goldProperty().asString());

            hatButton.setToggleGroup(categoryGroup);
            armorButton.setToggleGroup(categoryGroup);
            powerUpsButton.setToggleGroup(categoryGroup);

            hatButton.setSelected(true);
            setCenterFromFxml(idToFxml.get("hatButton"));
        }
    }

    private void setCenterFromFxml(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            loader.setController(this);
            Parent page = loader.load();
            centerHolder.getChildren().setAll(page);

            // Aggiorna tutti i componenti della nuova pagina tramite ricerca dinamica
            refreshShopUI(page);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Metodo dinamico: cerca tutti i ToggleButton nel nodo radice e li configura.
     * Elimina la necessità di liste manuali di bottoni.
     */
    private void refreshShopUI(Parent root) {
        GameRepository repo = GameRepository.getInstance();

        // lookupAll trova ogni bottone che usa la classe CSS .toggle-button
        for (Node node : root.lookupAll(".toggle-button")) {
            if (node instanceof ToggleButton btn) {
                String id = btn.getId().toLowerCase();
                boolean isMaxed;
                String resId = id;

                if (POWER_UPS.contains(id)) {
                    int lvl = repo.getPowCounts(id);
                    isMaxed = (lvl >= 3);
                    resId = id + (isMaxed ? "3" : (lvl + 1));
                } else {
                    isMaxed = repo.isItemOwned(id);
                }

                ItemModel item = repo.getItem(resId);
                if (item != null) {
                    // Sincronizzazione automatica: cerca label con ID "Price_NomeBottone"
                    Label pLabel = (Label) root.lookup("#Price_" + btn.getId());
                    if (pLabel != null) pLabel.setText(isMaxed ? "MAX" : String.valueOf(item.getPrice()));

                    // Sincronizzazione icone (per power-up)
                    ImageView icon = (ImageView) root.lookup("#Icon_" + btn.getId());
                    if (icon != null && item.getIconPath() != null) {
                        icon.setImage(new Image(getClass().getResourceAsStream(item.getIconPath())));
                    }

                    if (isMaxed) applySoldOutEffect(btn);
                    else btn.setOnAction(this::addToCart);
                }
            }
        }
    }

    @FXML
    private void addToCart(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        ToggleButton btn = (ToggleButton) event.getSource();
        String id = btn.getId().toLowerCase();

        // Determina il resourceId per l'item corrente o il prossimo livello
        String resId = POWER_UPS.contains(id)
                ? id + (GameRepository.getInstance().getPowCounts(id) + 1) : id;

        ItemModel item = GameRepository.getInstance().getItem(resId);
        int currentTotal = Integer.parseInt(cartTotalLabel.getText().isEmpty() ? "0" : cartTotalLabel.getText());

        if (btn.isSelected()) {
            cartTotalLabel.setText(String.valueOf(currentTotal + item.getPrice()));
            applySelectionEffect(btn);
        } else {
            cartTotalLabel.setText(String.valueOf(currentTotal - item.getPrice()));
            removeSelectionEffect(btn);
        }
    }

    @FXML
    private void confirmPurchase(ActionEvent event) {
        int spent = Integer.parseInt(cartTotalLabel.getText());
        PlayerModel player = GameRepository.getInstance().getPlayer();
        GameRepository repo = GameRepository.getInstance();

        if (spent == 0 || player.getGold() < spent) {
            playFeedback(spent == 0 ? "Il carrello è vuoto." : "Soldi insufficienti!", "no-funds.wav");
            return;
        }

        player.setGold(player.getGold() - spent);
        cartTotalLabel.setText("0");
        MusicManager.getInstance().playSoundEffect("item_bought.mp3");

        // Applica l'acquisto a tutti i bottoni selezionati nel centerHolder
        centerHolder.lookupAll(".toggle-button").forEach(node -> {
            ToggleButton b = (ToggleButton) node;
            if (b.isSelected()) {
                String id = b.getId().toLowerCase();
                if (POWER_UPS.contains(id)) {
                    int nextLvl = repo.getPowCounts(id) + 1;
                    applyPowerUpEffect(id + nextLvl, player);
                    repo.setPowCounts(id, nextLvl);
                } else {
                    repo.incrementItemCount(id);
                    player.addOwnedItem(id);
                }
            }
        });

        repo.saveGameToJSON();
        // Rinfresca la UI della pagina corrente (cast a Parent necessario)
        refreshShopUI((Parent) centerHolder.getChildren().get(0));
        playFeedback("Grazie per l'acquisto!", null);
    }

    private void applyPowerUpEffect(String id, PlayerModel player) {
        ItemModel item = GameRepository.getInstance().getItem(id);
        if (item == null) return;
        boolean male = player.isMale();

        switch (item.getType().toLowerCase()) {
            case "sword" -> {
                player.setAtk(player.getAtk() + 3);
                player.setSword(item.getLayerPath(male));
                player.setSwordIcon(item.getIconPath());
                player.setSwordName(item.getName());
            }
            case "shield" -> {
                player.setDef(player.getDef() + 3);
                player.setShield(item.getLayerPath(male));
                player.setShieldIcon(item.getIconPath());
                player.setShieldName(item.getName());
            }
            case "boots" -> player.setVel(player.getVel() + 3);
        }
    }

    private void applySoldOutEffect(ToggleButton btn) {
        if (btn.getGraphic() instanceof ImageView iv) {
            StackPane stack = new StackPane(new ImageView(iv.getImage()));
            Rectangle overlay = new Rectangle(iv.getFitWidth(), iv.getFitHeight(), Color.rgb(0, 0, 0, 0.7));
            Label label = new Label("SOLD OUT");
            label.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            stack.getChildren().addAll(overlay, label);
            btn.setGraphic(stack);
            btn.setDisable(true);
            btn.setSelected(false);
        }
    }

    private void playFeedback(String msg, String sound) {
        if (sound != null) MusicManager.getInstance().playSoundEffect(sound);
        dialogueLabel.setText(msg);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> dialogueLabel.setText("Cosa posso fare per te?"));
        pause.play();
    }

    @FXML private void handleMenu(ActionEvent e) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        String id = ((Node) e.getSource()).getId();
        if (idToFxml.containsKey(id)) setCenterFromFxml(idToFxml.get(id));
    }

    private void applySelectionEffect(ToggleButton b) { if (b.getGraphic() instanceof ImageView iv) iv.setEffect(new ColorAdjust(0,0,-0.5,0)); }
    private void removeSelectionEffect(ToggleButton b) { if (b.getGraphic() instanceof ImageView iv) iv.setEffect(null); }
    public void setHomeScene(Scene s) { this.homeScene = s; }
    @FXML public void goHome() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("background_music.mp3");
        if (homeScene != null) ((Stage) backButton.getScene().getWindow()).setScene(homeScene); }
}