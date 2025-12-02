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
import javafx.scene.image.Image;

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
    @FXML private ImageView Atk1,Atk2,Atk3,Def1,Def2,Def3,Spd1,Spd2,Spd3;

    private Scene homeScene;

    private List<Button> tuttiIBottoniDelNegozio() {
        return List.of(Cap1, Cap2, Cap3, Dres1, Dres2, Dres3, Pow1, Pow2, Pow3);
    }
    private List<ImageView> tuttoAtk() {
        return List.of(Atk1, Atk2, Atk3);
    }
    private List<ImageView> tuttoDef() {
        // Rimossa la virgola prima di Def1
        return List.of(Def1, Def2, Def3);
    }
    private List<ImageView> tuttoSpd() {
        // Rimossa la virgola prima di Spd1
        return List.of(Spd1, Spd2, Spd3);
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

        // 1. Calcoliamo PRIMA la spesa.
        // Non puoi controllare una variabile che non esiste ancora.
        int spesa = 0;
        try {
            spesa = Integer.parseInt(carrello.getText());
        } catch (NumberFormatException e) {
            spesa = 0;
        }

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


        MusicManager.getInstance().playSoundEffect("item_bought.mp3");


        player.setGold(player.getGold() - spesa);
        carrello.setText("0");


        for (Button b : tuttiIBottoniDelNegozio()) {
            Object userData = b.getUserData();

            if (userData instanceof Boolean && (Boolean) userData) {
                String resourceId = b.getId().toLowerCase();

                if (resourceId.startsWith("pow")) {
                    applicaPotenziamento(resourceId, player);

                    repo.incrementItemCount(resourceId);
                    if (repo.getItemCount(resourceId) >= 4) {
                        soldOut(b); // Bloccalo per sempre
                        b.setUserData(false);
                    } else {
                        b.setUserData(false);
                        rimuoviEffettoSelezione(b);
                    }
                }
                else {
                    repo.incrementItemCount(resourceId); // Segna come posseduto
                    soldOut(b);
                }
            }
        }

        DialogueLabel.setText("Grazie per l'acquisto!");
        resetDialogueAfterDelay();
    }

    // --- Metodi Helper ---

    private void applicaPotenziamento(String id, PlayerModel player) {
        int incremento = 2;
        GameRepository repo = GameRepository.getInstance();

        // 1. Recuperiamo l'oggetto
        ItemModel item = repo.getItem(id);
        if (item == null) return;

        // 2. Aumentiamo il prezzo (Inflazione)
        int nuovoPrezzo = item.getPrice() + 200;
        item.setPrice(nuovoPrezzo);

        // Aggiorna etichetta prezzo
        String buttonId = id.substring(0, 1).toUpperCase() + id.substring(1);
        Label labelPrezzo = getPriceLabel(buttonId);
        if (labelPrezzo != null) {
            labelPrezzo.setText(String.valueOf(nuovoPrezzo));
        }

        // 3. CAMBIO IMMAGINE PROGRESSIVO (La parte che ti interessa)

        // Percorso dell'immagine "PIENA" (Il rombo giallo/illuminato)
        // Assicurati che questo percorso sia corretto rispetto alla tua cartella resources
        String imagePath = getClass().getResource("/org/example/ProgettoUIDFinal/imagini/Shop/items/not-only-are-deltarune-save-points-a-different-color-than-v0-5ivw5efo1j5c1-removebg-preview.png").toExternalForm();
        Image imgPiena = new Image(imagePath);

        // Recuperiamo quanti ne abbiamo già comprati per sapere quale indice illuminare
        int index = repo.getItemCount(id);

        // Seleziona la lista corretta e aggiorna SOLO l'immagine corrente
        List<ImageView> targetList = null;

        switch (id) {
            case "pow1":
                targetList = tuttoAtk();
                // Logica Player
                player.setAtk(player.getAtk() + incremento > 100 ? 1 : player.getAtk() + incremento);
                break;
            case "pow2":
                targetList = tuttoDef();
                // Logica Player
                player.setDef(player.getDef() + incremento > 100 ? 1 : player.getDef() + incremento);
                break;
            case "pow3":
                targetList = tuttoSpd();
                // Logica Player
                player.setVel(player.getVel() + incremento > 100 ? 1 : player.getVel() + incremento);
                break;
        }

        // Applica l'immagine solo se l'indice è valido (evita crash se compri il 4° per sbaglio)
        if (targetList != null && index < targetList.size()) {
            targetList.get(index).setImage(imgPiena);
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