package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.model.GameRepository;

import java.util.prefs.Preferences;


public class profilePicChooserController {

    @FXML
    private Button confirmPic, confirmBanner;
    @FXML
    private ToggleButton pic1, pic2, pic3, pic4;
    private final ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private ToggleButton banner1, banner2, banner3, banner4;
    private final ToggleGroup toggleBannerGroup = new ToggleGroup();

    @FXML
    private StackPane picChooserPane;
    @FXML
    private ScrollPane bannerScrollPane;

    private GridPane blurredPane;
    private profileController mainController;

    @FXML
    public void initialize() {
        // Ignoriamo il Repository per ora, visto che non trovi il file properties.

        // Definiamo i percorsi ESATTI delle 4 immagini a mano qui.
        // Assicurati che questi percorsi siano corretti (iniziano con /org/...)
        String basePath = "/org/example/ProgettoUIDFinal/imagini/Icons/";

        String[] hardcodedPaths = {
                basePath + "chr_icon_1052.png", // pic1
                basePath + "chr_icon_1007.png", // pic2
                basePath + "chr_icon_1025.png", // pic3
                basePath + "chr_icon_1053.png"  // pic4
        };

        int totaleImmagini = hardcodedPaths.length;

        // --- IL CICLO ---
        for (int i = 0; i < totaleImmagini; i++) {

            // L'indice dell'array parte da 0, ma i tuoi bottoni si chiamano pic1, pic2...
            int buttonIndex = i + 1;
            String buttonId = "#pic" + buttonIndex;

            ToggleButton btn = (ToggleButton) picChooserPane.lookup(buttonId);

            if (btn != null) {
                btn.setToggleGroup(toggleGroup);

                // Prendiamo il percorso dall'array invece che dal file rotto
                String path = hardcodedPaths[i];

                // Debug per farti felice
                System.out.println("Configuro " + buttonId + " con path: " + path);

                // Assegniamo il percorso (UserData non sarà MAI null ora)
                btn.setUserData(path);

            } else {
                System.err.println("Non ho trovato il bottone con ID: " + buttonId);
            }
        }

        // --- BANNER (Restano uguali) ---
        banner1.setToggleGroup(toggleBannerGroup);
        banner2.setToggleGroup(toggleBannerGroup);
        banner3.setToggleGroup(toggleBannerGroup);
        banner4.setToggleGroup(toggleBannerGroup);

        banner1.setUserData("@imagini/profile/banners/Banner1.png");
        banner2.setUserData("@imagini/profile/banners/Banner2.png");
        banner3.setUserData("@imagini/profile/banners/Banner3.jpg");
        banner4.setUserData("@imagini/profile/banners/Banner4.png");
    }


    @FXML
    private void handleConfirmClick(ActionEvent event) {


        ToggleButton selected = (ToggleButton) toggleGroup.getSelectedToggle();

        if (selected != null) {

            // 1. Recupera il path completo salvato nel UserData
            String fullPath = (String) selected.getUserData();

            System.out.println("DEBUG CHOOSER - Hai selezionato: " + fullPath);
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");

            if (fullPath != null) {
                // 2. Aggiorna tutto tramite il Repository
                GameRepository.getInstance().changePlayerAvatar(fullPath);
                System.out.println("DEBUG CHOOSER - Richiesta inviata al Repository.");
            } else {
                System.err.println("ERRORE: Il bottone selezionato ha UserData NULL! Controlla character.properties");
            }
        } else {
            System.out.println("Nessun bottone selezionato.");
        }

        closeWindow();
    }

    public void initData(profileController mainController, GridPane mainContentPane, String currentBannerUrl) {

        this.mainController = mainController;
        this.blurredPane = mainContentPane;

        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);

        String defaultKey = "profile.pic1";
        String defaultPath = GameRepository.getInstance().getAvatarPathByKey(defaultKey);

        String savedPath = prefs.get("saved.avatar.path", defaultPath);

        //serve ad avere sempre selezionata la propria immagine profilo
        for (Toggle toggle : toggleGroup.getToggles()) {
            ToggleButton button = (ToggleButton) toggle;
            String buttonUrl = (String) button.getUserData();

            if (buttonUrl != null && buttonUrl.equals(savedPath)) {
                toggleGroup.selectToggle(toggle); // Usa selectToggle invece di setSelected per il gruppo
                break;
            }
        }

        //serve ad avere sempre selezionato il proprio banner
        for (Toggle toggle : toggleBannerGroup.getToggles()) {
            ToggleButton button = (ToggleButton) toggle;
            String buttonUrl = (String) button.getUserData();

            if (buttonUrl != null && buttonUrl.equals(currentBannerUrl)) {
                button.setSelected(true);
                break;
            }
        }

        bannerScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaX() != 0) {
                event.consume();
            }
        });
    }



    @FXML
    private void handleConfirmBannerClick(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");

        ToggleButton selected = (ToggleButton) toggleBannerGroup.getSelectedToggle();

        if (selected != null) {
            String imageUrl = (String) selected.getUserData();
            mainController.updateBannerPicture(imageUrl);

            //salva l'immagine
            Preferences prefs = Preferences.userNodeForPackage(profileController.class);
            prefs.put("banner_url", imageUrl);
        }

        closeWindow();
    }

    private void closeWindow() {
        if (blurredPane != null) {
            blurredPane.setEffect(null);
            blurredPane.setDisable(false);
        }
        if (picChooserPane != null && picChooserPane.getParent() != null) {
            ((StackPane) picChooserPane.getParent()).getChildren().remove(picChooserPane);
        }
    }

}