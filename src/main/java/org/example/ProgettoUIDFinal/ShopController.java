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
    @FXML private ToggleButton Pow1, Pow2, Pow3;

    @FXML private Label DialogueLabel;

    // Immagini statistiche (Power Ups)
    @FXML private ImageView Atk1, Atk2, Atk3;
    @FXML private ImageView Def1, Def2, Def3;
    @FXML private ImageView Spd1, Spd2, Spd3;

    private Scene homeScene;

    // Metodo helper per iterare sui bottoni
    private List<ToggleButton> tuttiIBottoniDelNegozio() {
        // Attenzione: Assicurati che questi ID esistano e non siano nulli
        return List.of(Cap1, Cap2, Cap3, Dres1, Dres2, Dres3, Pow1, Pow2, Pow3);
    }

    private List<ImageView> tuttoAtk() { return List.of(Atk1, Atk2, Atk3); }
    private List<ImageView> tuttoDef() { return List.of(Def1, Def2, Def3); }
    private List<ImageView> tuttoSpd() { return List.of(Spd1, Spd2, Spd3); }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MusicManager.getInstance().playMusic("shop.mp3");
        PlayerModel player = GameRepository.getInstance().getPlayer();
        GameRepository repo = GameRepository.getInstance();

        if (soldi != null) {
            soldi.textProperty().bind(player.goldProperty().asString());
        }

        // Inizializzazione Bottoni
        for (ToggleButton b : tuttiIBottoniDelNegozio()) {
            if (b == null) continue;

            String buttonId = b.getId();
            // IMPORTANTE: Assicurati che l'ID del bottone nel FXML corrisponda alla chiave nel JSON/Repo
            // Esempio: Se il bottone è "Cap1", cerchiamo "cap1"
            String resourceId = buttonId.toLowerCase();

            ItemModel item = repo.getItem(resourceId);

            if (item != null) {
                // Setta il prezzo nella label corrispondente
                Label priceLabel = getPriceLabel(buttonId);
                if (priceLabel != null) {
                    priceLabel.setText(String.valueOf(item.getPrice()));
                }

                boolean isOwned = repo.isItemOwned(resourceId);
                int count = repo.getItemCount(resourceId);
                int limiteMassimo = resourceId.startsWith("pow") ? 4 : 1;

                // Logica Sold Out
                if ((!resourceId.startsWith("pow") && isOwned) || count >= limiteMassimo) {
                    soldOut(b);
                }
            } else {
                // Debug per evitare crash se un ID non corrisponde
                System.err.println("Oggetto non trovato per ID: " + resourceId);
            }
        }
    }

    @FXML
    private void AggiungiAlCarrello(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");

        // Cast a ToggleButton
        ToggleButton b = (ToggleButton) event.getSource();
        String resourceId = b.getId().toLowerCase();
        ItemModel item = GameRepository.getInstance().getItem(resourceId);

        int prezzo = (item != null) ? item.getPrice() : 0;
        int totaleAttuale = 0;
        try {
            totaleAttuale = Integer.parseInt(carrello.getText());
        } catch (NumberFormatException e) { totaleAttuale = 0; }

        // --- LOGICA SEMPLIFICATA CON TOGGLE BUTTON ---
        // isSelected() è gestito automaticamente da JavaFX quando clicchi
        if (b.isSelected()) {
            // Se è appena stato premuto (Attivo) -> Aggiungi prezzo
            carrello.setText(String.valueOf(totaleAttuale + prezzo));
            applicaEffettoSelezione(b);
        } else {
            // Se è stato deselezionato -> Rimuovi prezzo
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

        // Acquisto riuscito
        MusicManager.getInstance().playSoundEffect("item_bought.mp3");
        player.setGold(player.getGold() - spesa);
        carrello.setText("0");

        // Processiamo gli oggetti acquistati
        for (ToggleButton b : tuttiIBottoniDelNegozio()) {

            // Controlliamo se è selezionato usando il metodo nativo
            if (b.isSelected()) {
                String resourceId = b.getId().toLowerCase();

                if (resourceId.startsWith("pow")) {
                    applicaPotenziamento(resourceId, player);
                    repo.incrementItemCount(resourceId);

                    if (repo.getItemCount(resourceId) >= 4) {
                        soldOut(b);
                    } else {
                        // Reset visuale per prossimo acquisto
                        b.setSelected(false);
                        rimuoviEffettoSelezione(b);
                    }
                } else {
                    // Oggetti normali (Armor/Hats)
                    repo.incrementItemCount(resourceId);
                    player.addOwnedItem(resourceId);
                    soldOut(b);
                }
            }
        }

        repo.saveGameToJSON();
        DialogueLabel.setText("Grazie per l'acquisto!");
        resetDialogueAfterDelay();
    }

    // --- Metodi Helper ---

    private void applicaPotenziamento(String id, PlayerModel player) {
        int incremento = 2;
        GameRepository repo = GameRepository.getInstance();
        ItemModel item = repo.getItem(id);

        if (item != null) {
            // Aumento prezzo progressivo
            int nuovoPrezzo = item.getPrice() + 200;
            item.setPrice(nuovoPrezzo);

            // Aggiorno la label del prezzo
            // Nota: id è minuscolo (es: "pow1"), convertiamo in "Pow1" per trovare la label
            String buttonId = id.substring(0, 1).toUpperCase() + id.substring(1);
            Label labelPrezzo = getPriceLabel(buttonId);
            if (labelPrezzo != null) {
                labelPrezzo.setText(String.valueOf(nuovoPrezzo));
            }
        }

        // Logica cambio immagine tacche statistiche...
        // (Il resto del tuo codice per le immagini rimane uguale)
    }

    private Label getPriceLabel(String buttonId) {
        // Mappa ID Bottone -> Label Prezzo
        return switch (buttonId) {
            case "Cap1" -> Hat1; case "Cap2" -> Hat2; case "Cap3" -> Hat3;
            case "Dres1" -> Dress1; case "Dres2" -> Dress2; case "Dres3" -> Dress3;
            case "Pow1" -> Power1; case "Pow2" -> Power2; case "Pow3" -> Power3;
            // Gestione varianti ID se necessario (es. nel tuo FXML avevi Cap11, Cap12...)
            case "Cap11" -> Hat1; // Esempio se usi nomi diversi nell'FXML
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
}