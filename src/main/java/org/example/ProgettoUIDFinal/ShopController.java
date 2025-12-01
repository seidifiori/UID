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

import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.ItemModel;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ShopController implements Initializable {

    @FXML private Label soldi;
    @FXML private Label carrello;

    // Label Prezzi Grafiche
    @FXML private Label Hat1, Hat2, Hat3;
    @FXML private Label Dress1, Dress2, Dress3;
    @FXML private Label Power1, Power2, Power3;

    @FXML private Button BackButton;

    // Bottoni Oggetti
    @FXML private Button Cap1, Cap2, Cap3;
    @FXML private Button Dres1, Dres2, Dres3;
    @FXML private Button Pow1, Pow2, Pow3;

    @FXML private Label labelHomeSoldi;
    @FXML private Label DialogueLabel;

    private Scene homeScene;

    private List<Button> tuttiIBottoniDelNegozio() {
        return List.of(Cap1, Cap2, Cap3, Dres1, Dres2, Dres3, Pow1, Pow2, Pow3);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MusicManager.getInstance().playMusic("shop.mp3");
        PlayerModel player = GameRepository.getInstance().getPlayer();
        GameRepository repo = GameRepository.getInstance();

        if (soldi != null) {
            soldi.textProperty().bind(player.goldProperty().asString());
        }

        for (Button b : tuttiIBottoniDelNegozio()) {
            if (b == null) continue;

            String buttonId = b.getId();
            String resourceId = buttonId.toLowerCase();

            ItemModel item = repo.getItem(resourceId);

            if (item != null) {
                // Setta il prezzo
                Label priceLabel = getPriceLabel(buttonId);
                if (priceLabel != null) {
                    priceLabel.setText(String.valueOf(item.getPrice()));
                }

                // --- LOGICA INIZIALIZZAZIONE ---
                int count = repo.getItemCount(resourceId);

                // Se è un "pow" il limite è 4, altrimenti 1
                int limiteMassimo = resourceId.startsWith("pow") ? 4 : 1;

                // Se abbiamo raggiunto o superato il limite -> Sold Out
                if (count >= limiteMassimo) {
                    soldOut(b);
                }

                // NOTA: Ho rimosso il blocco 'if (repo.isItemOwned)' che avevi messo qui sotto.
                // Quello bloccava i potenziamenti subito dopo il primo acquisto.
                // Ci fidiamo solo del controllo sul 'limiteMassimo'.
            } else {
                System.err.println("Attenzione: Oggetto non trovato nel Repository per ID: " + resourceId);
            }
        }
    }

    @FXML
    private void AggiungiAlCarrello(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        Button b = (Button) event.getSource();
        String resourceId = b.getId().toLowerCase();
        ItemModel item = GameRepository.getInstance().getItem(resourceId);

        int prezzo = (item != null) ? item.getPrice() : 0;
        int totaleAttuale = 0;
        try {
            totaleAttuale = Integer.parseInt(carrello.getText());
        } catch (NumberFormatException e) { totaleAttuale = 0; }

        boolean isSelected = b.getUserData() != null && b.getUserData() instanceof Boolean && (boolean) b.getUserData();

        if (isSelected) {
            // Rimuovi dal carrello
            carrello.setText(String.valueOf(totaleAttuale - prezzo));
            b.setUserData(false);
            rimuoviEffettoSelezione(b);
        } else {
            // Aggiungi al carrello
            carrello.setText(String.valueOf(totaleAttuale + prezzo));
            b.setUserData(true);
            applicaEffettoSelezione(b);
        }
    }

    @FXML
    private void ConfermaAcquisto(ActionEvent event) {

        GameRepository repo = GameRepository.getInstance();
        PlayerModel player = repo.getPlayer();

        int spesa = 0;
        try {
            MusicManager.getInstance().playSoundEffect("item_bought.mp3");
            spesa = Integer.parseInt(carrello.getText());
        } catch (NumberFormatException e) { spesa = 0; }

        if (player.getGold() < spesa) {
            DialogueLabel.setText("Soldi insufficienti!");
            resetDialogueAfterDelay();
            return;
        }

        // 1. Scala i soldi
        player.setGold(player.getGold() - spesa);
        carrello.setText("0");


        // 2. Consegna la merce
        for (Button b : tuttiIBottoniDelNegozio()) {
            Object userData = b.getUserData();

            // Controlla se è selezionato (true)
            if (userData instanceof Boolean && (Boolean) userData) {
                String resourceId = b.getId().toLowerCase();

                if (resourceId.startsWith("pow")) {
                    applicaPotenziamento(resourceId, player);

                    // Incrementiamo il contatore
                    repo.incrementItemCount(resourceId);

                    // Controllo: Ho raggiunto il limite di 4?
                    if (repo.getItemCount(resourceId) >= 4) {
                        soldOut(b); // Bloccalo per sempre
                        b.setUserData(false);
                    } else {
                        // Se non ho finito, resetto solo la selezione visiva
                        b.setUserData(false);
                        rimuoviEffettoSelezione(b);
                    }
                }
                else {
                    // CASO B: OGGETTO FISICO (Cappello/Vestito)
                    repo.incrementItemCount(resourceId); // Segna come posseduto
                    soldOut(b); // Blocca subito (limite è 1)
                }
            }
        }

        DialogueLabel.setText("Grazie per l'acquisto!");
        resetDialogueAfterDelay();
    }

    // --- Metodi Helper ---

    private void applicaPotenziamento(String id, PlayerModel player) {
        int incremento = 2; // O quello che vuoi tu

        // 1. Recuperiamo l'oggetto dal Repository (IL CERVELLO)
        ItemModel item = GameRepository.getInstance().getItem(id);

        if (item == null) {
            System.err.println("Errore gravissimo: Item " + id + " non esiste nel repository!");
            return;
        }

        // 2. Aumentiamo il prezzo nel Modello (DATI)
        int vecchioPrezzo = item.getPrice();
        int nuovoPrezzo = vecchioPrezzo + 200; // Inflazione
        item.setPrice(nuovoPrezzo);

        // 3. Aggiorniamo la Grafica (FACCIA) prendendo il dato dal Modello
        // Usiamo il metodo helper getPriceLabel che hai già scritto (o dovresti avere)
        // L'ID del bottone solitamente inizia con la maiuscola (Pow1), l'id risorsa è minuscolo (pow1)

        // Trucco veloce per convertire pow1 -> Pow1 per trovare la label
        String buttonId = id.substring(0, 1).toUpperCase() + id.substring(1);
        Label labelPrezzo = getPriceLabel(buttonId);

        if (labelPrezzo != null) {
            labelPrezzo.setText(String.valueOf(nuovoPrezzo));
        }

        // 4. Applichiamo la statistica al player
        switch (id) {
            case "pow1": // Forza
                int nuovoAtk = player.getAtk() + incremento;
                if (nuovoAtk > 100) nuovoAtk = 1;
                player.setAtk(nuovoAtk);
                break;
            case "pow2": // Difesa
                int nuovaDef = player.getDef() + incremento;
                if (nuovaDef > 100) nuovaDef = 1;
                player.setDef(nuovaDef);
                break;
            case "pow3": // Velocità
                int nuovaVel = player.getVel() + incremento;
                if (nuovaVel > 100) nuovaVel = 1;
                player.setVel(nuovaVel);
                break;
        }
    }

    private Label getPriceLabel(String buttonId) {
        return switch (buttonId) {
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
            b.setUserData(false);
        }
    }

    private void resetDialogueAfterDelay() {
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> DialogueLabel.setText("Cosa Posso fare per te?"));
        pause.play();
    }

    @FXML
    public void setHomeScene(Scene scene) { this.homeScene = scene; }

    @FXML
    public void Home() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");

        // AGGIUNGI QUESTA RIGA: Rimetti la musica principale
        MusicManager.getInstance().playMusic("background_music.mp3");

        if (homeScene != null) {
            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        }
    }
}