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
import javafx.scene.control.ToggleButton;
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

    @FXML private Button BackButton;

    @FXML private ToggleButton Cap1, Cap2, Cap3;
    @FXML private ToggleButton Dres1, Dres2, Dres3;
    @FXML private ToggleButton sword, shield, boots;

    @FXML private Label DialogueLabel;

    @FXML private ImageView swordIcon;
    @FXML private ImageView shieldIcon;
    @FXML private ImageView bootsIcon;

    private Scene homeScene;

    private List<ToggleButton> tuttiIBottoniDelNegozio() {
        return List.of(Cap1, Cap2, Cap3, Dres1, Dres2, Dres3, sword, shield, boots);
    }

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
            int limiteAcquisto = 3;
            int livelloFinaleVisivo = 3;

            boolean isProgressiveMaxed = false;
            boolean isNormalSoldOut = false;

            if (baseType.equals("sword") || baseType.equals("shield") || baseType.equals("boots")) {
                int currentLevel = repo.getPowCounts(baseType);

                if (currentLevel >= limiteAcquisto) {
                    isProgressiveMaxed = true;
                    resourceId = baseType + livelloFinaleVisivo;
                } else {
                    resourceId = baseType + (currentLevel + 1);
                }
            } else {
                resourceId = baseType;
                isNormalSoldOut = repo.isItemOwned(resourceId);
            }

            ItemModel item = repo.getItem(resourceId);

            if (item != null) {
                Label priceLabel = getPriceLabel(buttonId);
                if (priceLabel != null) {
                    if (isProgressiveMaxed || isNormalSoldOut) {
                        priceLabel.setText("MAX");
                    } else {
                        priceLabel.setText(String.valueOf(item.getPrice()));
                    }
                }

                // Gestione Icona
                if (baseType.equals("sword") || baseType.equals("shield") || baseType.equals("boots")) {
                    ImageView iconView = getIconView(buttonId);
                    if (iconView != null && item.getIconPath() != null) {
                        Image icona = new Image(getClass().getResourceAsStream(item.getIconPath()));
                        iconView.setImage(icona);
                    }
                }

                if (isProgressiveMaxed) {
                    b.setDisable(true);
                    b.setOpacity(1.0);
                    if (b.getGraphic() instanceof ImageView iv) {
                        iv.setEffect(null);
                    }
                } else if (isNormalSoldOut) {
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
        String baseType = b.getId().toLowerCase();

        String resourceId;
        if (baseType.equals("sword") || baseType.equals("shield") || baseType.equals("boots")) {
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

                if (baseType.equals("sword") || baseType.equals("shield") || baseType.equals("boots")) {
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
                        b.setDisable(true);
                        b.setOpacity(1.0);

                    } else {
                        int nextLevel = levelBought + 1;
                        aggiornaVisualSenzaRicaricare(b, baseType, nextLevel);
                        b.setSelected(false);
                        rimuoviEffettoSelezione(b);
                    }
                }
                else {
                    repo.incrementItemCount(baseType);
                    player.addOwnedItem(baseType);
                    soldOut(b);
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
            Label priceLabel = getPriceLabel(b.getId());
            if (priceLabel != null) {
                priceLabel.setText(String.valueOf(nextItem.getPrice()));
            }

            ImageView iconView = getIconView(b.getId());
            if (iconView != null && nextItem.getIconPath() != null) {
                Image nuovaImmagine = new Image(getClass().getResourceAsStream(nextItem.getIconPath()));
                iconView.setImage(nuovaImmagine);
            }
        }
    }

    // --- MODIFICA QUI ---
    private void applicaPotenziamento(String id, PlayerModel player) {
        GameRepository repo = GameRepository.getInstance();
        ItemModel item = repo.getItem(id);

        if (item != null) {
            // Otteniamo il sesso attuale per assegnare l'immagine corretta
            boolean isMale = player.isMale();

            if ("sword".equalsIgnoreCase(item.getType())) {
                int attualeAtk = player.getAtk();
                player.setAtk(attualeAtk + 3);

                // CORREZIONE: Passiamo isMale a getLayerPath
                player.setSword(item.getLayerPath(isMale));
                player.setSwordName(item.getName());
                if (item.getIconPath() != null) player.setSwordIcon(item.getIconPath());

            } else if ("shield".equalsIgnoreCase(item.getType())) {
                int attualeDef = player.getDef();
                player.setDef(attualeDef + 3);

                // CORREZIONE: Passiamo isMale a getLayerPath
                player.setShield(item.getLayerPath(isMale));
                player.setShieldName(item.getName());
                if (item.getIconPath() != null) player.setShieldIcon(item.getIconPath());
            } else if ("boots".equalsIgnoreCase(item.getType())) {
                int attualeVel = player.getVel();
                player.setVel(attualeVel + 3);
            }
        }
    }
    // -------------------

    private Label getPriceLabel(String buttonId) {
        return switch (buttonId) {
            case "Cap1" -> Hat1; case "Cap2" -> Hat2; case "Cap3" -> Hat3;
            case "Dres1" -> Dress1; case "Dres2" -> Dress2; case "Dres3" -> Dress3;
            case "sword" -> Power1;
            case "shield" -> Power2;
            case "boots" -> Power3;
            default -> null;
        };
    }

    @FXML
    private void applicaEffettoSelezione(ToggleButton b) {
        if (b.getGraphic() instanceof ImageView iv) {
            ColorAdjust darken = new ColorAdjust();
            darken.setBrightness(-0.5);
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
            ImageView original = new ImageView(iv.getImage());
            original.setFitWidth(h);
            original.setFitHeight(h);
            original.setPreserveRatio(true);

            Rectangle overlay = new Rectangle(h, h);
            overlay.setFill(Color.rgb(0, 0, 0, 0.8));

            Label soldOutLabel = new Label("SOLD OUT");
            soldOutLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 18px;");

            StackPane stack = new StackPane(original, overlay, soldOutLabel);
            stack.setAlignment(Pos.CENTER);
            stack.setPrefSize(h, h);

            b.setPadding(Insets.EMPTY);
            b.setGraphic(stack);

            b.setDisable(true);
            b.setSelected(false);
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
            case "boots" -> bootsIcon;
            default -> null;
        };
    }
}