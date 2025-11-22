package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.example.ProgettoUIDFinal.model.GameRepository;

import java.util.prefs.Preferences;


public class profilePicChooserController {

    @FXML private Button confirmPic, confirmBanner;
    @FXML private ToggleButton pic1, pic2, pic3, pic4;
    private final ToggleGroup toggleGroup = new ToggleGroup();

    @FXML private ToggleButton banner1, banner2, banner3, banner4;
    private final ToggleGroup toggleBannerGroup = new ToggleGroup();

    @FXML private StackPane picChooserPane;
    @FXML private ScrollPane bannerScrollPane;

    private GridPane blurredPane;
    private profileController mainController;


    public void initialize() {
        GameRepository repo = GameRepository.getInstance();

        //Toggles per l'immagine profilo
        pic1.setToggleGroup(toggleGroup);
        pic2.setToggleGroup(toggleGroup);
        pic3.setToggleGroup(toggleGroup);
        pic4.setToggleGroup(toggleGroup);

        pic1.setUserData(repo.getAvatarPathByKey("profile.pic1"));
        pic2.setUserData(repo.getAvatarPathByKey("profile.pic2"));
        pic3.setUserData(repo.getAvatarPathByKey("profile.pic3"));
        pic4.setUserData(repo.getAvatarPathByKey("profile.pic4"));

        //questo blocco serve a non far cambiare dimensione alla label
        pic1.setFocusTraversable(false);
        pic2.setFocusTraversable(false);
        pic3.setFocusTraversable(false);
        pic4.setFocusTraversable(false);
        confirmPic.setFocusTraversable(false);

        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                toggleGroup.selectToggle(oldToggle);
            }
        });

        //Toggles per il banner
        banner1.setToggleGroup(toggleBannerGroup);
        banner2.setToggleGroup(toggleBannerGroup);
        banner3.setToggleGroup(toggleBannerGroup);
        banner4.setToggleGroup(toggleBannerGroup);

        banner1.setUserData("@images/Banner1.png");
        banner2.setUserData("@images/Banner2.png");
        banner3.setUserData("@images/Banner3.jpg");
        banner4.setUserData("@images/Banner4.png");

        //questo blocco serve a non far cambiare dimensione alla label
        banner1.setFocusTraversable(false);
        banner2.setFocusTraversable(false);
        banner3.setFocusTraversable(false);
        banner4.setFocusTraversable(false);
        confirmBanner.setFocusTraversable(false);

        toggleBannerGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                toggleBannerGroup.selectToggle(oldToggle);
            }
        });


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
    private void handleConfirmClick(ActionEvent event) {
        ToggleButton selected = (ToggleButton) toggleGroup.getSelectedToggle();

        if (selected != null) {
            // 1. Recupera il path completo salvato nel UserData
            String fullPath = (String) selected.getUserData();

            if (fullPath != null) {
                // 2. Aggiorna tutto tramite il Repository
                GameRepository.getInstance().changePlayerAvatar(fullPath);
            }
        }

        closeWindow();
    }

    @FXML
    private void handleConfirmBannerClick(ActionEvent event) {

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
