package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.util.prefs.Preferences;

/**
 * CONTROLLER SELETTORE PROFILO: Gestisce l'interfaccia di scelta per l'avatar e il banner.
 * Funziona come una finestra modale sovrapposta (Overlay) che interagisce con il profileController
 * per aggiornare l'aspetto estetico del giocatore.
 */
public class profilePicChooserController {

    // --- ELEMENTI UI (Iniezione FXML) ---
    @FXML private ToggleButton profilePicButton, bannerButton;

    @FXML private StackPane picChooserPane, profileImage, bannerImage;
    @FXML private ToggleButton pic1, pic2, pic3, pic4, banner1, banner2, banner3, banner4;

    private final ToggleGroup tabToggleGroup = new ToggleGroup();
    private final ToggleGroup picToggleGroup = new ToggleGroup();
    private final ToggleGroup bannerToggleGroup = new ToggleGroup();

    private GridPane blurredPane;
    private profileController mainController;
    private PlayerModel player;


    /**
     * INIZIALIZZAZIONE: Configura i ToggleGroup e associa i percorsi delle risorse.
     * Utilizza il campo 'UserData' dei bottoni per memorizzare i percorsi dei file (String)
     * direttamente all'interno dei componenti grafici.
     */
    @FXML
    public void initialize() {
        this.player = GameRepository.getInstance().getPlayer();

        // Colleghiamo i bottoni ai gruppi e assegniamo i path
        setupButtons();

        profilePicButton.setToggleGroup(tabToggleGroup);
        bannerButton.setToggleGroup(tabToggleGroup);
        profilePicButton.setSelected(true);

        addPreventDeselectionListener(picToggleGroup);
        addPreventDeselectionListener(bannerToggleGroup);
        addPreventDeselectionListener(tabToggleGroup);

        // Switch iniziale
        profileImage.setVisible(true);
        bannerImage.setVisible(false);
    };

    /**
     * GESTIONE CONFERMA AVATAR: Recupera la scelta effettuata e aggiorna il modello globale.
     * Comunica con il GameRepository per rendere persistente il cambio di avatar.
     */
    @FXML
    private void handleConfirmClick(ActionEvent event) {
        ToggleButton selected = (ToggleButton) picToggleGroup.getSelectedToggle();
        if (selected != null) {
            String path = (String) selected.getUserData();

            // 1. Aggiorna il modello (Aspetto)
            player.setAvatarByPath(path);

            // 2. Salva permanentemente nel JSON
            GameRepository.getInstance().saveGameToJSON();

            MusicManager.getInstance().playSoundEffect("change_screen.mp3");
            closeWindow();
        }
    }

    private void setupButtons() {
        String iconsPath = "/org/example/ProgettoUIDFinal/imagini/profile/Icons/";
        String bannersPath = "/org/example/ProgettoUIDFinal/imagini/profile/banners/";

        // Foto Profilo
        ToggleButton[] pics = {pic1, pic2, pic3, pic4};
        String[] picFiles = {"chr_icon_1052.png", "chr_icon_1007.png", "chr_icon_1025.png", "chr_icon_1006.png"};
        for (int i = 0; i < pics.length; i++) {
            pics[i].setToggleGroup(picToggleGroup);
            pics[i].setUserData(iconsPath + picFiles[i]);
        }

        // Banner
        ToggleButton[] banners = {banner1, banner2, banner3, banner4};
        String[] bannerFiles = {"Banner1.png", "Banner2.png", "Banner3.jpg", "Banner4.png"};
        for (int i = 0; i < banners.length; i++) {
            banners[i].setToggleGroup(bannerToggleGroup);
            banners[i].setUserData(bannersPath + bannerFiles[i]);
        }
    }

    /**
     * Cerca il toggle corrispondente al path.
     * SE NON LO TROVA -> Seleziona il primo della lista.
     */
    private void selectToggleOrDefault(ToggleGroup group, String pathToCheck) {
        if (group.getToggles().isEmpty()) return;

        // Tenta di trovare la corrispondenza
        if (pathToCheck != null && !pathToCheck.isEmpty()) {
            for (Toggle t : group.getToggles()) {
                String btnPath = (String) t.getUserData();
                // endsWith è utile se nel JSON hai solo il nome file e nel bottone il percorso intero
                if (btnPath != null && btnPath.endsWith(pathToCheck)) {
                    group.selectToggle(t);
                    return; // Trovato, fine.
                }
            }
        }

        // Se siamo qui, non ha trovato nulla (o il path era vuoto).
        // Seleziona il PRIMO di default.
        group.selectToggle(group.getToggles().get(0));
    }

    /**
     * INIEZIONE DATI (Dependency Injection): Riceve i riferimenti dal controller chiamante.
     * Imposta lo stato iniziale dei ToggleButton in base alle preferenze attualmente salvate.
     *
     * @param mainController Il controller del profilo per inviare aggiornamenti
     * @param mainContentPane Il pannello da "sbloccare" alla chiusura
     * @param currentBannerUrl L'URL del banner attualmente in uso
     */
    public void initData(profileController mainController, GridPane mainContentPane, String currentBannerUrl) {
        this.mainController = mainController;
        this.blurredPane = mainContentPane;

        selectToggleOrDefault(picToggleGroup, player.getAvatarPath());
        selectToggleOrDefault(bannerToggleGroup, player.getBannerPath());
    }

    /**
     * GESTIONE CONFERMA BANNER: Aggiorna l'estetica del profilo e salva la scelta.
     * Utilizza le API Preferences per la persistenza locale del tema scelto.
     */
    @FXML
    private void handleConfirmBannerClick(ActionEvent event) {
        ToggleButton selected = (ToggleButton) bannerToggleGroup.getSelectedToggle();
        if (selected != null) {
            String path = (String) selected.getUserData();

            // 1. Aggiorna il banner nel modello (necessario aggiungere il metodo nel PlayerModel)
            player.setBannerPath(path);

            // 2. Aggiorna subito la grafica del profilo principale se è aperto
            if (mainController != null) mainController.updateBannerPicture(path);

            // 3. Salva nel JSON
            GameRepository.getInstance().saveGameToJSON();

            MusicManager.getInstance().playSoundEffect("change_screen.mp3");
            closeWindow();
        }
    }

    /**
     * Metodo helper per impedire la deselezione di un ToggleGroup.
     * Se l'utente clicca sul bottone già selezionato, questo rimane attivo.
     */
    private void addPreventDeselectionListener(ToggleGroup group) {
        group.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                oldVal.setSelected(true);
            }
        });
    }

    @FXML private void showProfilePane() { profileImage.setVisible(true); bannerImage.setVisible(false); }
    @FXML private void showBannerPane() { profileImage.setVisible(false); bannerImage.setVisible(true); }

    /**
     * LOGICA DI CHIUSURA (Overlay Reset): Rimuove la modale e ripristina la UI sottostante.
     * Rimuove l'effetto sfocatura e riabilita l'interazione con il pannello principale.
     */

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