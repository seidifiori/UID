package org.example.ProgettoUIDFinal;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust; // Importante per l'effetto scuro
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
            goldLabel.textProperty().bind(GameRepository.getInstance().getPlayer().goldProperty().asString());

            hatButton.setToggleGroup(categoryGroup);
            armorButton.setToggleGroup(categoryGroup);
            powerUpsButton.setToggleGroup(categoryGroup);

            hatButton.setSelected(true);
            loadPage(idToFxml.get("hatButton"));
        }
    }

    private void loadPage(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            loader.setController(this);
            Parent root = loader.load();
            centerHolder.getChildren().setAll(root);
            refreshUI(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // --- AGGIORNAMENTO GRAFICA ---
    private void refreshUI(Parent root) {
        GameRepository repo = GameRepository.getInstance();

        for (Node node : root.lookupAll(".toggle-button")) {
            if (node instanceof ToggleButton btn) {
                String id = btn.getId();

                int count = POWER_UPS.contains(id) ? repo.getPowCounts(id) : (repo.isItemOwned(id) ? 1 : 0);
                boolean isMaxed = POWER_UPS.contains(id) ? count >= 3 : count > 0;
                String resId = POWER_UPS.contains(id) ? id + (isMaxed ? "3" : (count + 1)) : id;

                ItemModel item = repo.getItem(resId);

                // DATI DI FALLBACK (Se item è null)
                int priceVal = (item != null) ? item.getPrice() : getFallbackPrice(resId);
                String iconPath = (item != null) ? item.getIconPath() : getFallbackIcon(resId);

                // Aggiorna Prezzo
                Label priceLabel = (Label) root.lookup("#Price_" + id);
                if (priceLabel != null) {
                    priceLabel.setText(isMaxed ? "MAX" : String.valueOf(priceVal));
                }

                // Aggiorna Immagine
                ImageView icon = getIcon(btn);
                if (icon != null && iconPath != null) {
                    try {
                        icon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
                    } catch (Exception e) {
                        System.err.println("Immagine non trovata: " + iconPath);
                    }
                }

                if (isMaxed) {
                    setSoldOut(btn, icon);
                } else {
                    if (btn.getGraphic() instanceof StackPane) btn.setGraphic(icon);
                    btn.setDisable(false);
                    btn.setSelected(false);
                    // IMPORTANTE: Rimuove l'effetto scuro se la pagina viene ricaricata
                    removeSelectionEffect(btn);
                    btn.setOnAction(this::addToCart);
                }
            }
        }
    }

    // --- LOGICA AGGIUNTA AL CARRELLO (CON EFFETTO SCURO) ---
    @FXML private void addToCart(ActionEvent e) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        ToggleButton btn = (ToggleButton) e.getSource();
        String id = btn.getId();

        String resId;

        if (POWER_UPS.contains(id)) {
            int current = GameRepository.getInstance().getPowCounts(id);
            int next = (current >= 3) ? 3 : current + 1;
            resId = id + next;
        } else {
            resId = id;
        }

        ItemModel item = GameRepository.getInstance().getItem(resId);

        int price = (item != null) ? item.getPrice() : getFallbackPrice(resId);

        int total = Integer.parseInt(cartTotalLabel.getText().isEmpty() ? "0" : cartTotalLabel.getText());

        if (btn.isSelected()) {
            cartTotalLabel.setText(String.valueOf(total + price));
            // APPLICA L'EFFETTO SCURO
            applySelectionEffect(btn);
        } else {
            cartTotalLabel.setText(String.valueOf(total - price));
            // RIMUOVI L'EFFETTO SCURO
            removeSelectionEffect(btn);
        }
    }

    // --- NUOVI METODI PER L'EFFETTO VISIVO ---

    private void applySelectionEffect(ToggleButton btn) {
        ImageView icon = getIcon(btn);
        if (icon != null) {
            ColorAdjust darken = new ColorAdjust();
            darken.setBrightness(-0.5); // Scurisce del 50%
            icon.setEffect(darken);
        }
    }

    private void removeSelectionEffect(ToggleButton btn) {
        ImageView icon = getIcon(btn);
        if (icon != null) {
            icon.setEffect(null); // Rimuove ogni effetto
        }
    }

    // --- DATI MANUALI DI FALLBACK ---

    private int getFallbackPrice(String resId) {
        if (resId.endsWith("1")) return 250;
        if (resId.endsWith("2")) return 500;
        if (resId.endsWith("3")) return 1000;
        if (resId.contains("dres")) return 500;
        if (resId.contains("cap")) return 200;
        return 0;
    }

    private String getFallbackIcon(String resId) {
        String basePath = "/org/example/ProgettoUIDFinal/imagini/weapons/";
        if (resId.startsWith("sword")) {
            if (resId.endsWith("1")) return basePath + "sword-lv-2.png";
            if (resId.endsWith("2")) return basePath + "sword-lv-3.png";
            if (resId.endsWith("3")) return basePath + "sword-lv-4.png";
        }
        if (resId.startsWith("shield")) {
            if (resId.endsWith("1")) return basePath + "shield-lv-2.png";
            if (resId.endsWith("2")) return basePath + "shield-lv-3.png";
            if (resId.endsWith("3")) return basePath + "shield-lv-4.png";
        }
        if (resId.startsWith("boots")) {
            if (resId.endsWith("1")) return basePath + "boots-lv-2.png";
            if (resId.endsWith("2")) return basePath + "boots-lv-3.png";
            if (resId.endsWith("3")) return basePath + "boots-lv-4.png";
        }
        return null;
    }

    // --- METODI HELPER ---

    private ImageView getIcon(ToggleButton btn) {
        if (btn.getGraphic() instanceof ImageView iv) return iv;
        if (btn.getGraphic() instanceof StackPane sp) {
            for (Node n : sp.getChildren()) if (n instanceof ImageView iv) return iv;
        }
        return null;
    }

    private void setSoldOut(ToggleButton btn, ImageView icon) {
        StackPane sp = new StackPane(new ImageView(icon.getImage()));
        Rectangle rect = new Rectangle(icon.getFitWidth(), icon.getFitHeight(), Color.rgb(0,0,0,0.7));
        Label l = new Label("SOLD OUT"); l.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        sp.getChildren().addAll(rect, l);
        btn.setGraphic(sp);
        btn.setDisable(true);
    }

    @FXML private void confirmPurchase(ActionEvent e) {
        int cost = Integer.parseInt(cartTotalLabel.getText());
        PlayerModel p = GameRepository.getInstance().getPlayer();
        GameRepository r = GameRepository.getInstance();

        if (p.getGold() < cost) { msg("Fondi insufficienti!");
            MusicManager.getInstance().playSoundEffect("no-funds.wav");
            return; }
        if (cost == 0) { msg("Il carrello è vuoto!");
            MusicManager.getInstance().playSoundEffect("no-funds.wav");
            return; }

        p.setGold(p.getGold() - cost);
        cartTotalLabel.setText("0");
        MusicManager.getInstance().playSoundEffect("item_bought.mp3");

        centerHolder.lookupAll(".toggle-button").forEach(node -> {
            ToggleButton btn = (ToggleButton) node;
            if (btn.isSelected()) {
                String id = btn.getId();
                if (POWER_UPS.contains(id)) {
                    int next = r.getPowCounts(id) + 1;
                    r.setPowCounts(id, next);
                    applyStats(id + next, p);
                } else {
                    r.incrementItemCount(id);
                    p.addOwnedItem(id);
                }
                // Rimuove la selezione dopo l'acquisto
                btn.setSelected(false);
                removeSelectionEffect(btn);
            }
        });
        r.saveGameToJSON();
        refreshUI((Parent) centerHolder.getChildren().get(0));
        msg("Acquisto completato!");
        MusicManager.getInstance().playSoundEffect("item_bought.mp3");
    }

    private void applyStats(String id, PlayerModel p) {
        ItemModel item = GameRepository.getInstance().getItem(id);

        if (id.startsWith("sword")) {
            p.setAtk(p.getAtk()+2);
            if(item != null) p.setSword(item.getLayerPath(p.isMale()));
        }
        if (id.startsWith("shield")) {
            p.setDef(p.getDef()+2);
            if(item != null) p.setShield(item.getLayerPath(p.isMale()));
        }
        if (id.startsWith("boots")) p.setVel(p.getVel()+2);
    }

    private void msg(String t) {
        dialogueLabel.setText(t);
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(ev -> dialogueLabel.setText("Come posso aiutarti oggi?!"));
        pt.play();
    }

    @FXML private void handleMenu(ActionEvent e) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");loadPage(idToFxml.get(((Node)e.getSource()).getId())); }
    @FXML public void goHome() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("background_music.mp3");
        ((Stage)backButton.getScene().getWindow()).setScene(homeScene); }
    public void setHomeScene(Scene s) { this.homeScene = s; }
}