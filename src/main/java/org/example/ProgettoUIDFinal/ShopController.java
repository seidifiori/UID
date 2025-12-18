package org.example.ProgettoUIDFinal;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton; // <--- Importante!
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.ItemModel;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopController implements Initializable {

    @FXML private Label soldi;
    @FXML private Label carrello;

    // Label Prezzi
    @FXML private Label Hat1, Hat2, Hat3;
    @FXML private Label Dress1, Dress2, Dress3;
    @FXML private Label Power1, Power2, Power3;

    // Tasto Indietro (Resta un Button normale)
    @FXML private Button BackButton;

    // --- CORREZIONE: Usa ToggleButton invece di Button ---
    @FXML private ToggleButton Cap1, Cap2, Cap3;
    @FXML private ToggleButton Dres1, Dres2, Dres3;
    @FXML private ToggleButton sword, shield;

    @FXML private Label DialogueLabel;

    // Immagini statistiche (Power Ups)

    private Scene homeScene;

    // Metodo helper per iterare sui bottoni
    private List<ToggleButton> tuttiIBottoniDelNegozio() {
        // Attenzione: Assicurati che questi ID esistano e non siano nulli
        return List.of(Cap1, Cap2, Cap3, Dres1, Dres2, Dres3, sword, shield);
    }

    @FXML
    private ImageView swordIcon;

    @FXML
    private ImageView shieldIcon;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MusicManager.getInstance().playMusic("shop.mp3");
        PlayerModel player = GameRepository.getInstance().getPlayer();
        GameRepository repo = GameRepository.getInstance();

        if (soldi != null) {
            soldi.textProperty().bind(player.goldProperty().asString());
        }

        for (ToggleButton b : tuttiIBottoniDelNegozio()) {
            if (b == null) continue;

            String buttonId = b.getId();
            String baseType = buttonId.toLowerCase();

            String resourceId;
            // Definiamo due limiti diversi
            int limiteAcquisto = 3; // Fino a che livello puoi comprare (es. sword3)
            int livelloFinaleVisivo = 3; // L'icona che vuoi mostrare alla fine (es. sword4)

            boolean isProgressiveMaxed = false;
            boolean isNormalSoldOut = false;

            // --- LOGICA ID DINAMICO (Sword/Shield) ---
            if (baseType.equals("sword") || baseType.equals("shield")) {
                int currentLevel = repo.getPowCounts(baseType);

                if (currentLevel >= limiteAcquisto) {
                    // Se hai già comprato il livello 3, mostriamo il livello 4
                    isProgressiveMaxed = true;
                    resourceId = baseType + livelloFinaleVisivo;
                } else {
                    // Altrimenti mostriamo il prossimo livello acquistabile
                    resourceId = baseType + (currentLevel + 1);
                }
            } else {
                // Logica standard per cappelli/vestiti
                resourceId = baseType;
                isNormalSoldOut = repo.isItemOwned(resourceId);
            }

            // --- APPLICAZIONE DATI DA ITEMMODEL ---
            ItemModel item = repo.getItem(resourceId);

            if (item != null) {
                // 1. Gestione Prezzo
                Label priceLabel = getPriceLabel(buttonId);
                if (priceLabel != null) {
                    // Se è maxato (progressivo) o posseduto (normale), scrivi MAX
                    if (isProgressiveMaxed || isNormalSoldOut) {
                        priceLabel.setText("MAX");
                    } else {
                        priceLabel.setText(String.valueOf(item.getPrice()));
                    }
                }

                // 2. Gestione Icona (Questa parte funziona già bene)
                if (baseType.equals("sword") || baseType.equals("shield")) {
                    ImageView iconView = getIconView(buttonId);
                    if (iconView != null && item.getIconPath() != null) {
                        Image icona = new Image(getClass().getResourceAsStream(item.getIconPath()));
                        iconView.setImage(icona);
                    }
                }

                // 3. Stato del Bottone (LA PARTE CRUCIALE)
                if (isProgressiveMaxed) {
                    // CASO SPADA/SCUDO AL MASSIMO: Disabilita solo il bottone, niente overlay grafico
                    b.setDisable(true);
                    b.setOpacity(1.0);

                    if (b.getGraphic() instanceof ImageView iv) {
                        iv.setEffect(null);
                    }
                } else if (isNormalSoldOut) {
                    // CASO CAPPELLI GIA' COMPRATI: Usa l'overlay grafico "SOLD OUT"
                    soldOut(b);
                }
            }
        }
    }

    @FXML
    private void AggiungiAlCarrello(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        GameRepository repo = GameRepository.getInstance();

        ToggleButton b = (ToggleButton) event.getSource();
        String baseType = b.getId().toLowerCase(); // "sword", "shield", "cap1", ecc.

        // Determiniamo il resourceId corretto (es: sword -> sword1)
        String resourceId;
        if (baseType.equals("sword") || baseType.equals("shield")) {
            int nextLevel = repo.getPowCounts(baseType) + 1;
            resourceId = baseType + nextLevel;
        } else {
            resourceId = baseType;
        }

        ItemModel item = repo.getItem(resourceId);
        if (item == null) {
            System.err.println("Errore: Item non trovato per " + resourceId);
            return;
        }

        int prezzo = item.getPrice();
        int totaleAttuale = 0;
        try {
            totaleAttuale = Integer.parseInt(carrello.getText());
        } catch (NumberFormatException e) { totaleAttuale = 0; }

        if (b.isSelected()) {
            carrello.setText(String.valueOf(totaleAttuale + prezzo));
            applicaEffettoSelezione(b);
        } else {
            carrello.setText(String.valueOf(totaleAttuale - prezzo));
            rimuoviEffettoSelezione(b);
        }
    }

    @FXML
    private void ConfermaAcquisto(ActionEvent event) {
        GameRepository repo = GameRepository.getInstance();
        PlayerModel player = repo.getPlayer();

        int spesa = 0;
        try {
            spesa = Integer.parseInt(carrello.getText());
        } catch (NumberFormatException e) { spesa = 0; }

        if (spesa == 0) {
            DialogueLabel.setText("Il carrello è vuoto.");
            resetDialogueAfterDelay();
            return;
        }

        if (player.getGold() < spesa) {
            MusicManager.getInstance().playSoundEffect("no-funds.wav");
            DialogueLabel.setText("Soldi insufficienti!");
            resetDialogueAfterDelay();
            return;
        }

        // --- ACQUISTO RIUSCITO ---
        MusicManager.getInstance().playSoundEffect("item_bought.mp3");
        player.setGold(player.getGold() - spesa);
        carrello.setText("0");

        for (ToggleButton b : tuttiIBottoniDelNegozio()) {
            if (b.isSelected()) {
                String baseType = b.getId().toLowerCase();

                if (baseType.equals("sword") || baseType.equals("shield")) {
                    int currentLevel = repo.getPowCounts(baseType);

                    int levelBought = currentLevel + 1;
                    String resourceIdBought = baseType + levelBought;

                    applicaPotenziamento(resourceIdBought, player);
                    repo.setPowCounts(baseType, levelBought);

                    int limiteAcquisto = 3;
                    int livelloFinaleVisivo = 3;

                    if (levelBought >= limiteAcquisto) {
                        aggiornaVisualSenzaRicaricare(b, baseType, livelloFinaleVisivo);

                        Label priceLabel = getPriceLabel(b.getId());
                        if (priceLabel != null) priceLabel.setText("MAX");

                        b.setSelected(false);
                        rimuoviEffettoSelezione(b);

                        // DISABILITA ma mantiene l'opacità piena
                        b.setDisable(true);
                        b.setOpacity(1.0);

                    } else {
                        // Normal progressione (es. comprato liv 1, mostra liv 2)
                        int nextLevel = levelBought + 1;
                        aggiornaVisualSenzaRicaricare(b, baseType, nextLevel);
                        b.setSelected(false);
                        rimuoviEffettoSelezione(b);
                    }
                }
                // --- CASO 2: OGGETTI NORMALI ---
                else {
                    repo.incrementItemCount(baseType);
                    player.addOwnedItem(baseType);
                    soldOut(b); // Per i cappelli usiamo ancora l'overlay grafico
                }
            }
        }

        repo.saveGameToJSON();
        DialogueLabel.setText("Grazie per l'acquisto!");
        resetDialogueAfterDelay();
    }

    private void aggiornaVisualSenzaRicaricare(ToggleButton b, String baseType, int nextLevel) {
        GameRepository repo = GameRepository.getInstance();
        String nextResourceId = baseType + nextLevel;
        ItemModel nextItem = repo.getItem(nextResourceId);

        if (nextItem != null) {
            // 1. Aggiorna il Prezzo
            Label priceLabel = getPriceLabel(b.getId());
            if (priceLabel != null) {
                priceLabel.setText(String.valueOf(nextItem.getPrice()));
            }

            // 2. Aggiorna l'Icona (es: sword1 -> sword2)
            ImageView iconView = getIconView(b.getId());
            if (iconView != null && nextItem.getIconPath() != null) {
                Image nuovaImmagine = new Image(getClass().getResourceAsStream(nextItem.getIconPath()));
                iconView.setImage(nuovaImmagine);
            }
        }
    }

    // --- Metodi Helper ---

    private void applicaPotenziamento(String id, PlayerModel player) {
        GameRepository repo = GameRepository.getInstance();
        ItemModel item = repo.getItem(id);

        if (item != null) {
            if ("sword".equalsIgnoreCase(item.getType())) {
                int attualeAtk = player.getAtk();
                player.setAtk(attualeAtk + 3);

                player.setSword(item.getLayerPath());     // Imposta l'immagine sul personaggio
                player.setSwordName(item.getName());      // Aggiorna il nome (es: "Spada Leggendaria")
                if (item.getIconPath() != null) player.setSwordIcon(item.getIconPath());

            } else if ("shield".equalsIgnoreCase(item.getType())) {
                int attualeDef = player.getDef();
                player.setDef(attualeDef + 3);

                player.setShield(item.getLayerPath());    // Imposta l'immagine sullo scudo
                player.setShieldName(item.getName());
                if (item.getIconPath() != null) player.setShieldIcon(item.getIconPath());
            }
        }
    }

    private Label getPriceLabel(String buttonId) {
        return switch (buttonId) {
            case "Cap1" -> Hat1; case "Cap2" -> Hat2; case "Cap3" -> Hat3;
            case "Dres1" -> Dress1; case "Dres2" -> Dress2; case "Dres3" -> Dress3;
            // Questi devono corrispondere agli ID dei ToggleButton e alle Label nel FXML
            case "sword" -> Power1;
            case "shield" -> Power2;
            default -> null;
        };
    }

    @FXML
    private void applicaEffettoSelezione(ToggleButton b) {
        if (b.getGraphic() instanceof ImageView iv) {
            ColorAdjust darken = new ColorAdjust();
            darken.setBrightness(-0.5); // Scurisce l'immagine per mostrare selezione
            iv.setEffect(darken);
        }
    }

    @FXML
    private void rimuoviEffettoSelezione(ToggleButton b) {
        if (b.getGraphic() instanceof ImageView iv) {
            iv.setEffect(null);
        }
    }

    private void soldOut(ToggleButton b) {
        if (b.getGraphic() instanceof ImageView iv) {
            double h = iv.getFitHeight();
            // Creiamo una copia dell'immagine per non alterare l'originale
            ImageView original = new ImageView(iv.getImage());
            original.setFitWidth(h);
            original.setFitHeight(h);
            original.setPreserveRatio(true);

            // Overlay scuro
            Rectangle overlay = new Rectangle(h, h);
            overlay.setFill(Color.rgb(0, 0, 0, 0.8));

            // Scritta
            Label soldOutLabel = new Label("SOLD OUT");
            soldOutLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 18px;");

            StackPane stack = new StackPane(original, overlay, soldOutLabel);
            stack.setAlignment(Pos.CENTER);
            stack.setPrefSize(h, h);

            b.setPadding(Insets.EMPTY);
            b.setGraphic(stack);

            // Disabilita interazione
            b.setDisable(true);
            b.setSelected(false); // Importante per non contarlo nel carrello
        }
    }

    private void resetDialogueAfterDelay() {
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> DialogueLabel.setText("Cosa posso fare per te?"));
        pause.play();
    }

    @FXML
    public void setHomeScene(Scene scene) { this.homeScene = scene; }

    @FXML
    public void Home() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("background_music.mp3");

        if (homeScene != null) {
            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        }
    }

    private ImageView getIconView(String buttonId) {
        return switch (buttonId) {
            case "sword" -> swordIcon;
            case "shield" -> shieldIcon;
            default -> null;
        };
    }
}