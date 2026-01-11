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
 * CONTROLLER DEL NEGOZIO (Shop): Gestisce l'interfaccia di acquisto e potenziamento.
 * Implementa un sistema di navigazione a schede dinamiche, la gestione di un carrello
 * virtuale e la logica di progressione dei Power-Up (Sword, Shield, Boots).
 */
public class ShopController implements Initializable {

    // --- ELEMENTI UI (Iniezione FXML) ---
    @FXML private StackPane centerHolder; // Contenitore per il caricamento dinamico delle categorie
    @FXML private Label goldLabel, cartTotalLabel, dialogueLabel;
    @FXML private Button backButton;
    @FXML private ToggleButton hatButton, armorButton, powerUpsButton;

    // Gruppo per garantire la selezione esclusiva di una categoria alla volta
    private final ToggleGroup categoryGroup = new ToggleGroup();
    private Scene homeScene;

    // Elenco degli ID per i quali è prevista una logica di incremento livelli (Power-Ups)
    private final List<String> POWER_UPS = List.of("sword", "shield", "boots");

    // Mapping ID pulsante -> Percorso FXML della categoria corrispondente
    private final Map<String, String> idToFxml = Map.of(
            "hatButton", "/org/example/ProgettoUIDFinal/shop-hats.fxml",
            "armorButton", "/org/example/ProgettoUIDFinal/shop-armors.fxml",
            "powerUpsButton", "/org/example/ProgettoUIDFinal/shop-powerUps.fxml"
    );

    /**
     * INIZIALIZZAZIONE: Configura l'ambiente dello Shop.
     * Attiva il binding dell'oro e carica la prima categoria disponibile (Cappelli).
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Controllo del contesto: esegue la logica solo se l'FXML caricato è quello principale
        if (url != null && url.getPath().contains("Shop.fxml")) {
            MusicManager.getInstance().playMusic("shop.mp3");

            // DATA BINDING: L'oro visualizzato segue reattivamente il valore nel PlayerModel
            goldLabel.textProperty().bind(GameRepository.getInstance().getPlayer().goldProperty().asString());

            hatButton.setToggleGroup(categoryGroup);
            armorButton.setToggleGroup(categoryGroup);
            powerUpsButton.setToggleGroup(categoryGroup);

            hatButton.setSelected(true);
            loadPage(idToFxml.get("hatButton"));
        }
    }

    /**
     * CARICAMENTO DINAMICO (Sub-Views): Inietta un nuovo FXML nel contenitore centrale.
     * Questo approccio permette di mantenere una struttura "Single Page Application" (SPA)
     * dove cambia solo la parte centrale dell'interfaccia.
     */
    private void loadPage(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            loader.setController(this); // Rende questo controller responsabile anche per la sub-view
            Parent root = loader.load();
            centerHolder.getChildren().setAll(root);
            refreshUI(root); // Sincronizza lo stato degli oggetti (già posseduti o disponibili)
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * UI REFRESH: Scansiona i prodotti della categoria e ne aggiorna lo stato visivo.
     * Identifica quali oggetti sono già stati acquistati (SOLD OUT) o quali livelli
     * di potenziamento mostrare.
     */
    private void refreshUI(Parent root) {
        GameRepository repo = GameRepository.getInstance();

        // Ricerca programmata di tutti i ToggleButton (prodotti) tramite selettore CSS
        for (Node node : root.lookupAll(".toggle-button")) {
            if (node instanceof ToggleButton btn) {
                String originalId = btn.getId(); // ID definito nell'FXML (es. "Sword")
                String id = originalId.toLowerCase(); // Normalizzazione per il DB

                // LOGICA PROGRESSIONE: Verifica se l'item è un PowerUp o un oggetto cosmetico
                int count = POWER_UPS.contains(id) ? repo.getPowCounts(id) : (repo.isItemOwned(id) ? 1 : 0);
                boolean isMaxed = POWER_UPS.contains(id) ? count >= 3 : count > 0;

                // Calcolo dell'ID risorsa (es. sword1, sword2...) in base al livello posseduto
                String resId = POWER_UPS.contains(id) ? id + (isMaxed ? "3" : (count + 1)) : id;

                ItemModel item = repo.getItem(resId);
                int priceVal = (item != null) ? item.getPrice() : getFallbackPrice(resId);
                String iconPath = (item != null) ? item.getIconPath() : getFallbackIcon(resId);

                // Aggiornamento etichetta prezzo tramite lookup dinamico dell'ID
                Label priceLabel = (Label) root.lookup("#Price_" + originalId);
                if (priceLabel != null) {
                    priceLabel.setText(isMaxed ? "MAX" : String.valueOf(priceVal));
                }

                ImageView icon = getIcon(btn);
                if (icon != null && iconPath != null) {
                    try {
                        icon.setImage(new Image(getClass().getResourceAsStream(iconPath)));
                    } catch (Exception e) { e.printStackTrace(); }
                }

                // GESTIONE STATO ESAURITO: Se posseduto, disabilita l'interazione
                if (isMaxed) {
                    setSoldOut(btn, icon);
                } else {
                    btn.setDisable(false);
                    btn.setSelected(false);
                    removeSelectionEffect(btn);
                    btn.setOnAction(this::addToCart);
                }
            }
        }
    }

    /**
     * GESTIONE CARRELLO: Calcola il totale parziale in base agli oggetti selezionati.
     * Applica un effetto visivo (darken) per indicare la selezione dell'item.
     */
    @FXML private void addToCart(ActionEvent e) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        ToggleButton btn = (ToggleButton) e.getSource();
        String id = btn.getId();

        // Risoluzione dell'ID per il calcolo del prezzo (livello attuale + 1)
        String resId;
        if (POWER_UPS.contains(id)) {
            int current = GameRepository.getInstance().getPowCounts(id);
            resId = id + (current >= 3 ? 3 : current + 1);
        } else {
            resId = id;
        }

        ItemModel item = GameRepository.getInstance().getItem(resId);
        int price = (item != null) ? item.getPrice() : getFallbackPrice(resId);
        int total = Integer.parseInt(cartTotalLabel.getText().isEmpty() ? "0" : cartTotalLabel.getText());

        // Update del totale tramite addizione/sottrazione
        if (btn.isSelected()) {
            cartTotalLabel.setText(String.valueOf(total + price));
            applySelectionEffect(btn);
        } else {
            cartTotalLabel.setText(String.valueOf(total - price));
            removeSelectionEffect(btn);
        }
    }

    // --- LOGICA DI TRANSAZIONE ---

    /**
     * TRANSAZIONE FINALE: Valida l'acquisto, scala l'oro e aggiorna le statistiche.
     * Implementa un sistema di Feedback Utente (dialogueLabel) e persiste i dati su JSON.
     */
    @FXML
    private void confirmPurchase(ActionEvent e) {
        int cost = Integer.parseInt(cartTotalLabel.getText());
        PlayerModel p = GameRepository.getInstance().getPlayer();
        GameRepository r = GameRepository.getInstance();

        // Business Logic Validation: Verifica disponibilità fondi
        if (p.getGold() < cost) {
            msg("Fondi insufficienti!");
            MusicManager.getInstance().playSoundEffect("no-funds.wav");
            return;
        }
        if (cost == 0) return;

        // Esecuzione transazione
        p.setGold(p.getGold() - cost);
        cartTotalLabel.setText("0");

        // Processing degli oggetti nel carrello
        centerHolder.lookupAll(".toggle-button").forEach(node -> {
            ToggleButton btn = (ToggleButton) node;
            if (btn.isSelected()) {
                String rawId = btn.getId().toLowerCase();

                // Gestione specifica per i PowerUp (Incremento statistiche e livelli)
                if (POWER_UPS.contains(rawId) || rawId.startsWith("sword") || rawId.startsWith("shield") || rawId.startsWith("boots")) {
                    String baseId = rawId.replaceAll("[0-9]", "");
                    if(POWER_UPS.contains(baseId)) {
                        int next = r.getPowCounts(baseId) + 1;
                        r.setPowCounts(baseId, next);
                        String newItemId = baseId + next;
                        applyStats(newItemId, p); // Booster statistiche permanente
                    }
                } else {
                    // Gestione Oggetti Cosmetici
                    r.incrementItemCount(rawId);
                    p.addOwnedItem(rawId);
                }
                btn.setSelected(false);
            }
        });

        // PERSISTENZA: Sincronizza i dati con il file di salvataggio fisico
        r.saveGameToJSON();
        refreshUI((Parent) centerHolder.getChildren().get(0));
        msg("Acquisto completato!");
        MusicManager.getInstance().playSoundEffect("item_bought.mp3");
    }

    /**
     * STATS INJECTION: Applica i bonus permanenti al giocatore dopo l'acquisto.
     * Nota: Alcuni oggetti (spada/scudo) aggiornano anche il layer visivo.
     */
    private void applyStats(String id, PlayerModel p) {
        ItemModel item = GameRepository.getInstance().getItem(id);
        if (id.startsWith("sword")) {
            p.setAtk(p.getAtk() + 2);
            if(item != null) p.setSword(item.getLayerPath(p.isMale()));
            p.setSwordName(item.getName());
            if (item.getIconPath() != null) p.setSwordIcon(item.getIconPath());

        }
        if (id.startsWith("shield")) {
            p.setDef(p.getDef() + 2);
            if(item != null) p.setShield(item.getLayerPath(p.isMale()));
            p.setShieldName(item.getName());
            if (item.getIconPath() != null) p.setShieldIcon(item.getIconPath());

        }
        if (id.startsWith("boots")) p.setVel(p.getVel() + 2);
    }

    // --- FEEDBACK E INTERFACCIA ---

    /**
     * FEEDBACK TEMPORIZZATO: Mostra un testo nel fumetto e lo resetta dopo 2 secondi
     * tramite una PauseTransition (Thread-safe UI update).
     */
    private void msg(String t) {
        dialogueLabel.setText(t);
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(ev -> dialogueLabel.setText("Come posso aiutarti oggi?!"));
        pt.play();
    }

    private void applySelectionEffect(ToggleButton btn) {
        ImageView icon = getIcon(btn);
        if (icon != null) {
            ColorAdjust darken = new ColorAdjust();
            darken.setBrightness(-0.5);
            icon.setEffect(darken);
        }
    }

    private void removeSelectionEffect(ToggleButton btn) {
        ImageView icon = getIcon(btn);
        if (icon != null) icon.setEffect(null);
    }

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
        Label l = new Label("SOLD OUT");
        l.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        sp.getChildren().addAll(rect, l);
        btn.setGraphic(sp);
        btn.setDisable(true);
    }

    // --- NAVIGAZIONE ---

    @FXML private void handleMenu(ActionEvent e) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        loadPage(idToFxml.get(((Node)e.getSource()).getId()));
    }

    @FXML public void goHome() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("background_music.mp3");
        ((Stage)backButton.getScene().getWindow()).setScene(homeScene);
    }

    public void setHomeScene(Scene s) { this.homeScene = s; }

    // Fallback logic per prezzi ed icone (in caso di assenza dati nel Repository)
    private int getFallbackPrice(String resId) {
        if (resId.endsWith("1")) return 250;
        if (resId.endsWith("2")) return 500;
        if (resId.endsWith("3")) return 1000;
        return 500;
    }

    private String getFallbackIcon(String resId) {
        // Logica di recupero icone basata su convenzioni di naming
        return null;
    }
}