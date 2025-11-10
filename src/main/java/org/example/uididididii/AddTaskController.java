package org.example.uididididii;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AddTaskController {
    @FXML
    private VBox taskContainer;
    @FXML
    private Label emptyLabel;
    @FXML
    private Button addButton;
    private HelloApplication mainApp;

    public AddTaskController() {
    }

    public void setMainApp(HelloApplication mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        // Inizializza la lista vuota
        updateEmptyLabel();
    }

    @FXML
    private void handleAddButton() {
        try {
            // Questo apre la FINESTRA DIALOGO per inserire un task
            this.mainApp.showAddTaskDialog();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Metodo chiamato dalla finestra di dialogo per aggiungere un task
    public void addTask(String taskName) {
        if (taskName != null && !taskName.isBlank()) {
            // Nascondi il messaggio "nessun task"
            this.emptyLabel.setVisible(false);
            this.emptyLabel.setManaged(false);

            // Crea un nuovo CheckBox per il task
            CheckBox checkBox = new CheckBox(taskName);

            // RIMUOVI lo stile del testo bianco o imposta colore nero
            // Opzione 1: Rimuovi completamente lo stile
            checkBox.setStyle("");

            // Opzione 2: Imposta esplicitamente il testo nero
            // checkBox.setStyle("-fx-text-fill: black; -fx-font-size: 14px;");

            // Opzione 3: Usa lo stile normale (consigliato)
            // checkBox.setStyle("-fx-font-size: 14px;");

            // Aggiungi alla lista
            this.taskContainer.getChildren().add(checkBox);
        }
    }

    private void updateEmptyLabel() {
        boolean hasTasks = taskContainer.getChildren().size() > 1; // >1 perché emptyLabel è già un child
        emptyLabel.setVisible(!hasTasks);
        emptyLabel.setManaged(!hasTasks);
    }
}