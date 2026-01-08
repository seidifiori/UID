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

/**
 * Controller che gestisce la logica del Negozio (Shop).
 * Gestisce la visualizzazione degli oggetti, il carrello, l'acquisto
 * e l'aggiornamento dell'inventario del giocatore.
 */
public class ShopController implements Initializable {

    @FXML private StackPane centerHolder;
    @FXML private Label goldLabel, cartTotalLabel, dialogueLabel;
    @FXML private Button backButton;
    @FXML private ToggleButton hatButton, armorButton, powerUpsButton;

    private final ToggleGroup categoryGroup = new ToggleGroup();
    private Scene homeScene;

    // Lista dei potenziamenti che hanno logiche speciali (livelli multipli)
    private final List<String> POWER_UPS = List.of("sword", "shield", "boots");

    // Mappa per la navigazione dinamica tra le categorie dello shop
    private final Map<String, String> idToFxml = Map.of(
            "hatButton", "/org/example/ProgettoUIDFinal/shop-hats.fxml",
            "armorButton", "/org/example/ProgettoUIDFinal/shop-armors.fxml",
            "powerUpsButton", "/org/example/ProgettoUIDFinal/shop-powerUps.fxml"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Verifica di essere nella pagina principale dello shop per avviare la musica e i binding
        if (url != null && url.getPath().contains("Shop.fxml")) {
            MusicManager.getInstance().playMusic("shop.mp3");

            // Collega l'etichetta dell'oro alla proprietà osservabile del giocatore (aggiornamento automatico)
            goldLabel.textProperty().bind(GameRepository.getInstance().getPlayer().goldProperty().asString());

            // Configura i pulsanti delle categorie
            hatButton.setToggleGroup(categoryGroup);
            armorButton.setToggleGroup(categoryGroup);
            powerUpsButton.setToggleGroup(categoryGroup);

            // Carica la categoria di default (Cappelli)
            hatButton.setSelected(true);
            loadPage(idToFxml.get("hatButton"));
        }
    }

    /**
     * Carica dinamicamente il contenuto FXML di una categoria nel pannello centrale.
     */
    private void loadPage(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            loader.setController(this); // Usa questo stesso controller per i sottocontenuti
            Parent root = loader.load();
            centerHolder.getChildren().setAll(root);
            refreshUI(root); // Aggiorna lo stato degli oggetti (prezzi, posseduti, sold out)
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Aggiorna l'interfaccia grafica dei prodotti caricati.
     * Gestisce i prezzi, le icone e lo stato "SOLD OUT" se l'oggetto è già posseduto.
     */
    private void refreshUI(Parent root) {
        GameRepository repo = GameRepository.getInstance();

        // Itera su tutti i ToggleButton (i prodotti) nella pagina corrente
        for (Node node : root.lookupAll(".toggle-button")) {
            if (node instanceof ToggleButton btn) {

                // Recupera l'ID grafico (es. "Cap1") per cercare le Label nell'FXML
                String originalId = btn.getId();

                // Normalizza l'ID in minuscolo (es. "cap1") per interrogare il Database/Properties
                // Questo passaggio risolve la differenza di case-sensitivity tra FXML e Backend
                String id = originalId.toLowerCase();

                // Verifica se l'oggetto è posseduto o il livello del potenziamento
                int count = POWER_UPS.contains(id) ? repo.getPowCounts(id) : (repo.isItemOwned(id) ? 1 : 0);
                boolean isMaxed = POWER_UPS.contains(id) ? count >= 3 : count > 0;

                // Calcola l'ID della risorsa specifica da mostrare (es. spada livello 2)
                String resId = POWER_UPS.contains(id) ? id + (isMaxed ? "3" : (count + 1)) : id;

                ItemModel item = repo.getItem(resId);

                // Dati di fallback nel caso il database non restituisca l'item
                int priceVal = (item != null) ? item.getPrice() : getFallbackPrice(resId);
                String iconPath = (item != null) ? item.getIconPath() : getFallbackIcon(resId);

                // Aggiorna la Label del prezzo (usa l'ID originale dell'FXML)
                Label priceLabel = (Label) root.lookup("#Price_" + originalId);
                if (priceLabel != null) {
                    priceLabel.setText(isMaxed ? "MAX" : String.valueOf(priceVal));
                }

                // Aggiorna l'icona del pulsante
                ImageView icon = getIcon(btn);
                if (icon != null && iconPath != null) {
                    try {
                        icon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
                    } catch (Exception e) {
                        System.err.println("Immagine non trovata: " + iconPath);
                    }
                }

                // Se l'oggetto è al massimo livello o già posseduto, lo segna come SOLD OUT
                if (isMaxed) {
                    setSoldOut(btn, icon);
                } else {
                    // Altrimenti abilita il pulsante per l'acquisto
                    if (btn.getGraphic() instanceof StackPane) btn.setGraphic(icon);
                    btn.setDisable(false);
                    btn.setSelected(false);
                    removeSelectionEffect(btn);
                    btn.setOnAction(this::addToCart);
                }
            }
        }
    }

    /**
     * Gestisce la selezione di un oggetto.
     * Aggiorna il totale del carrello e applica effetti visivi.
     */
    @FXML private void addToCart(ActionEvent e) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        ToggleButton btn = (ToggleButton) e.getSource();
        String id = btn.getId(); // Nota: qui l'ID è ancora quello grezzo dell'FXML

        // Determina l'ID corretto per il prezzo (gestione livelli successivi per i PowerUp)
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

        // Aggiunge o rimuove il prezzo dal totale e gestisce l'effetto scuro di selezione
        if (btn.isSelected()) {
            cartTotalLabel.setText(String.valueOf(total + price));
            applySelectionEffect(btn);
        } else {
            cartTotalLabel.setText(String.valueOf(total - price));
            removeSelectionEffect(btn);
        }
    }

    // --- EFFETTI VISIVI ---

    private void applySelectionEffect(ToggleButton btn) {
        ImageView icon = getIcon(btn);
        if (icon != null) {
            ColorAdjust darken = new ColorAdjust();
            darken.setBrightness(-0.5); // Scurisce l'immagine del 50%
            icon.setEffect(darken);
        }
    }

    private void removeSelectionEffect(ToggleButton btn) {
        ImageView icon = getIcon(btn);
        if (icon != null) {
            icon.setEffect(null); // Rimuove qualsiasi effetto
        }
    }

    // --- DATI DI FALLBACK (Sicurezza se il DB fallisce) ---

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

    // --- METODI DI UTILITÀ ---

    // Estrae l'ImageView da un bottone (anche se annidato in uno StackPane)
    private ImageView getIcon(ToggleButton btn) {
        if (btn.getGraphic() instanceof ImageView iv) return iv;
        if (btn.getGraphic() instanceof StackPane sp) {
            for (Node n : sp.getChildren()) if (n instanceof ImageView iv) return iv;
        }
        return null;
    }

    // Modifica graficamente il bottone per indicare che è esaurito
    private void setSoldOut(ToggleButton btn, ImageView icon) {
        StackPane sp = new StackPane(new ImageView(icon.getImage()));
        Rectangle rect = new Rectangle(icon.getFitWidth(), icon.getFitHeight(), Color.rgb(0,0,0,0.7));
        Label l = new Label("SOLD OUT");
        l.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        sp.getChildren().addAll(rect, l);
        btn.setGraphic(sp);
        btn.setDisable(true);
    }

    /**
     * Finalizza l'acquisto degli oggetti nel carrello.
     * Controlla i fondi, scala i soldi, aggiorna le statistiche e salva gli oggetti nell'inventario.
     */
    @FXML
    private void confirmPurchase(ActionEvent e) {
        int cost = Integer.parseInt(cartTotalLabel.getText());
        PlayerModel p = GameRepository.getInstance().getPlayer();
        GameRepository r = GameRepository.getInstance();

        // Controllo fondi
        if (p.getGold() < cost) {
            msg("Fondi insufficienti!");
            MusicManager.getInstance().playSoundEffect("no-funds.wav");
            return;
        }
        if (cost == 0) {
            msg("Il carrello è vuoto!");
            MusicManager.getInstance().playSoundEffect("no-funds.wav");
            return;
        }

        // 1. Transazione economica
        p.setGold(p.getGold() - cost);
        cartTotalLabel.setText("0");
        MusicManager.getInstance().playSoundEffect("item_bought.mp3");

        // 2. Elaborazione oggetti selezionati
        centerHolder.lookupAll(".toggle-button").forEach(node -> {
            ToggleButton btn = (ToggleButton) node;
            if (btn.isSelected()) {

                // Normalizza l'ID in minuscolo per compatibilità con il sistema di salvataggio
                String rawId = btn.getId().toLowerCase();

                // Caso A: Potenziamenti (Spade, Scudi, Stivali)
                // Hanno logica di livello e statistiche
                if (POWER_UPS.contains(rawId) || rawId.startsWith("sword") || rawId.startsWith("shield") || rawId.startsWith("boots")) {
                    String baseId = rawId.replaceAll("[0-9]", ""); // Rimuove numeri per identificare la categoria

                    if(POWER_UPS.contains(baseId)) {
                        int next = r.getPowCounts(baseId) + 1;
                        r.setPowCounts(baseId, next);

                        String newItemId = baseId + next; // Es: "sword1"
                        applyStats(newItemId, p); // Applica boost alle statistiche

                        // Aggiunge l'oggetto all'inventario visibile
                        p.addOwnedItem(newItemId);
                    }
                } else {
                    // Caso B: Oggetti Cosmetici standard (Cappelli, Armature)
                    r.incrementItemCount(rawId);
                    p.addOwnedItem(rawId);
                }

                // Resetta lo stato del bottone dopo l'acquisto
                btn.setSelected(false);
                removeSelectionEffect(btn);
            }
        });

        // Salvataggio su file JSON e aggiornamento immediato della UI
        r.saveGameToJSON();
        refreshUI((Parent) centerHolder.getChildren().get(0));

        msg("Acquisto completato!");
        MusicManager.getInstance().playSoundEffect("item_bought.mp3");
    }

    // Applica le statistiche al giocatore in base all'oggetto comprato
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

    // Mostra messaggi temporanei nel fumetto del mercante
    private void msg(String t) {
        dialogueLabel.setText(t);
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(ev -> dialogueLabel.setText("Come posso aiutarti oggi?!"));
        pt.play();
    }

    // Navigazione
    @FXML private void handleMenu(ActionEvent e) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        loadPage(idToFxml.get(((Node)e.getSource()).getId()));
    }
//torna nella pagina principale
    @FXML public void goHome() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("background_music.mp3");
        ((Stage)backButton.getScene().getWindow()).setScene(homeScene);
    }

    public void setHomeScene(Scene s) { this.homeScene = s; }
}