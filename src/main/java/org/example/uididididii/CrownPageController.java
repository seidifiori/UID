package org.example.uididididii;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class CrownPageController implements Initializable {

    @FXML private ToggleButton Cap1, Cap2, Cap3, Cap4, Cap5, Cap6, Cap7, Cap8, Cap9;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateCapImages();
    }

    public void updateCapImages() {
        ToggleButton[] caps = {Cap1, Cap2, Cap3, Cap4, Cap5, Cap6, Cap7, Cap8, Cap9};

        for (ToggleButton tb : caps) {
            if (tb == null) continue;

            Image ownedImage = InventoryService.getInstance().getItemImage(tb.getId());
            if (ownedImage != null && tb.getGraphic() instanceof ImageView iv) {
                iv.setImage(ownedImage);
            }

            // Protezione: rendi immagine cliccabile trasparente se vuoi
            ImageView iv = findImageView(tb.getGraphic());
            if (iv != null) iv.setMouseTransparent(true);

            attachSelectionListener(tb);
        }
    }

    private void attachSelectionListener(ToggleButton tb) {
        tb.selectedProperty().addListener((obs, oldVal, newVal) -> {
            ImageView iv = findImageView(tb.getGraphic());
            if (iv != null) {
                if (newVal) iv.setOpacity(0.6);
                else iv.setOpacity(1.0);
            }
        });
    }

    private ImageView findImageView(javafx.scene.Node node) {
        if (node instanceof ImageView iv) return iv;
        if (node instanceof javafx.scene.Parent p) {
            for (javafx.scene.Node child : p.getChildrenUnmodifiable()) {
                ImageView result = findImageView(child);
                if (result != null) return result;
            }
        }
        return null;
    }
}
