package org.example.ProgettoUIDFinal;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;
// Assicurati di importare la tua classe model corretta
import org.example.ProgettoUIDFinal.model.QuestModel;

public class TaskController {

    @FXML private AnchorPane mainContainer;
    @FXML private Label levelLabel;
    @FXML private ImageView backgroundImageView;
    @FXML private Button backButton;
    @FXML private Button dailyTasksButton;
    @FXML private Label moneyLabel;
    @FXML private Label playerName;
    @FXML private ImageView profilePicImageView;
    @FXML private ProgressBar xpBar;

    // --- NUOVI RIFERIMENTI PER LA QUEST LOGIC ---
    @FXML private VBox questListVBox;      // Colonna sinistra (lista)
    @FXML private Label detailTitleLabel;  // Colonna destra (Titolo)
    @FXML private Label detailDescLabel;   // Colonna destra (Descrizione)
    @FXML private ImageView detailDiffIcon;// Colonna destra (Faccia difficoltà)
    // --------------------------------------------

    @FXML private ImageView flag1, flag2, flag3, flag4, flag5;
    @FXML private CheckBox task1, task2, task3, task4, task5;

    private PlayerModel player;
    private Scene homeScene;
    private ColorAdjust verdeEffect;

    // COORDINATE FISSE DAL TUO FXML
    private final double FIXED_X = 142.0;
    private final double FIXED_Y = 65.0;

    private List<ImageView> tutteLeFlag() {
        if (flag1 == null) return new ArrayList<>();
        return List.of(flag1, flag2, flag3, flag4, flag5);
    }

    private List<CheckBox> tuttiIBottoniDelleTask() {
        if (task1 == null) return new ArrayList<>();
        return List.of(task1, task2, task3, task4, task5);
    }

    @FXML
    private void initialize() {
        player = GameRepository.getInstance().getPlayer();

        verdeEffect = new ColorAdjust();
        verdeEffect.setHue(0.6);
        verdeEffect.setSaturation(1.0);
        verdeEffect.setBrightness(0.3);

        initializeTaskStates();

        if (mainContainer != null && getClass().getResource("style.css") != null) {
            String css = this.getClass().getResource("style.css").toExternalForm();
            mainContainer.getStylesheets().add(css);
            applyStylesToAllNodes(mainContainer);
        }

        if (playerName != null) {
            playerName.textProperty().bind(player.playerNameProperty());
        }
        if(levelLabel != null){
            levelLabel.textProperty().bind(player.levelProperty().asString());
        }
        if (moneyLabel != null) {
            moneyLabel.textProperty().bind(player.goldProperty().asString());
        }
        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }

        if (xpBar != null) {
            final double MAX_XP = 100.0;
            xpBar.progressProperty().bind(player.xpProperty().divide(MAX_XP));
        }

        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null && backgroundImageView != null) {
            applyBackground(backgroundImageView, currentBg);
        }

        BackgroundService.getInstance().backgroundProperty().addListener((obs, oldImg, newImg) -> {
            if (newImg != null) {
                applyBackground(backgroundImageView, newImg);
            }
        });
    }

    // =========================================================
    //       NUOVA LOGICA PER AGGIUNGERE QUEST E DETTAGLI
    // =========================================================

    /**
     * Chiamato da AddQuestController quando l'utente conferma.
     */
    public void aggiungiNuovaQuest(String titolo, String descrizione, int difficolta) {
        // 1. Crea l'oggetto QuestModel (Corretto l'errore di sintassi qui)
        QuestModel nuovaQuest = new QuestModel(titolo, descrizione, difficolta);

        // 2. Crea il bottone per la lista a sinistra
        Button btn = new Button(titolo);
        btn.setMaxWidth(Double.MAX_VALUE); // Occupa tutta la larghezza
        btn.setPrefHeight(45);
        btn.setAlignment(Pos.CENTER_LEFT); // Testo allineato a sinistra

        // Stile inline per il bottone
        btn.setStyle("-fx-background-color: #5D4037; -fx-text-fill: white; -fx-border-color: #3E2723; -fx-border-width: 2; -fx-padding: 0 0 0 10;");

        // 3. Salva i dati completi DENTRO il bottone
        btn.setUserData(nuovaQuest);

        // 4. Azione al click: aggiorna la parte destra
        btn.setOnAction(event -> {
            // CAST CORRETTO: Dev'essere QuestModel, non Quest
            if (btn.getUserData() instanceof QuestModel) {
                QuestModel q = (QuestModel) btn.getUserData();
                aggiornaDettagliDestra(q);
            }
        });

        // 5. Aggiungi alla VBox
        if (questListVBox != null) {
            questListVBox.getChildren().add(btn);

            // Opzionale: Se è la prima quest, selezionala subito visivamente
            if (questListVBox.getChildren().size() == 1) {
                aggiornaDettagliDestra(nuovaQuest);
            }
        }
    }

    private void aggiornaDettagliDestra(QuestModel quest) {
        System.out.println("DEBUG: Click rilevato! Provo ad aggiornare i dettagli...");

        // 1. Aggiorna Testi
        if (detailTitleLabel != null) {
            detailTitleLabel.setText(quest.getTitolo());
        } else {
            System.err.println("ERRORE GRAVE: detailTitleLabel è NULL! Controlla fx:id nell'FXML.");
        }

        if (detailDescLabel != null) {
            detailDescLabel.setText(quest.getDescrizione());
        }

        // 2. Controllo se l'icona esiste nell'interfaccia
        if (detailDiffIcon == null) {
            System.err.println("ERRORE GRAVE: detailDiffIcon è NULL! L'immagine non può essere caricata perché manca il collegamento fx:id nell'FXML.");
            return; // Esco perché non posso fare nulla
        }

        // 3. Scelta del nome file ESATTO (Case Sensitive) basato sul tuo screenshot
        String nomeFile;
        switch (quest.getDifficolta()) {
            case 1: nomeFile = "Easy.png"; break;       // E maiuscola
            case 2: nomeFile = "Normal.png"; break;     // N maiuscola
            case 3: nomeFile = "Hard.png"; break;       // h minuscola
            case 4: nomeFile = "Impossible.png"; break; // i minuscola
            default: nomeFile = "debug.png";            // d minuscola
        }

        // 4. Costruzione percorso ASSOLUTO
        // path: resources/org/example/ProgettoUIDFinal/imagini/Task/Difficulties/
        String imagePath = "/org/example/ProgettoUIDFinal/imagini/Task/Difficulties/" + nomeFile;

        System.out.println("Cerco immagine in: " + imagePath);

        try {
            URL resource = getClass().getResource(imagePath);
            if (resource != null) {
                detailDiffIcon.setImage(new Image(resource.toExternalForm()));
                System.out.println("SUCCESSO: Immagine caricata!");
            } else {
                System.err.println("ERRORE: File non trovato nel percorso: " + imagePath);
                // Carica la debug come fallback se non trova quella specifica
                URL debugRes = getClass().getResource("/org/example/ProgettoUIDFinal/imagini/Task/Difficulties/debug.png");
                if(debugRes != null) detailDiffIcon.setImage(new Image(debugRes.toExternalForm()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================

    private void initializeTaskStates() {
        List<CheckBox> tasks = tuttiIBottoniDelleTask();
        List<ImageView> flags = tutteLeFlag();

        int size = Math.min(tasks.size(), flags.size());

        for (int i = 0; i < size; i++) {
            String taskId = "task" + (i + 1);
            if (player.isTaskCompleted(taskId)) {
                CheckBox task = tasks.get(i);
                ImageView flag = flags.get(i);
                if (task != null && flag != null) {
                    task.setDisable(true);
                    task.setSelected(true);
                    flag.setEffect(verdeEffect);
                }
            }
        }
    }

    @FXML
    private void confermaAzione() {
        MusicManager.getInstance().playSoundEffect("xp_gain.mp3");
        List<CheckBox> tasks = tuttiIBottoniDelleTask();
        List<ImageView> flags = tutteLeFlag();

        int size = Math.min(tasks.size(), flags.size());

        for (int i = 0; i < size; i++) {
            CheckBox task = tasks.get(i);
            ImageView flag = flags.get(i);

            if (task != null && flag != null) {
                if (task.isSelected() && !task.isDisabled()) {
                    task.setDisable(true);
                    player.completeTask("task" + (i + 1));
                    player.increaseXp(20);
                    player.setGold(player.getGold() + 150);
                    flag.setEffect(verdeEffect);
                    System.out.println("Task " + (i + 1) + " completata!");
                }
            }
        }
    }

    private void applyStylesToAllNodes(javafx.scene.Node node) {
        if (node instanceof Region) {
            StyleManager.getInstance().applyStyle((Region) node);
        }

        if (node instanceof Parent) {
            for (javafx.scene.Node child : ((Parent) node).getChildrenUnmodifiable()) {
                applyStylesToAllNodes(child);
            }
        }
    }

    @FXML
    private void showDailyTasks() {
        if (mainContainer == null) return;

        Node currentGridNode = mainContainer.lookup("#tasksGrid");

        if (currentGridNode != null && "daily".equals(currentGridNode.getUserData())) {
            Pane parent = (Pane) currentGridNode.getParent();
            if (parent != null) {
                parent.getChildren().remove(currentGridNode);
            }
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dailytasks.fxml"));
            Parent dailyTasksView = loader.load();

            if (getClass().getResource("style.css") != null) {
                String css = this.getClass().getResource("style.css").toExternalForm();
                dailyTasksView.getStylesheets().add(css);
            }

            GridPane dailyTasksGrid = (GridPane) dailyTasksView.lookup("#tasksGrid");
            dailyTasksGrid.setUserData("daily");

            dailyTasksGrid.setLayoutX(FIXED_X);
            dailyTasksGrid.setLayoutY(FIXED_Y);

            applyStylesToAllNodes(dailyTasksGrid);

            MusicManager.getInstance().playSoundEffect("change_screen.mp3");

            TaskController dailyController = loader.getController();
            dailyController.setBackButtonVisible(true);
            dailyController.setDailyTasksButtonVisible(false);
            dailyController.setHomeScene(homeScene);

            if (currentGridNode != null) {
                Pane parent = (Pane) currentGridNode.getParent();
                if (parent != null) {
                    parent.getChildren().remove(currentGridNode);
                    parent.getChildren().add(dailyTasksGrid);
                }
            } else {
                mainContainer.getChildren().add(dailyTasksGrid);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento di dailytasks.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void showMainTasks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Quests.fxml"));
            Parent tasksView = loader.load();

            applyStylesToAllNodes(tasksView);

            GridPane tasksGrid = (GridPane) tasksView.lookup("#tasksGrid");
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");


            if (tasksGrid != null) {
                tasksGrid.setLayoutX(FIXED_X);
                tasksGrid.setLayoutY(FIXED_Y);

                TaskController tasksController = loader.getController();
                tasksController.setBackButtonVisible(true);
                tasksController.setDailyTasksButtonVisible(true);
                tasksController.setHomeScene(homeScene);

                if (mainContainer != null) {
                    GridPane currentGrid = (GridPane) mainContainer.lookup("#tasksGrid");
                    if (currentGrid != null) {
                        Pane parent = (Pane) currentGrid.getParent();
                        if (parent != null) {
                            parent.getChildren().remove(currentGrid);
                            parent.getChildren().add(tasksGrid);
                        }
                    } else {
                        mainContainer.getChildren().add(tasksGrid);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void setHomeScene(Scene scene) { this.homeScene = scene; }

    @FXML
    private void showSettings() {
        System.out.println("Settings button clicked");
    }

    @FXML
    public void Home() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        MusicManager.getInstance().playMusic("background_music.mp3");

        if (homeScene != null && mainContainer != null && mainContainer.getScene() != null) {
            Stage currentStage = (Stage) mainContainer.getScene().getWindow();
            currentStage.setScene(homeScene);
        }
    }

    public void setBackButtonVisible(boolean visible) {
        if (backButton != null) {
            backButton.setVisible(visible);
        }
    }

    public void setDailyTasksButtonVisible(boolean visible) {
        if (dailyTasksButton != null) {
            dailyTasksButton.setVisible(visible);
        }
    }

    private void applyBackground(ImageView imageView, Image image) {
        if (imageView != null && image != null) {
            imageView.setImage(image);
        }
    }

    @FXML
    public void showQuestAdd(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AddQuest.fxml"));
        Parent root = loader.load();

        // --- COLLEGAMENTO TRA LE FINESTRE ---
        AddQuestController controller = loader.getController();
        controller.setParentController(this); // Passiamo questo controller al figlio

        Scene addQuestScene = new Scene(root);
        controller.setHomeScene(addQuestScene);

        if (root instanceof Region) {
            StyleManager.getInstance().applyStyle((Region) root);
        }
        Stage newStage = new Stage();
        newStage.setTitle("Aggiungi Nuova Quest");
        newStage.setScene(addQuestScene);

        newStage.show();
    }
}