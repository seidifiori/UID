package org.example.ProgettoUIDFinal;

import javafx.application.Platform;
import javafx.event.ActionEvent; // Import necessario
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage; // Import necessario
import org.example.ProgettoUIDFinal.model.GameRepository;
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

public class AddQuestController implements Initializable {

    @FXML private Button ConfirmButton;
    @FXML private Button IaButton;
    @FXML private Label TitleLabel;
    @FXML private Label DescriptionLabel;
    @FXML private TextField TitleTextField;
    @FXML private TextField DescriptionTextField;
    @FXML private Label difficoltàsuggerita;

    private int difficulty = 1; // Default a 1 per sicurezza
    private Scene homeScene;

    // --- NUOVO: Riferimento al controller padre ---
    private TaskController parentController;

    @Override
    public void initialize(URL location, ResourceBundle resources){
        PlayerModel player = GameRepository.getInstance().getPlayer();
        if(ConfirmButton != null) {
            ConfirmButton.setVisible(false);
        }
    }

    public void setHomeScene(Scene scene) {
        this.homeScene = scene;
    }

    // --- NUOVO: Metodo per impostare il padre ---
    public void setParentController(TaskController controller) {
        this.parentController = controller;
    }

    // --- NUOVO: Metodo che scatta quando premi CONFERMA ---
    // Assicurati che nel tuo AddQuest.fxml il bottone abbia: onAction="#onConfirmClicked"
    @FXML
    public void onConfirmClicked(ActionEvent event) {
        if (parentController != null) {
            String title = TitleTextField.getText();
            String desc = DescriptionTextField.getText();

            // Passiamo i dati al TaskController che creerà il bottone
            parentController.aggiungiNuovaQuest(title, desc, this.difficulty);

            // Chiudiamo la finestra di aggiunta
            Stage stage = (Stage) ConfirmButton.getScene().getWindow();
            stage.close();
        } else {
            System.err.println("Errore: parentController è null!");
        }
    }

    public void askIA() {
        String title = TitleTextField.getText();
        String description = DescriptionTextField.getText();

        // Controllo base se i campi sono vuoti
        if (title.isEmpty() || description.isEmpty()) {
            difficoltàsuggerita.setText("Inserisci titolo e descrizione!");
            return;
        }

        difficoltàsuggerita.setText("Elaborazione in corso...");
        ConfirmButton.setVisible(false); // Nascondi conferma mentre calcola

        new Thread(() -> {
            try {
                int resultDifficulty = callGeminiAPI(title, description);
                this.difficulty = resultDifficulty;

                Platform.runLater(() -> {
                    ConfirmButton.setVisible(true);
                    difficoltàsuggerita.setText("Difficoltà suggerita: " + this.difficulty);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    difficoltàsuggerita.setText("Errore API. Uso default.");
                    this.difficulty = 1; // Fallback
                    ConfirmButton.setVisible(true);
                });
            }
        }).start();
    }

    // -------------------------------
    //        GEMINI API CON RETRY
    // -------------------------------
    private int callGeminiAPI(String title, String desc) throws Exception {

        String apiKey = System.getenv("GEMINI_API_KEY");
        // Se non trovi la key, usa un fallback invece di crashare l'app
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("API KEY MANCANTE: Ritorno valore casuale per test.");
            return new Random().nextInt(4) + 1;
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        String safeTitle = title.replace("\"", "'").replace("\n", " ");
        String safeDesc = desc.replace("\"", "'").replace("\n", " ");

        String prompt = "Analizza questo titolo: '" + safeTitle +
                "' e questa descrizione: '" + safeDesc +
                "'. Stima una difficolta da 1 a 4. Rispondi solo con un numero intero, RISPOSTA 0 SE NON APPROPRIATA.";

        String jsonBody = "{"
                + "\"contents\": [{"
                + "  \"parts\": [{\"text\": \"" + prompt + "\"}]"
                + "}]"
                + "}";

        HttpClient client = HttpClient.newHttpClient();

        System.out.println("Invio richiesta a Gemini...");

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
                    System.out.println("Modello sovraccarico, retry in corso...");
                    Thread.sleep(2000);
                    retries--;
                } else {
                    System.err.println("Errore API: " + response.body());
                    throw new RuntimeException("Errore API Gemini code: " + response.statusCode());
                }

            } catch (Exception e) {
                retries--;
                if (retries == 0) {
                    System.out.println("Fallito, ritorno numero casuale per test.");
                    return new Random().nextInt(4) + 1;
                }
                Thread.sleep(1000);
            }
        }
        return 1;
    }

    private int extractNumberFromGemini(String jsonResponse) {
        Pattern pattern = Pattern.compile("\"text\"\\s*:\\s*\"(\\d+)\"");
        Matcher matcher = pattern.matcher(jsonResponse);

        if (matcher.find()) {
            String answer = matcher.group(1).trim();
            try {
                return Integer.parseInt(answer);
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }
    // ---------------------------------------------------
    //    GESTIONE MANUALE DELLA DIFFICOLTÀ (Stelline)
    // ---------------------------------------------------

    @FXML
    public void setDiff1() {
        impostaManuale(1);
    }

    @FXML
    public void setDiff2() {
        impostaManuale(2);
    }

    @FXML
    public void setDiff3() {
        impostaManuale(3);
    }

    @FXML
    public void setDiff4() {
        impostaManuale(4);
    }

    private void impostaManuale(int diff) {
        this.difficulty = diff;
        difficoltàsuggerita.setText("Difficoltà manuale: " + diff);
        // Mostriamo il tasto conferma perché l'utente ha scelto una difficoltà
        ConfirmButton.setVisible(true);
    }
}