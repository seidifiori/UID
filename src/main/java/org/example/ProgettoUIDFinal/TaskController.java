package org.example.ProgettoUIDFinal;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class TaskController {
    @FXML
    private AnchorPane mainContainer;
    @FXML
    private VBox tasksContainer;
    @FXML
    private ImageView backgroundImageView;
    @FXML
    private Button backButton;
    @FXML
    private Button dailyTasksButton;

    private Scene homeScene;
    private final List<String> dailyTasks = List.of(
            "Complete morning routine",
            "Drink 8 glasses of water",
            "30 minutes of exercise",
            "Read for 20 minutes",
            "Work on project for 1 hour"
    );

    @FXML
    private void initialize() {
        // Apply style to main container and all its children
        if (mainContainer != null) {
            applyStylesToAllNodes(mainContainer);
        }

        // Set initial background
        Image currentBg = BackgroundService.getInstance().getBackground();
        if (currentBg != null) {
            applyBackground(backgroundImageView, currentBg);
        }

        // Listen for background changes
        BackgroundService.getInstance().backgroundProperty().addListener((obs, oldImg, newImg) -> {
            if (newImg != null) {
                applyBackground(backgroundImageView, newImg);
            }
        });
    }
    
    /**
     * Recursively applies styles to a node and all its children
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

    @FXML
    private void showDailyTasks() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("dailytasks.fxml"));
                Parent dailyTasksView = loader.load();

                // -------------------------------------------------------------------------
                // AGGIUNGI QUESTO. ORA.
                // Assumendo che il tuo CSS si chiami "style.css" e sia nelle risorse.
                // Senza questo, il tuo FXML è cieco.
                String css = this.getClass().getResource("style.css").toExternalForm();
                dailyTasksView.getStylesheets().add(css);
                // -------------------------------------------------------------------------

                GridPane dailyTasksGrid = (GridPane) dailyTasksView.lookup("#tasksGrid");

                // Il resto del tuo codice pasticciato...
                applyStylesToAllNodes(dailyTasksGrid);
            // ---------------------------------------------------------

            MusicManager.getInstance().playSoundEffect("change_screen.mp3");

            TaskController dailyController = loader.getController();

            dailyController.setBackButtonVisible(true);
            dailyController.setDailyTasksButtonVisible(false);
            dailyController.setHomeScene(homeScene);

            GridPane currentGrid = (GridPane) mainContainer.lookup("#tasksGrid");
            if (currentGrid != null) {
                Pane parent = (Pane) currentGrid.getParent();
                if (parent != null) {
                    parent.getChildren().remove(currentGrid);

                    dailyTasksGrid.setLayoutX(currentGrid.getLayoutX());
                    dailyTasksGrid.setLayoutY(currentGrid.getLayoutY());
                    parent.getChildren().add(dailyTasksGrid);

                    // Questa riga sotto ora è ridondante se usi applyStylesToAllNodes sopra,
                    // ma conoscendoti, lasciala pure per sicurezza emotiva.
                    // StyleManager.getInstance().applyStyle(dailyTasksGrid);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showMainTasks() {
        try {
            // Load the main tasks GridPane
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Tasks.fxml"));
            Parent tasksView = loader.load();
            
            // Apply style to the loaded view and all its children
            applyStylesToAllNodes(tasksView);
            
            GridPane tasksGrid = (GridPane) tasksView.lookup("#tasksGrid");
            if (tasksGrid != null) {
                // Get the controller
                TaskController tasksController = loader.getController();
                
                // Set up the buttons in the main tasks view
                tasksController.setBackButtonVisible(true);
                tasksController.setDailyTasksButtonVisible(true);
                tasksController.setHomeScene(homeScene);
                
                // Find the current GridPane in the main container
                GridPane currentGrid = (GridPane) mainContainer.lookup("#tasksGrid");
                if (currentGrid != null) {
                    // Get the parent of the current GridPane
                    Pane parent = (Pane) currentGrid.getParent();
                    if (parent != null) {
                        // Remove the current GridPane
                        parent.getChildren().remove(currentGrid);
                        
                        // Add the new GridPane with the same layout constraints
                        tasksGrid.setLayoutX(currentGrid.getLayoutX());
                        tasksGrid.setLayoutY(currentGrid.getLayoutY());
                        parent.getChildren().add(tasksGrid);
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
    public void Home() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");

        // AGGIUNGI QUESTA RIGA: Rimetti la musica principale
        MusicManager.getInstance().playMusic("background_music.mp3");

        if (homeScene != null) {
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.setScene(homeScene);
        }
    }


    public void setBackButtonVisible(boolean visible) {
        if (backButton != null) {
            backButton.setVisible(true);
        }
    }

    public void setDailyTasksButtonVisible(boolean visible) {
        if (dailyTasksButton != null) {
            dailyTasksButton.setVisible(true);
        }
    }

    private void applyBackground(ImageView imageView, Image image) {
        if (imageView != null && image != null) {
            imageView.setImage(image);
        }
    }
}