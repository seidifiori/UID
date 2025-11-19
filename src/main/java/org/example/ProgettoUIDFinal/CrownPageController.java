package org.example.ProgettoUIDFinal;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup; // 👈 ASSICURATI DI IMPORTARE QUESTO
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.List; // 👈 ASSICURATI DI IMPORTARE QUESTO
import java.util.ResourceBundle;

public class CrownPageController implements Initializable {

    @FXML private ToggleButton Cap1, Cap2, Cap3, Cap4, Cap5, Cap6, Cap7, Cap8, Cap9;

    // Questo campo verrà "iniettato" da HelloController
    private ImageView hatPreview;

    /**
     * Chiamato da HelloController per passare l'ImageView del personaggio.
     */
    public void setHatPreview(ImageView hatPreview) {
        this.hatPreview = hatPreview;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ToggleGroup hatGroup = new ToggleGroup();

        // 1. 👉 LISTA COMPLETA DI TUTTI I BOTTONI
        List<ToggleButton> hatButtons = List.of(
                Cap1, Cap2, Cap3, Cap4, Cap5, Cap6, Cap7, Cap8, Cap9
        );

        for (ToggleButton capButton : hatButtons) {
            if (capButton != null) {
                capButton.setToggleGroup(hatGroup);

                // Questo è l'unico listener di cui hai bisogno
                capButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                    if (isSelected) {
                        // Se il bottone è selezionato, applica la sua immagine all'anteprima
                        applyPreviewImage(capButton);
                    } else if (hatGroup.getSelectedToggle() == null) {
                        // Se nessun bottone è selezionato (perché l'utente ha cliccato
                        // di nuovo su quello attivo), rimuovi l'immagine dall'anteprima.
                        if (hatPreview != null) {
                            hatPreview.setImage(null);
                        }
                    }
                });
            }
        }
    }

    /**
     * 2. 👉 METODO MANCANTE, ORA AGGIUNTO
     * Prende l'immagine dal bottone cliccato e la imposta sull'ImageView
     * (hatPreview) che ci ha passato HelloController.
     */
    private void applyPreviewImage(ToggleButton button) {
        if (hatPreview == null) {
            System.err.println("hatPreview non è stato impostato da HelloController!");
            return;
        }

        ImageView iv = findImageView(button.getGraphic());
        if (iv != null) {
            hatPreview.setImage(iv.getImage());
        }
    }


    public void updateCapImages() {
        ToggleButton[] caps = {Cap1, Cap2, Cap3, Cap4, Cap5, Cap6, Cap7, Cap8, Cap9};

        for (ToggleButton tb : caps) {
            if (tb == null) continue;

            Image ownedImage = InventoryService.getInstance().getItemImage(tb.getId());
            ImageView iv = findImageView(tb.getGraphic());

            if (ownedImage != null) {
                // Oggetto POSSEDUTO:
                // Imposta l'immagine corretta e abilita il bottone
                if (iv != null) {
                    iv.setImage(ownedImage);
                }
                tb.setDisable(false);
            } else {
                // Oggetto NON POSSEDUTO:
                // Disabilita il bottone. L'utente lo vedrà ma non potrà cliccarlo.
                tb.setDisable(true);
            }

            // Rendi l'immagine interna trasparente ai click (buona prassi)
            if (iv != null) {
                iv.setMouseTransparent(true);
            }
        }
    }

    /**
     * Metodo helper per trovare l'ImageView (già presente nel tuo codice)
     */
    private ImageView findImageView(Node node) {
        if (node instanceof ImageView iv) return iv;
        if (node instanceof javafx.scene.Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                ImageView result = findImageView(child);
                if (result != null) return result;
            }
        }
        return null;
    }

    // 4. ❌ Il metodo 'attachSelectionListener' è stato RIMOSSO perché ridondante.
}