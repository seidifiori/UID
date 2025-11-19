package org.example.ProgettoUIDFinal;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

// === NUOVI IMPORT DEL MODEL ===
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.ItemModel;
import org.example.ProgettoUIDFinal.model.PlayerModel;
// ==============================

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Shop implements Initializable {

    @FXML private Label soldi;
    @FXML private Label carrello;

    // Label Prezzi (Potresti anche rimuoverle e leggere i prezzi dall'Item del model, ma per ora le teniamo per semplicità UI)
    @FXML private Label Hat1, Hat2, Hat3;
    @FXML private Label Dress1, Dress2, Dress3;
    @FXML private Label Power1, Power2, Power3;

    @FXML private Button BackButton;

    // Bottoni Oggetti
    @FXML private Button Cap1, Cap2, Cap3;
    @FXML private Button Dres1, Dres2, Dres3;
    @FXML private Button Pow1, Pow2, Pow3;

    @FXML private Label labelHomeSoldi; // Non serve più realmente col binding, ma lo lasciamo per compatibilità
    @FXML private Label DialogueLabel;

    private Scene homeScene;

    private List<Button> tuttiIBottoniDelNegozio() {
        return List.of(Cap1, Cap2, Cap3, Dres1, Dres2, Dres3, Pow1, Pow2, Pow3);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PlayerModel player = GameRepository.getInstance().getPlayer();

        // 1. BINDING DEI SOLDI
        // Collega la label del negozio ai soldi veri del player.
        // Appena il player spende, questa scritta cambia da sola.
        soldi.textProperty().bind(player.goldProperty().asString());

        // 2. CONTROLLO OGGETTI POSSEDUTI
        for (Button b : tuttiIBottoniDelNegozio()) {
            if (b == null) continue;
            String itemId = b.getId(); // Es. "cap1" (assicurati che l'ID nel FXML coincida con l'ID nel properties!)

            // Chiediamo al model se abbiamo l'oggetto
            if (player.hasItem(itemId)) {
                soldOut(b);
            } else {
                // Opzionale: Potremmo aggiornare il prezzo nella UI leggendolo dal Model
                ItemModel item = GameRepository.getInstance().getItem(itemId);
                if (item != null) {
                    Label priceLabel = getPriceLabel(itemId);
                    if (priceLabel != null) priceLabel.setText(String.valueOf(item.getPrice()));
                }
            }
        }
    }

    @FXML
    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }

    // Questo metodo non serve più davvero grazie al binding, ma lo lasciamo vuoto per non rompere vecchie chiamate
    @FXML
    public void setSoldiLabel(Label homeLabel) {
        // Non fare nulla, usiamo il model ora!
    }

    @FXML
    private void AggiungiAlCarrello(ActionEvent event) {
        Button b = (Button) event.getSource();
        String id = b.getId();

        // Recupera il prezzo dal Model invece che dalla Label (più sicuro)
        // Se preferisci usare ancora le Label, usa il vecchio metodo switch
        ItemModel item = GameRepository.getInstance().getItem(id);
        int prezzo = (item != null) ? item.getPrice() : 0;

        // Se l'item non esiste nel model, proviamo a leggere dalla label come fallback
        if (prezzo == 0) {
            Label l = getPriceLabel(id);
            if (l != null) prezzo = Integer.parseInt(l.getText());
        }

        int totaleAttuale = Integer.parseInt(carrello.getText());
        boolean isSelected = b.getUserData() != null && (boolean) b.getUserData();

        if (isSelected) {
            // Rimuovi
            carrello.setText(String.valueOf(totaleAttuale - prezzo));
            b.setUserData(false);
            rimuoviEffettoSelezione(b);
        } else {
            // Aggiungi
            carrello.setText(String.valueOf(totaleAttuale + prezzo));
            b.setUserData(true);
            applicaEffettoSelezione(b);
        }
    }

    @FXML
    private void ConfermaAcquisto(ActionEvent event) {
        PlayerModel player = GameRepository.getInstance().getPlayer();
        int spesa = Integer.parseInt(carrello.getText());

        // 1. Controllo soldi tramite Model
        if (player.getGold() < spesa) {
            DialogueLabel.setText("Soldi insufficienti!");
            resetDialogueAfterDelay();
            return;
        }

        // 2. Effettua l'acquisto
        // Scaliamo i soldi (la UI si aggiorna da sola grazie al binding!)
        player.setGold(player.getGold() - spesa);

        // Resetta carrello
        carrello.setText("0");

        // 3. Aggiungi oggetti all'inventario e aggiorna grafica
        for (Button b : tuttiIBottoniDelNegozio()) {
            Boolean selected = (Boolean) b.getUserData();
            if (Boolean.TRUE.equals(selected)) {
                String itemId = b.getId();

                // Aggiungi al Model
                player.addItem(itemId);

                // Aggiorna UI
                soldOut(b);
            }
        }

        DialogueLabel.setText("Grazie per l'acquisto!");
        resetDialogueAfterDelay();
    }

    // --- Metodi Helper Grafici (Invariati o leggermente puliti) ---

    private Label getPriceLabel(String id) {
        return switch (id) {
            case "Cap1" -> Hat1;
            case "Cap2" -> Hat2;
            case "Cap3" -> Hat3;
            case "Dres1" -> Dress1;
            case "Dres2" -> Dress2;
            case "Dres3" -> Dress3;
            case "Pow1" -> Power1;
            case "Pow2" -> Power2;
            case "Pow3" -> Power3;
            default -> null;
        };
    }

    @FXML
    private void applicaEffettoSelezione(Button b) {
        if (b.getGraphic() instanceof ImageView iv) {
            ColorAdjust darken = new ColorAdjust();
            darken.setBrightness(-0.5);
            iv.setEffect(darken);
        }
    }

    @FXML
    private void rimuoviEffettoSelezione(Button b) {
        if (b.getGraphic() instanceof ImageView iv) {
            iv.setEffect(null);
        }
    }

    private void soldOut(Button b) {
        if (b.getGraphic() instanceof ImageView iv) {
            double h = iv.getFitHeight();

            ImageView original = new ImageView(iv.getImage());
            original.setFitWidth(h);
            original.setFitHeight(h);
            original.setPreserveRatio(true);

            Rectangle overlay = new Rectangle(h, h);
            overlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.8));

            Label soldOutLabel = new Label("SOLD OUT");
            soldOutLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 18px;");

            StackPane stack = new StackPane(original, overlay, soldOutLabel);
            stack.setAlignment(javafx.geometry.Pos.CENTER);
            stack.setPrefSize(h, h);
            stack.setMaxSize(h, h);
            stack.setMinSize(h, h);

            b.setPadding(javafx.geometry.Insets.EMPTY);
            b.setAlignment(javafx.geometry.Pos.CENTER);
            b.setGraphic(stack);
            b.setDisable(true);
            b.setUserData(false); // Importante: deselezionarlo logicamente
        }
    }

    private void resetDialogueAfterDelay() {
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> DialogueLabel.setText("Cosa Posso fare per te?"));
        pause.play();
    }

    @FXML
    public void Home() {
        if (homeScene != null) {
            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        } else {
            System.err.println("⚠ Nessuna scena Home disponibile!");
        }
    }
}