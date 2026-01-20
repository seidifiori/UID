package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.Services.GameRepository;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;

import java.net.URL;
import java.util.ResourceBundle;

public class RecollectionController implements Initializable {

    @FXML private AnchorPane rootPane;
    private Scene bossLobbyScene;

    @FXML private ImageView boss0; // Nergigante
    @FXML private ImageView boss1; // Maga
    @FXML private ImageView boss2; // Artorias

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        StyleManager.getInstance().applyStyle(rootPane);

        // Applichiamo l'oscuramento in base ai progressi
        updateBossData();
    }

    private void updateBossData() {
        GameRepository repo = GameRepository.getInstance();

        // Controlliamo ogni boss (usando degli ID ipotetici "nergigante", "maga", "artorias")
        applyLockEffect(boss0, repo.isBossDefeated("nergigante"));
        applyLockEffect(boss1, repo.isBossDefeated("maga"));
        applyLockEffect(boss2, repo.isBossDefeated("artorias"));
    }

    /**
     * Applica l'effetto oscurato se il boss NON è stato battuto.
     */
    private void applyLockEffect(ImageView iv, boolean isDefeated) {
        if (iv == null) return;

        if (isDefeated) {
            // Boss battuto: mostralo normale
            iv.setEffect(null);
            iv.setOpacity(1.0);
        } else {
            // Boss NON battuto: oscuralo (come nel Closet)
            ColorAdjust darken = new ColorAdjust();
            darken.setSaturation(-1.0); // Rende in bianco e nero
            darken.setBrightness(-0.8); // Molto scuro

            iv.setEffect(darken);
            iv.setOpacity(0.5); // Leggera trasparenza
        }
    }

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