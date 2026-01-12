package org.example.ProgettoUIDFinal;

import javafx.animation.PauseTransition;
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

import org.example.ProgettoUIDFinal.Services.BackgroundService;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;
import org.example.ProgettoUIDFinal.model.QuestModel;

/**
 * SERVICE CONTROLLER - MISSION SYSTEM: Gestisce il registro delle attività (Tasks e Quests).
 * Implementa una logica duale per la gestione di task statiche (Daily) e
 * istanze dinamiche (User-defined Quests) basate sul pattern model-driven.
 */
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

    // --- DINAMIC QUEST LISTING COMPONENTS ---
    @FXML private VBox questListVBox;
    @FXML private Label detailTitleLabel;
    @FXML private Label detailDescLabel;
    @FXML private ImageView detailDiffIcon;
    private QuestModel questSelezionataCorrente;

    @FXML private ImageView flag1, flag2, flag3, flag4, flag5;
    @FXML private CheckBox task1, task2, task3, task4, task5;

    private PlayerModel player;
    private Scene homeScene;
    private ColorAdjust verdeEffect;

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

    /**
     * LIFE-CYCLE INITIALIZE: Esegue il bootstrapping del modulo missioni.
     * Configura il Data Binding tra il PlayerModel e la UI e inizializza i filtri grafici.
     */
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

    /**
     * DYNAMIC NODE FACTORY: Genera e inietta un nuovo componente Quest nella UI.
     * Utilizza il campo UserData del nodo Button per la persistenza del QuestModel associato.
     */
    public void aggiungiNuovaQuest(String titolo, String descrizione, int difficolta) {
        QuestModel nuovaQuest = new QuestModel(titolo, descrizione, difficolta);

        Button btn = new Button(titolo);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.setAlignment(Pos.CENTER_LEFT);

        btn.setStyle("-fx-background-color: #5D4037; -fx-text-fill: white; -fx-border-color: #3E2723; -fx-border-width: 2; -fx-padding: 0 0 0 10;");

        btn.setUserData(nuovaQuest);

        btn.setOnAction(event -> {
            if (btn.getUserData() instanceof QuestModel) {
                QuestModel q = (QuestModel) btn.getUserData();
                aggiornaDettagliDestra(q);
            }
        });

        if (questListVBox != null) {
            questListVBox.getChildren().add(btn);

            if (questListVBox.getChildren().size() == 1) {
                aggiornaDettagliDestra(nuovaQuest);
            }
        }
    }

    /**
     * UI SYNC - DETAIL VIEW: Aggiorna i componenti della colonna dettagli.
     */
    private void aggiornaDettagliDestra(QuestModel quest) {
        if (detailDiffIcon == null) return;
        this.questSelezionataCorrente = quest;

        if (detailTitleLabel != null) detailTitleLabel.setText(quest.getTitolo());
        if (detailDescLabel != null) detailDescLabel.setText(quest.getDescrizione());

        String nomeFile;
        switch (quest.getDifficolta()) {
            case 1: nomeFile = "Easy.png"; break;
            case 2: nomeFile = "Normal.png"; break;
            case 3: nomeFile = "hard.png"; break;
            case 4: nomeFile = "impossible.png"; break;
            default: nomeFile = "debug.png";
        }

        String imagePath = "/org/example/ProgettoUIDFinal/imagini/Task/Difficulties/" + nomeFile;
        URL url = getClass().getResource(imagePath);
        if (url != null) {
            Image img = new Image(url.toExternalForm(), false);

            if (img.isError()) {
                System.err.println("ERRORE CARICAMENTO IMMAGINE: " + img.getException());
            }

            detailDiffIcon.setImage(img);

            detailDiffIcon.setFitWidth(100);
            detailDiffIcon.setFitHeight(100);
            detailDiffIcon.setPreserveRatio(true);
            detailDiffIcon.setVisible(true);

        } else {
            System.err.println("URL non trovato per: " + imagePath);
        }
    }

    /**
     * STATE RESTORE: Ripristina lo stato delle Daily Tasks consultando il PlayerModel.
     */
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

    /**
     * TASK COMPLETION LOGIC: Elabora il completamento delle Daily Tasks attive.
     */
    @FXML
    private void confermaAzione() {
        List<CheckBox> tasks = tuttiIBottoniDelleTask();
        List<ImageView> flags = tutteLeFlag();

        int size = Math.min(tasks.size(), flags.size());

        for (int i = 0; i < size; i++) {
            CheckBox task = tasks.get(i);
            ImageView flag = flags.get(i);

            if (task != null && flag != null) {
                if (task.isSelected() && !task.isDisabled()) {
                    task.setDisable(true);
                    MusicManager.getInstance().playSoundEffect("xp_gain.mp3");
                    player.completeTask("task" + (i + 1));
                    player.increaseXp(20);
                    player.setGold(player.getGold() + 150);
                    flag.setEffect(verdeEffect);
                    player.setTaskCompleted(player.getTaskCompleted() + 1);
                }
            }
        }
    }

    /**
     * REWARD CALCULATION: Valida il completamento di una Quest dinamica.
     */
    @FXML private void ConfermaQuest() {
        if (questSelezionataCorrente == null) {
            return;
        }

        int difficolta = questSelezionataCorrente.getDifficolta();
        MusicManager.getInstance().playSoundEffect("xp_gain.mp3");

        switch (difficolta) {
            case 1:
                player.increaseXp(15);
                player.setGold(player.getGold() + 100);
                break;
            case 2:
                player.increaseXp(20);
                player.setGold(player.getGold() + 150);
                break;
            case 3:
                player.increaseXp(30);
                player.setGold(player.getGold() + 250);
                break;
            case 4:
                player.increaseXp(50);
                player.setGold(player.getGold() + 500);
                break;
            default:
                break;
        }

        questListVBox.getChildren().removeIf(node -> node.getUserData() == questSelezionataCorrente);

        resetDetailPane();
    }

    /**
     * INSTANCE DELETION: Rimuove una Quest senza erogazione di ricompense.
     */
    @FXML private void DeleteQuest(){
        MusicManager.getInstance().playSoundEffect("no-funds.wav");
        questListVBox.getChildren().removeIf(node -> node.getUserData() == questSelezionataCorrente);
        resetDetailPane();
    }

    /**
     * UI RESET: Ripristina lo stato neutro della colonna dettagli.
     */
    private void resetDetailPane() {
        questSelezionataCorrente = null;
        if(detailTitleLabel != null) detailTitleLabel.setText("Seleziona una quest");
        if(detailDescLabel != null) detailDescLabel.setText("");
        if(detailDiffIcon != null) detailDiffIcon.setImage(null);
    }

    /**
     * RECURSIVE STYLE APPLICATION: Propaga lo stile grafico a tutti i nodi.
     */
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

    /**
     * UI ROUTING: Carica e visualizza il sotto-modulo delle Daily Tasks.
     */
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
        }
    }

    /**
     * UI ROUTING: Ripristina la visualizzazione del modulo principale Quest.
     */
    @FXML
    private void showMainTasks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Tasks.fxml"));
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
    }

    /**
     * BACK NAVIGATION: Esegue il ripristino della scena Home.
     */
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

    /**
     * EXTERNAL STAGE OPENING: Avvia il modulo per l'inserimento di nuove Quest.
     */
    @FXML
    public void showQuestAdd(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AddQuest.fxml"));
        Parent root = loader.load();

        AddQuestController controller = loader.getController();
        controller.setParentController(this);

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