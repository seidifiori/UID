package org.example.ProgettoUIDFinal;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.ProgettoUIDFinal.Services.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller JavaFX responsabile della schermata di creazione di una nuova quest.
 * Gestisce:
 * - Inserimento titolo e descrizione
 * - Calcolo automatico della difficoltà tramite API Gemini
 * - Impostazione manuale della difficoltà
 * - Comunicazione con il controller principale (TaskController)
 */
public class AddQuestController implements Initializable {

    /* Componenti UI collegati tramite FXML */
    @FXML private Button ConfirmButton;
    @FXML private Button IaButton;
    @FXML private Label TitleLabel;
    @FXML private Label DescriptionLabel;
    @FXML private TextField TitleTextField;
    @FXML private TextField DescriptionTextField;
    @FXML private Label difficoltàsuggerita;

    /* Difficoltà della quest (1–4). Valore di default per sicurezza */
    private int difficulty = 1;

    /* Riferimento alla scena principale per eventuali ritorni */
    private Scene homeScene;

    /* Riferimento al controller padre che gestisce la lista delle quest */
    private TaskController parentController;

    /**
     * Metodo chiamato automaticamente all’inizializzazione del controller.
     * Viene usato per impostare lo stato iniziale dell’interfaccia.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources){
        PlayerModel player = GameRepository.getInstance().getPlayer();

        /* Il pulsante di conferma viene nascosto finché non è definita la difficoltà */
        if(ConfirmButton != null) {
            ConfirmButton.setVisible(false);
        }
        ConfirmButton.setDefaultButton(true);
    }

    /**
     * Imposta la scena principale da cui è stata aperta la finestra.
     */
    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }

    /**
     * Imposta il controller padre per permettere la comunicazione
     * tra la finestra di aggiunta quest e la schermata principale.
     */
    public void setParentController(TaskController controller) {
        this.parentController = controller;
    }

    /**
     * Gestisce il click sul pulsante "Conferma".
     * Invia i dati della nuova quest al TaskController e chiude la finestra.
     */
    @FXML
    public void onConfirmClicked(ActionEvent event) {
        if (parentController != null) {
            String title = TitleTextField.getText();
            String desc = DescriptionTextField.getText();

            /* Delego al controller padre la creazione della nuova quest */
            parentController.aggiungiNuovaQuest(title, desc, this.difficulty);

            /* Chiusura della finestra di inserimento */
            Stage stage = (Stage) ConfirmButton.getScene().getWindow();
            stage.close();
        } else {
            System.err.println("Errore: parentController non inizializzato");
        }
    }

    /**
     * Avvia il calcolo automatico della difficoltà utilizzando l'IA.
     * La chiamata viene eseguita su thread separato per non bloccare la UI.
     */
    public void askIA() {
        String title = TitleTextField.getText();
        String description = DescriptionTextField.getText();

        /* Validazione di base dei campi */
        if (title.isEmpty() || description.isEmpty()) {
            difficoltàsuggerita.setText("Inserisci titolo e descrizione!");
            return;
        }

        difficoltàsuggerita.setText("Elaborazione in corso...");
        ConfirmButton.setVisible(false);

        new Thread(() -> {
            try {
                int resultDifficulty = callGeminiAPI(title, description);
                this.difficulty = resultDifficulty;

                /* Aggiornamento UI sul JavaFX Application Thread */
                Platform.runLater(() -> {
                    ConfirmButton.setVisible(true);
                    difficoltàsuggerita.setText("Difficoltà suggerita: " + this.difficulty);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    difficoltàsuggerita.setText("Errore API. Uso valore di default.");
                    this.difficulty = 1;
                    ConfirmButton.setVisible(true);
                });
            }
        }).start();
    }

    /**
     * Effettua la chiamata HTTP alle API Gemini per stimare la difficoltà.
     * Implementa un meccanismo di retry in caso di errore temporaneo.
     */
    private int callGeminiAPI(String title, String desc) throws Exception {

        String apiKey = System.getenv("GEMINI_API_KEY");

        /* Fallback se la chiave API non è presente */
        if (apiKey == null || apiKey.isEmpty()) {
            return new Random().nextInt(4) + 1;
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        String safeTitle = title.replace("\"", "'").replace("\n", " ");
        String safeDesc = desc.replace("\"", "'").replace("\n", " ");

        String prompt = "Analizza questo titolo: '" + safeTitle +
                "' e questa descrizione: '" + safeDesc +
                "'. Stima una difficolta da 1 a 4. Rispondi solo con un numero intero.";

        String jsonBody = "{"
                + "\"contents\": [{"
                + "  \"parts\": [{\"text\": \"" + prompt + "\"}]"
                + "}]"
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        int retries = 3;

        while (retries > 0) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return extractNumberFromGemini(response.body());
                } else if (response.statusCode() == 503) {
                    retries--;
                    Thread.sleep(2000);
                } else {
                    throw new RuntimeException("Errore API: " + response.statusCode());
                }

            } catch (Exception e) {
                retries--;
                if (retries == 0) {
                    return new Random().nextInt(4) + 1;
                }
                Thread.sleep(1000);
            }
        }
        return 1;
    }

    /**
     * Estrae il valore numerico della difficoltà dalla risposta JSON di Gemini.
     */
    private int extractNumberFromGemini(String jsonResponse) {
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"(\\d+)\"");
        Matcher matcher = pattern.matcher(jsonResponse);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 1;
    }

    /* Metodi FXML per impostazione manuale della difficoltà */

    @FXML public void setDiff1() { impostaManuale(1); }
    @FXML public void setDiff2() { impostaManuale(2); }
    @FXML public void setDiff3() { impostaManuale(3); }
    @FXML public void setDiff4() { impostaManuale(4); }

    /**
     * Imposta manualmente la difficoltà e abilita la conferma.
     */
    private void impostaManuale(int diff) {
        this.difficulty = diff;
        difficoltàsuggerita.setText("Difficoltà manuale: " + diff);
        ConfirmButton.setVisible(true);
    }
}
