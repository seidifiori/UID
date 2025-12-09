package org.example.ProgettoUIDFinal;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import org.example.ProgettoUIDFinal.model.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

public class TaskController {

    @FXML private AnchorPane mainContainer;
    @FXML private VBox tasksContainer;
    @FXML private ImageView backgroundImageView;
    @FXML private Button backButton;
    @FXML private Button dailyTasksButton;
    @FXML private Label moneyLabel;
    @FXML private Label playerName;
    @FXML private ImageView profilePicImageView;
    @FXML private ProgressBar xpBar;

    @FXML private ImageView flag1, flag2, flag3, flag4, flag5;
    @FXML private CheckBox task1, task2, task3, task4, task5;

    private Scene homeScene;

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
        PlayerModel player = GameRepository.getInstance().getPlayer();

        // --- FIX CRITICO QUI ---
        // Aggiungiamo il controllo "mainContainer != null" prima di toccarlo
        if (mainContainer != null && getClass().getResource("style.css") != null) {
            String css = this.getClass().getResource("style.css").toExternalForm();
            mainContainer.getStylesheets().add(css);
        }

        if (mainContainer != null) {
            applyStylesToAllNodes(mainContainer);
        }
        // -----------------------

        if (playerName != null) {
            playerName.textProperty().bind(player.playerNameProperty());
        }
        if (moneyLabel != null) {
            moneyLabel.textProperty().bind(player.goldProperty().asString());
        }

        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }
        
        // Bind XP progress bar
        if (xpBar != null) {
            final double MAX_XP = 100.0; // Same as in ProfileController
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

    @FXML
    private void confermaAzione() {
        List<CheckBox> tasks = tuttiIBottoniDelleTask();
        List<ImageView> flags = tutteLeFlag();

        ColorAdjust verdeEffect = new ColorAdjust();
        verdeEffect.setHue(0.6);
        verdeEffect.setSaturation(1.0);
        verdeEffect.setBrightness(0.3);

        int size = Math.min(tasks.size(), flags.size());

        for (int i = 0; i < size; i++) {
            CheckBox task = tasks.get(i);
            ImageView flag = flags.get(i);

            if (task.isSelected() && !task.isDisabled()) {
                task.setDisable(true);
                flag.setEffect(verdeEffect);
                System.out.println("Task " + (i+1) + " completata!");
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("dailytasks.fxml"));
            Parent dailyTasksView = loader.load();

            if (getClass().getResource("style.css") != null) {
                String css = this.getClass().getResource("style.css").toExternalForm();
                dailyTasksView.getStylesheets().add(css);
            }

            GridPane dailyTasksGrid = (GridPane) dailyTasksView.lookup("#tasksGrid");

            applyStylesToAllNodes(dailyTasksGrid);

            MusicManager.getInstance().playSoundEffect("change_screen.mp3");

            TaskController dailyController = loader.getController();
            dailyController.setBackButtonVisible(true);
            dailyController.setDailyTasksButtonVisible(false);
            dailyController.setHomeScene(homeScene);

            // Importante: controlla che mainContainer non sia null prima di fare lookup
            if (mainContainer != null) {
                GridPane currentGrid = (GridPane) mainContainer.lookup("#tasksGrid");
                if (currentGrid != null) {
                    Pane parent = (Pane) currentGrid.getParent();
                    if (parent != null) {
                        parent.getChildren().remove(currentGrid);
                        dailyTasksGrid.setLayoutX(currentGrid.getLayoutX());
                        dailyTasksGrid.setLayoutY(currentGrid.getLayoutY());
                        parent.getChildren().add(dailyTasksGrid);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento di dailytasks.fxml: " + e.getMessage());
        }
    }

    @FXML
    private void showMainTasks() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Tasks.fxml"));
            Parent tasksView = loader.load();

            applyStylesToAllNodes(tasksView);

            GridPane tasksGrid = (GridPane) tasksView.lookup("#tasksGrid");
            if (tasksGrid != null) {
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
                            tasksGrid.setLayoutX(currentGrid.getLayoutX());
                            tasksGrid.setLayoutY(currentGrid.getLayoutY());
                            parent.getChildren().add(tasksGrid);
                        }
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
}