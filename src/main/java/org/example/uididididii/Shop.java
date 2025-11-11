package org.example.uididididii;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Shop {

    @FXML private Label soldi;   // ad esempio: 100 euro
    @FXML private Label carrello;
    @FXML private Label Hat1;
    @FXML private Label Hat2;
    @FXML private Label Hat3;
    @FXML private Label Dress1;
    @FXML private Label Dress2;
    @FXML private Label Dress3;
    @FXML private Label Power1;
    @FXML private Label Power2;
    @FXML private Label Power3;
    @FXML private Button BackButton;
    @FXML private Button Cap1;
    @FXML private Button Cap2;
    @FXML private Button Cap3;
    @FXML private Button Dres1;
    @FXML private Button Dres2;
    @FXML private Button Dres3;
    @FXML private Button Pow1;
    @FXML private Button Pow2;
    @FXML private Button Pow3;
    @FXML private Label labelHomeSoldi;
    @FXML private Scene homeScene;  // servirà per tornare indietro;

    private List<Button> tuttiIBottoniDelNegozio() {
        return List.of(Cap1, Cap2, Cap3, Dres1, Dres2, Dres3, Pow1, Pow2, Pow3);
    }
    @FXML
    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }
    @FXML
    public void setSoldiLabel(Label homeLabel) {
        this.labelHomeSoldi = homeLabel;
        soldi.setText(homeLabel.getText());
    }

    @FXML
    private void ConfermaAcquisto(ActionEvent event) {
        int soldiAttuali = Integer.parseInt(soldi.getText());
        int spesa = Integer.parseInt(carrello.getText());
        int risultato = soldiAttuali - spesa;

        if (risultato < 0) {
            System.out.println("Soldi insufficienti!");
            return;
        }

        soldi.setText(String.valueOf(risultato));
        carrello.setText("0");

        if (labelHomeSoldi != null) {
            labelHomeSoldi.setText(String.valueOf(risultato));
        }

        // 🔹 Salva gli oggetti acquistati
        for (Button b : tuttiIBottoniDelNegozio()) {
            Boolean bought = (Boolean) b.getUserData();
            if (bought != null && bought) {
                if (b.getGraphic() instanceof ImageView iv) {
                    InventoryService.getInstance().addItem(b.getId(), iv.getImage());
                }
                b.setUserData(false);
                rimuoviEffettoSelezione(b);
            }
        }
    }



    @FXML
    private void AggiungiAlCarrello(ActionEvent event) {
        Button b = (Button) event.getSource();
        String id = b.getId();  // es. "Cap1", "Dres2", ecc.

        Label prezzoLabel = switch (id) {
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

        if (prezzoLabel == null) return;

        // === PARTE 1: Gestione del carrello ===
        int prezzo = Integer.parseInt(prezzoLabel.getText());
        int totale = Integer.parseInt(carrello.getText());

        // Controlla se il bottone è già selezionato o meno
        boolean isSelected = b.getUserData() != null && (boolean) b.getUserData();

        if (isSelected) {
            // Se era selezionato → toglilo dal carrello
            carrello.setText(String.valueOf(totale - prezzo));
            b.setUserData(false); // aggiorna stato
            rimuoviEffettoSelezione(b);
        } else {
            // Se NON era selezionato → aggiungilo
            carrello.setText(String.valueOf(totale + prezzo));
            b.setUserData(true);
            applicaEffettoSelezione(b);
        }
    }
    @FXML
    private void applicaEffettoSelezione(Button b) {
        if (b.getGraphic() instanceof ImageView iv) {
            ColorAdjust darken = new ColorAdjust();
            darken.setBrightness(-0.5); // scurisce l'immagine
            iv.setEffect(darken);
        }
    }
    @FXML
    private void rimuoviEffettoSelezione(Button b) {
        if (b.getGraphic() instanceof ImageView iv) {
            iv.setEffect(null); // rimuove l'effetto
        }
    }
    private void soldOut(Button b) {
            if (b.getGraphic() instanceof ImageView iv) {
                double w = iv.getFitWidth();
                double h = iv.getFitHeight();

                ImageView original = new ImageView(iv.getImage());
                original.setFitWidth(w);
                original.setFitHeight(h);
                original.setPreserveRatio(true);

                javafx.scene.shape.Rectangle overlay = new javafx.scene.shape.Rectangle(h, h);
                overlay.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.6));

                Label soldOut = new Label("SOLD OUT");
                soldOut.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 18px;");

                StackPane stack = new StackPane(original, overlay, soldOut);
                stack.setAlignment(javafx.geometry.Pos.CENTER);
                stack.setPrefSize(w, h);
                stack.setMaxSize(w, h);
                stack.setMinSize(w, h);

                b.setPadding(javafx.geometry.Insets.EMPTY);
                b.setAlignment(javafx.geometry.Pos.CENTER);
                b.setGraphic(stack);
            }


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

