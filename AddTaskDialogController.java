package org.example.uididididii;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddTaskDialogController {
    @FXML
    private TextField taskInput;

    private HelloApplication mainApp;
    private Stage dialogStage;

    @FXML
    private void initialize() {
        // Inizializzazione
    }

    @FXML
    private void handleConfirm() {
        String taskName = taskInput.getText();
        if (taskName != null && !taskName.trim().isEmpty()) {
            // Passa il task al controller della lista
            if (mainApp.getAddTaskController() != null) {
                mainApp.getAddTaskController().addTask(taskName);
            }
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    public void setMainApp(HelloApplication mainApp) {
        this.mainApp = mainApp;
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}