package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class RecollectionController implements Initializable {

    @FXML private AnchorPane rootPane; // Deve coincidere con fx:id nell'FXML
    private Scene bossLobbyScene;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Questa riga risolve il problema del FONT
        StyleManager.getInstance().applyStyle(rootPane);
    }

    /**
     * Riceve la scena della Boss Lobby per poter tornare indietro.
     * Viene chiamato dal bossController tramite Reflection.
     */
    public void setHomeScene(Scene scene) {
        this.bossLobbyScene = scene;
    }

    @FXML
    void handleBack(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        if (bossLobbyScene != null) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(bossLobbyScene);
        }
    }
}