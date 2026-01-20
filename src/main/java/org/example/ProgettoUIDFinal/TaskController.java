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
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;
import org.example.ProgettoUIDFinal.Services.BackgroundService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.example.ProgettoUIDFinal.Services.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;
import org.example.ProgettoUIDFinal.model.QuestModel;

/**
 * TaskController gestisce la logica delle Quest (missioni) e dei Daily Tasks (obiettivi giornalieri).
 * Si occupa dell'aggiornamento della UI, della progressione del giocatore (XP e Oro)
 * e della navigazione tra le diverse schermate di gioco.
 */
public class TaskController {

    // Riferimenti agli elementi UI definiti nel file FXML
    @FXML private AnchorPane mainContainer;
    @FXML private Label levelLabel;
    @FXML private ImageView backgroundImageView;
    @FXML private Button backButton;
    @FXML private Button dailyTasksButton;
    @FXML private Label moneyLabel;
    @FXML private Label playerName;
    @FXML private ImageView profilePicImageView;
    @FXML private ProgressBar xpBar;

    // UI specifica per la gestione delle Quest persistenti
    @FXML private VBox questListVBox;
    @FXML private Label detailTitleLabel;
    @FXML private Label detailDescLabel;
    @FXML private ImageView detailDiffIcon;
    private QuestModel questSelezionataCorrente;

    // UI specifica per i Daily Tasks (completamento tramite checkbox e icone/bandiere)
    @FXML private ImageView flag1, flag2, flag3, flag4, flag5;
    @FXML private CheckBox task1, task2, task3, task4, task5;

    private PlayerModel player;
    private Scene homeScene;
    private ColorAdjust verdeEffect; // Effetto visivo per indicare il completamento

    // Coordinate fisse per il posizionamento dei nodi caricati dinamicamente
    private final double FIXED_X = 142.0;
    private final double FIXED_Y = 65.0;

    /**
     * Helper per raggruppare le ImageView delle bandiere dei task giornalieri.
     */
    private List<ImageView> tutteLeFlag() {
        if (flag1 == null) return new ArrayList<>();
        return List.of(flag1, flag2, flag3, flag4, flag5);
    }

    /**
     * Helper per raggruppare le CheckBox dei task giornalieri.
     */
    private List<CheckBox> tuttiIBottoniDelleTask() {
        if (task1 == null) return new ArrayList<>();
        return List.of(task1, task2, task3, task4, task5);
    }

    /**
     * Metodo di inizializzazione richiamato automaticamente da JavaFX dopo il caricamento dell'FXML.
     */
    @FXML
    private void initialize() {
        // Recupera i dati del giocatore dal repository centrale
        player = GameRepository.getInstance().getPlayer();

        // Configura l'effetto cromatico verde per i task completati
        verdeEffect = new ColorAdjust();
        verdeEffect.setHue(0.6);
        verdeEffect.setSaturation(1.0);
        verdeEffect.setBrightness(0.3);

        // Ripristina lo stato grafico dei task giornalieri (se già completati in precedenza)
        initializeTaskStates();

        // Applicazione dello stile CSS e dei temi personalizzati
        if (mainContainer != null && getClass().getResource("style.css") != null) {
            String css = this.getClass().getResource("style.css").toExternalForm();
            mainContainer.getStylesheets().add(css);
            applyStylesToAllNodes(mainContainer);
        }

        // BINDING: Collega le proprietà del modello Player direttamente ai Label/ProgressBar della UI
        if (playerName != null) playerName.textProperty().bind(player.playerNameProperty());
        if (levelLabel != null) levelLabel.textProperty().bind(player.levelProperty().asString());
        if (moneyLabel != null) moneyLabel.textProperty().bind(player.goldProperty().asString());
        if (profilePicImageView != null) profilePicImageView.imageProperty().bind(player.avatarImageProperty());

        if (xpBar != null) {
            final double MAX_XP = 100.0;
            // La barra progredisce in base alla percentuale (XP attuale / MAX_XP)
            xpBar.progressProperty().bind(player.xpProperty().divide(MAX_XP));
        }

        // Gestione dinamica dello sfondo
        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null && backgroundImageView != null) {
            applyBackground(backgroundImageView, currentBg);
        }

        // Listener per aggiornare lo sfondo in tempo reale se cambia globalmente
        BackgroundService.getInstance().backgroundProperty().addListener((obs, oldImg, newImg) -> {
            if (newImg != null) applyBackground(backgroundImageView, newImg);
        });

        if (backButton != null) backButton.setCancelButton(true);

        // Carica la lista delle quest se ci troviamo nella schermata dedicata
        loadQuestsFromRepo();
    }


    // GESTIONE QUEST PERSISTENTI (SISTEMA A ELENCO)

    /**
     * Carica le quest salvate nel GameRepository e le visualizza nella VBox.
     */
    private void loadQuestsFromRepo() {
        if (questListVBox == null) return;

        questListVBox.getChildren().clear();
        List<QuestModel> quests = GameRepository.getInstance().getQuests();
        if (quests == null) quests = new ArrayList<>();

        for (QuestModel q : quests) {
            questListVBox.getChildren().add(createQuestButton(q));
        }

        // Seleziona automaticamente la prima quest se presente
        if (!quests.isEmpty()) {
            showQuestDetails(quests.get(0));
        } else {
            clearQuestDetailsUI();
        }
    }

    /**
     * Aggiunge una nuova quest al sistema, salvandola su file JSON e aggiornando la UI.
     */
    public void addNewQuest(String titolo, String descrizione, int difficolta) {
        QuestModel nuovaQuest = new QuestModel(titolo, descrizione, difficolta);

        // Persistenza dei dati
        GameRepository.getInstance().addQuest(nuovaQuest);
        GameRepository.getInstance().saveGameToJSON();

        // Aggiornamento grafico immediato
        if (questListVBox != null) {
            questListVBox.getChildren().add(createQuestButton(nuovaQuest));
            if (questListVBox.getChildren().size() == 1) showQuestDetails(nuovaQuest);
        }
    }

    /**
     * Crea dinamicamente un bottone per la lista delle quest.
     */
    private Button createQuestButton(QuestModel quest) {
        Button btn = new Button(quest.getTitle());
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(45);
        btn.setAlignment(Pos.CENTER_LEFT);
        // Stile inline
        btn.setStyle("-fx-background-color: #5D4037; -fx-text-fill: white; -fx-border-color: #3E2723; -fx-border-width: 2; -fx-padding: 0 0 0 10;");
        btn.setUserData(quest); // Memorizza l'oggetto quest nel bottone per recuperarlo al click
        btn.setOnAction(event -> showQuestDetails(quest));
        return btn;
    }

    /**
     * Mostra i dettagli della quest selezionata nell'area di anteprima.
     */
    private void showQuestDetails(QuestModel quest) {
        if (quest == null || detailDiffIcon == null) return;

        this.questSelezionataCorrente = quest;
        if (detailTitleLabel != null) detailTitleLabel.setText(quest.getTitle());
        if (detailDescLabel != null) detailDescLabel.setText(quest.getDescription());

        // Gestione dell'icona basata sul livello di difficoltà
        String nomeFile;
        switch (quest.getDifficulty()) {
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
            detailDiffIcon.setImage(img);
            detailDiffIcon.setVisible(true);
        } else {
            System.err.println("❌ URL non trovato per: " + imagePath);
        }
    }

    /**
     * Azione eseguita quando il giocatore clicca su "Conferma" (Quest completata).
     * Eroga ricompense scalate in base alla difficoltà.
     */
    @FXML
    private void confirmQuest() {
        if (questSelezionataCorrente == null) return;

        int difficolta = questSelezionataCorrente.getDifficulty();
        MusicManager.getInstance().playSoundEffect("xp_gain.mp3");

        // Calcolo ricompense
        switch (difficolta) {
            case 1 -> { player.increaseXp(15); player.setGold(player.getGold() + 100); }
            case 2 -> { player.increaseXp(20); player.setGold(player.getGold() + 150); }
            case 3 -> { player.increaseXp(30); player.setGold(player.getGold() + 250); }
            case 4 -> { player.increaseXp(50); player.setGold(player.getGold() + 500); }
        }
        player.setTaskCompleted(player.getTaskCompleted() + 1);

        // Rimozione della quest completata dal sistema
        GameRepository.getInstance().removeQuest(questSelezionataCorrente);
        GameRepository.getInstance().saveGameToJSON();

        // Rimozione del nodo grafico corrispondente
        if (questListVBox != null) {
            QuestModel toRemove = questSelezionataCorrente;
            questListVBox.getChildren().removeIf(node -> node.getUserData() == toRemove);
        }

        clearQuestDetailsUI();
    }

    /**
     * Elimina la quest selezionata senza erogare ricompense.
     */
    @FXML
    private void DeleteQuest() {
        if (questSelezionataCorrente == null) return;
        MusicManager.getInstance().playSoundEffect("no-funds.wav");

        GameRepository.getInstance().removeQuest(questSelezionataCorrente);
        GameRepository.getInstance().saveGameToJSON();

        if (questListVBox != null) {
            QuestModel toRemove = questSelezionataCorrente;
            questListVBox.getChildren().removeIf(node -> node.getUserData() == toRemove);
        }
        clearQuestDetailsUI();
    }


    // DAILY TASKS (SISTEMA CHECKBOX)

    /**
     * Sincronizza lo stato visivo dei task giornalieri con i dati salvati nel profilo player.
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
                    flag.setEffect(verdeEffect); // Applica l'effetto "completato"
                }
            }
        }
    }

    /**
     * Conferma i task giornalieri selezionati, eroga XP/Oro e aggiorna lo stato visivo.
     */
    @FXML
    private void confirmDaily() {
        List<CheckBox> tasks = tuttiIBottoniDelleTask();
        List<ImageView> flags = tutteLeFlag();
        int size = Math.min(tasks.size(), flags.size());

        for (int i = 0; i < size; i++) {
            CheckBox task = tasks.get(i);
            ImageView flag = flags.get(i);

            if (task != null && flag != null && task.isSelected() && !task.isDisabled()) {
                task.setDisable(true);
                MusicManager.getInstance().playSoundEffect("xp_gain.mp3");
                player.completeTask("task" + (i + 1)); // Salva lo stato nel modello
                player.increaseXp(20);
                player.setGold(player.getGold() + 150);
                flag.setEffect(verdeEffect);
                player.setTaskCompleted(player.getTaskCompleted() + 1);
            }
        }
    }


    // NAVIGAZIONE E UI HELPERS

    /**
     * Applica ricorsivamente lo stile personalizzato a tutti i componenti della UI.
     */
    private void applyStylesToAllNodes(javafx.scene.Node node) {
        if (node instanceof Region) StyleManager.getInstance().applyStyle((Region) node);
        if (node instanceof Parent) {
            for (javafx.scene.Node child : ((Parent) node).getChildrenUnmodifiable()) {
                applyStylesToAllNodes(child);
            }
        }
    }

    /**
     * Carica dinamicamente il file dailytasks.fxml all'interno del contenitore principale.
     */
    @FXML
    private void showDailyTasks() {
        if (mainContainer == null) return;

        // Se è già aperto, lo chiude (toggle)
        Node currentGridNode = mainContainer.lookup("#tasksGrid");
        if (currentGridNode != null && "daily".equals(currentGridNode.getUserData())) {
            ((Pane) currentGridNode.getParent()).getChildren().remove(currentGridNode);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dailytasks.fxml"));
            Parent dailyTasksView = loader.load();

            GridPane dailyTasksGrid = (GridPane) dailyTasksView.lookup("#tasksGrid");
            dailyTasksGrid.setUserData("daily");
            dailyTasksGrid.setLayoutX(FIXED_X);
            dailyTasksGrid.setLayoutY(FIXED_Y);

            applyStylesToAllNodes(dailyTasksGrid);
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");

            // Configura il controller della nuova vista
            TaskController dailyController = loader.getController();
            dailyController.setBackButtonVisible(true);
            dailyController.setDailyTasksButtonVisible(false);
            dailyController.setHomeScene(homeScene);

            mainContainer.getChildren().add(dailyTasksGrid);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Carica dinamicamente la vista Quests.fxml (le missioni principali).
     */
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
                tasksController.loadQuestsFromRepo(); // Fondamentale per vedere le quest salvate

                // Sostituisce la grid corrente se presente
                GridPane currentGrid = (GridPane) mainContainer.lookup("#tasksGrid");
                if (currentGrid != null) {
                    ((Pane) currentGrid.getParent()).getChildren().remove(currentGrid);
                }
                mainContainer.getChildren().add(tasksGrid);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Torna alla scena Home del gioco.
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

    /**
     * Apre una finestra popup per la creazione di una nuova quest.
     */
    @FXML
    public void showQuestAdd(ActionEvent event) throws IOException {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AddQuest.fxml"));
        Parent root = loader.load();

        AddQuestController controller = loader.getController();
        controller.setParentController(this); // Permette al popup di comunicare con questo controller

        Scene addQuestScene = new Scene(root);
        Stage newStage = new Stage();
        newStage.setTitle("Aggiungi Nuova Quest");
        newStage.setScene(addQuestScene);
        newStage.show();
    }

    // Metodi setter per configurazione esterna
    public void setHomeScene(Scene scene) { this.homeScene = scene; }
    public void setBackButtonVisible(boolean visible) { if (backButton != null) backButton.setVisible(visible); }
    public void setDailyTasksButtonVisible(boolean visible) { if (dailyTasksButton != null) dailyTasksButton.setVisible(visible); }
    private void applyBackground(ImageView imageView, Image image) { if (imageView != null && image != null) imageView.setImage(image); }

    @FXML private void showSettings() { System.out.println("Settings button clicked"); }
    private void clearQuestDetailsUI() {
        questSelezionataCorrente = null;
        if (detailTitleLabel != null) detailTitleLabel.setText("Seleziona una quest");
        if (detailDescLabel != null) detailDescLabel.setText("");
        if (detailDiffIcon != null) detailDiffIcon.setImage(null);
    }
}