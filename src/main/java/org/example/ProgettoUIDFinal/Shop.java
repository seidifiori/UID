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

public class Shop implements Initializable {

    @FXML private Label soldi;
    @FXML private Label carrello;

    // Label Prezzi Grafiche
    @FXML private Label Hat1, Hat2, Hat3;
    @FXML private Label Dress1, Dress2, Dress3;
    @FXML private Label Power1, Power2, Power3;

    @FXML private Button BackButton;

    // Bottoni Oggetti (Assicurati che fx:id nel FXML sia: Cap1, Cap2... Dres1... Pow1...)
    @FXML private Button Cap1, Cap2, Cap3;
    @FXML private Button Dres1, Dres2, Dres3;
    @FXML private Button Pow1, Pow2, Pow3;

    @FXML private Label labelHomeSoldi;
    @FXML private Label DialogueLabel;

    private Scene homeScene;

    private List<Button> tuttiIBottoniDelNegozio() {
        // Aggiungi qui tutti i bottoni che hai nel negozio
        return List.of(Cap1, Cap2, Cap3, Dres1, Dres2, Dres3, Pow1, Pow2, Pow3);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PlayerModel player = GameRepository.getInstance().getPlayer();
        GameRepository repo = GameRepository.getInstance(); // <--- Riferimento al repo

        if (soldi != null) {
            soldi.textProperty().bind(player.goldProperty().asString());
        }

        for (Button b : tuttiIBottoniDelNegozio()) {
            if (b == null) continue;

            String buttonId = b.getId();
            String resourceId = buttonId.toLowerCase();

            ItemModel item = repo.getItem(resourceId); // Usa repo invece di chiamare singleton ogni volta

            if (item != null) {
                Label priceLabel = getPriceLabel(buttonId);
                if (priceLabel != null) {
                    priceLabel.setText(String.valueOf(item.getPrice()));
                }

                if (repo.isItemOwned(resourceId)) {
                    soldOut(b);
                }
            } else {
                System.err.println("Attenzione: Oggetto non trovato nel Repository per ID: " + resourceId);
            }
        }
    }

    @FXML
    private void AggiungiAlCarrello(ActionEvent event) {
        Button b = (Button) event.getSource();

        // Recupera l'ID risorsa (minuscolo)
        String resourceId = b.getId().toLowerCase();

        // CHIEDIAMO IL PREZZO AL MODEL (RESOURCES), NON ALLA LABEL
        ItemModel item = GameRepository.getInstance().getItem(resourceId);

        // Se l'item non esiste (es. Pow1 non configurato), usiamo 0 per non crashare
        int prezzo = (item != null) ? item.getPrice() : 0;

        int totaleAttuale = Integer.parseInt(carrello.getText());
        boolean isSelected = b.getUserData() != null && (boolean) b.getUserData();

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
        GameRepository repo = GameRepository.getInstance(); // <--- Riferimento al repo
        PlayerModel player = repo.getPlayer();

        int spesa = Integer.parseInt(carrello.getText());

        if (player.getGold() < spesa) {
            DialogueLabel.setText("Soldi insufficienti!");
            resetDialogueAfterDelay();
            return;
        }

        // Scala i soldi dal Player
        player.setGold(player.getGold() - spesa);
        carrello.setText("0");

        // Consegna oggetti
        for (Button b : tuttiIBottoniDelNegozio()) {
            Boolean selected = (Boolean) b.getUserData();
            if (Boolean.TRUE.equals(selected)) {
                String resourceId = b.getId().toLowerCase();


                repo.markItemAsOwned(resourceId);

                // Aggiorna grafica
                soldOut(b);
            }
        }

        DialogueLabel.setText("Grazie per l'acquisto!");
        resetDialogueAfterDelay();
    }

    // --- Metodi Helper ---

    private Label getPriceLabel(String buttonId) {
        // Mappa l'ID del bottone (es. "Cap1") alla Label del prezzo (es. Hat1)
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
            // Creiamo snapshot quadrato
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
            b.setUserData(false); // Deseleziona logica
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
        if (homeScene != null) {
            Stage currentStage = (Stage) BackButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        }
    }
}