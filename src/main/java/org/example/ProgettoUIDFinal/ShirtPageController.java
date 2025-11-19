package org.example.ProgettoUIDFinal;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class ShirtPageController implements Initializable {

    @FXML private ToggleButton Dres1,Dres2,Dres3,Dres4,Dres5,Dres6,Dres7,Dres8,Dres9;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        updateDresImages();
    }

    public void updateDresImages() {
        ToggleButton[] caps = {Dres1,Dres2,Dres3,Dres4,Dres5,Dres6,Dres7,Dres8,Dres9};

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