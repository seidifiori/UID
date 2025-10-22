package org.example.uididididii;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    private HelloApplication mainApp;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inizializzazione del controller
    }

    @FXML
    private void showAddTaskView() {
        try {
            // Questo apre la LISTA TASK (AddTaskView)
            mainApp.showAddTaskView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMainApp(HelloApplication mainApp) {
        this.mainApp = mainApp;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}