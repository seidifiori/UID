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

/**
 * CONTROLLER SELETTORE PROFILO: Gestisce l'interfaccia di scelta per l'avatar e il banner.
 * Funziona come una finestra modale sovrapposta (Overlay) che interagisce con il profileController
 * per aggiornare l'aspetto estetico del giocatore.
 */
public class profilePicChooserController {

    // --- ELEMENTI UI (Iniezione FXML) ---
    @FXML private Button confirmPic, confirmBanner;

    // Gruppo di scelta esclusiva per l'icona del profilo (Avatar)
    @FXML private ToggleButton pic1, pic2, pic3, pic4;
    private final ToggleGroup toggleGroup = new ToggleGroup();

    // Gruppo di scelta esclusiva per il banner di sfondo
    @FXML private ToggleButton banner1, banner2, banner3, banner4;
    private final ToggleGroup toggleBannerGroup = new ToggleGroup();

    @FXML private StackPane picChooserPane;
    @FXML private ScrollPane bannerScrollPane;

    // Riferimenti per la gestione dell'effetto sfocatura e callback
    private GridPane blurredPane;
    private profileController mainController;

    /**
     * INIZIALIZZAZIONE: Configura i ToggleGroup e associa i percorsi delle risorse.
     * Utilizza il campo 'UserData' dei bottoni per memorizzare i percorsi dei file (String)
     * direttamente all'interno dei componenti grafici.
     */
    @FXML
    public void initialize() {
        // Percorsi assoluti delle icone avatar (Resources Path)
        String basePath = "/org/example/ProgettoUIDFinal/imagini/Icons/";

        String[] hardcodedPaths = {
                basePath + "chr_icon_1052.png",
                basePath + "chr_icon_1007.png",
                basePath + "chr_icon_1025.png",
                basePath + "chr_icon_1053.png"
        };

        // Mapping dinamico dei bottoni: associa ogni ToggleButton al suo percorso immagine
        for (int i = 0; i < hardcodedPaths.length; i++) {
            int buttonIndex = i + 1;
            String buttonId = "#pic" + buttonIndex;

            // Lookup del componente nel grafo dei nodi tramite ID
            ToggleButton btn = (ToggleButton) picChooserPane.lookup(buttonId);

            if (btn != null) {
                btn.setToggleGroup(toggleGroup);
                // Iniezione del percorso nel metadato UserData
                btn.setUserData(hardcodedPaths[i]);
            }
        }

        // Configurazione Gruppo Banner
        banner1.setToggleGroup(toggleBannerGroup);
        banner2.setToggleGroup(toggleBannerGroup);
        banner3.setToggleGroup(toggleBannerGroup);
        banner4.setToggleGroup(toggleBannerGroup);

        // Mappatura UserData per i banner (usa il prefisso @ per logica interna di parsing)
        banner1.setUserData("@imagini/profile/banners/Banner1.png");
        banner2.setUserData("@imagini/profile/banners/Banner2.png");
        banner3.setUserData("@imagini/profile/banners/Banner3.jpg");
        banner4.setUserData("@imagini/profile/banners/Banner4.png");
    }

    /**
     * GESTIONE CONFERMA AVATAR: Recupera la scelta effettuata e aggiorna il modello globale.
     * Comunica con il GameRepository per rendere persistente il cambio di avatar.
     */
    @FXML
    private void handleConfirmClick(ActionEvent event) {
        ToggleButton selected = (ToggleButton) toggleGroup.getSelectedToggle();

        if (selected != null) {
            // Estrazione del percorso risorsa dal metadato del bottone
            String fullPath = (String) selected.getUserData();
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");

            if (fullPath != null) {
                // Aggiornamento del PlayerModel tramite il Repository (Pattern Facade)
                GameRepository.getInstance().changePlayerAvatar(fullPath);
            }
        }
        closeWindow();
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

        // Recupero preferenze salvate per l'avatar
        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);
        String defaultKey = "profile.pic1";
        String defaultPath = GameRepository.getInstance().getAvatarPathByKey(defaultKey);
        String savedPath = prefs.get("saved.avatar.path", defaultPath);

        // Sincronizzazione UI: seleziona il bottone corrispondente all'avatar salvato
        for (Toggle toggle : toggleGroup.getToggles()) {
            ToggleButton button = (ToggleButton) toggle;
            String buttonUrl = (String) button.getUserData();

            if (buttonUrl != null && buttonUrl.equals(savedPath)) {
                toggleGroup.selectToggle(toggle);
                break;
            }
        }

        // Sincronizzazione UI: seleziona il bottone corrispondente al banner salvato
        for (Toggle toggle : toggleBannerGroup.getToggles()) {
            ToggleButton button = (ToggleButton) toggle;
            String buttonUrl = (String) button.getUserData();

            if (buttonUrl != null && buttonUrl.equals(currentBannerUrl)) {
                button.setSelected(true);
                break;
            }
        }

        // Event Filter per la gestione dello scroll: disabilita lo scroll orizzontale non voluto
        bannerScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaX() != 0) event.consume();
        });
    }

    /**
     * GESTIONE CONFERMA BANNER: Aggiorna l'estetica del profilo e salva la scelta.
     * Utilizza le API Preferences per la persistenza locale del tema scelto.
     */
    @FXML
    private void handleConfirmBannerClick(ActionEvent event) {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        ToggleButton selected = (ToggleButton) toggleBannerGroup.getSelectedToggle();

        if (selected != null) {
            String imageUrl = (String) selected.getUserData();
            // Callback al controller principale per aggiornamento grafico immediato
            mainController.updateBannerPicture(imageUrl);

            // Scrittura della preferenza nel registro di sistema
            Preferences prefs = Preferences.userNodeForPackage(profileController.class);
            prefs.put("banner_url", imageUrl);
        }
        closeWindow();
    }

    /**
     * LOGICA DI CHIUSURA (Overlay Reset): Rimuove la modale e ripristina la UI sottostante.
     * Rimuove l'effetto sfocatura e riabilita l'interazione con il pannello principale.
     */
    private void closeWindow() {
        if (blurredPane != null) {
            blurredPane.setEffect(null);
            blurredPane.setDisable(false);
        }
        // Rozione dinamica del nodo dal contenitore padre (StackPane)
        if (picChooserPane != null && picChooserPane.getParent() != null) {
            ((StackPane) picChooserPane.getParent()).getChildren().remove(picChooserPane);
        }
    }
}