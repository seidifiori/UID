package org.example.ProgettoUIDFinal;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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

public class SettingsController {

    @FXML private ChoiceBox<String> choiceBox;

    public void initialize() {
        choiceBox.getItems().addAll("Piccolo", "Medio","Grande");
    }
    @FXML private Scene homeScene;
    @FXML private Button BackButton;
    public void setHomeScene(Scene scene) { this.homeScene = scene; }
    // Dentro SettingsController.java

    @FXML
    private void choiceChanged() {
        String selected = choiceBox.getValue();

        // Recuperiamo la radice della scena attuale.
        // Nota: BackButton deve essere dentro la scena per funzionare,
        // oppure usa un qualsiasi altro nodo visibile per ottenere la scena.
        if (choiceBox.getScene() == null || choiceBox.getScene().getRoot() == null) {
            return; // Evitiamo crash se la scena non è pronta
        }

        String fontSizeStyle = "";

        switch (selected) {
            case "Piccolo": // O "Option 1" se sei pigro
                // Impostiamo la base a 12px. I label "em" scaleranno di conseguenza.
                fontSizeStyle = "-fx-font-size: 12px;";
                break;

            case "Medio":
                fontSizeStyle = "-fx-font-size: 14px;";
                break;

            case "Grande": // Per i tuoi simili miopi
                fontSizeStyle = "-fx-font-size: 18px;";
                break;

            default:
                return;
        }

        // Applichiamo lo stile alla RADICE.
        // JavaFX è abbastanza intelligente da ricalcolare tutti gli 'em'.
        choiceBox.getScene().getRoot().setStyle(fontSizeStyle);

        System.out.println("Dimensione testo cambiata. Riesci a leggere ora?");
    }
    @FXML  public void Home() {
        if (homeScene != null) {
        Stage currentStage = (Stage) BackButton.getScene().getWindow();
        currentStage.setScene(homeScene);
    }
    }
}
