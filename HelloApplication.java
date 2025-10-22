package org.example.uididididii;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane; // Changed from AnchorPane
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    private Stage primaryStage;
    private HelloController mainController;
    private AddTaskController addTaskController;


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        StackPane root = fxmlLoader.load(); // Root is StackPane (for background)

        // Get the inner BorderPane
        BorderPane borderPane = (BorderPane) root.getChildren().get(1);

        mainController = fxmlLoader.getController();
        mainController.setMainApp(this);
        mainController.setPrimaryStage(stage);

        Scene scene = new Scene(root, 900, 450);

        root.prefWidthProperty().bind(scene.widthProperty());
        root.prefHeightProperty().bind(scene.heightProperty());

        // Scaling based on scene size
        double baseWidth = 900.0;
        double baseHeight = 450.0;

        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            double scale = newVal.doubleValue() / baseWidth;
            root.setScaleX(scale);
            root.setScaleY(scale);
        });

        scene.heightProperty().addListener((obs, oldVal, newVal) -> {
            double scale = newVal.doubleValue() / baseHeight;
            root.setScaleX(scale);
            root.setScaleY(scale);
        });

        stage.setTitle("Home");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }


    // ✅ Mostra la lista task (AddTaskView)
    public void showAddTaskView() throws IOException {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("add-task-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 400, 300);
        Stage taskStage = new Stage();
        taskStage.setTitle("Lista Task");
        taskStage.setScene(scene);

        addTaskController = loader.getController();
        addTaskController.setMainApp(this);
        taskStage.show();
    }

    // ✅ Mostra la finestra di dialogo per aggiungere un task
    public void showAddTaskDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("add-task-dialog.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 400, 200);
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Aggiungi Nuovo Task");
        dialogStage.setScene(scene);

        AddTaskDialogController controller = loader.getController();
        controller.setMainApp(this);
        controller.setDialogStage(dialogStage);
        dialogStage.showAndWait();
    }

    public HelloController getMainController() {
        return this.mainController;
    }

    public AddTaskController getAddTaskController() {
        return this.addTaskController;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
