package org.example.ProgettoUIDFinal;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.ProgettoUIDFinal.Services.BackgroundService;
import org.example.ProgettoUIDFinal.Services.MusicManager;
import org.example.ProgettoUIDFinal.Services.StyleManager;
import org.example.ProgettoUIDFinal.Services.GameRepository;
import org.example.ProgettoUIDFinal.model.PlayerModel;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.io.IOException;

/**
 * CONTROLLER PROFILO: Gestisce la visualizzazione dettagliata del personaggio.
 * Include logiche per il rendering grafico di statistiche (Spider Chart),
 * gestione della personalizzazione estetica (Banner) e riepilogo equipaggiamento.
 */
public class ProfileController {

    // --- ELEMENTI UI (Iniezione FXML) ---
    @FXML private Canvas canvas; // Utilizzato per il rendering procedurale del grafico statistiche
    @FXML private StackPane rootStackPane;
    @FXML private GridPane mainContentPane;
    @FXML private ImageView profilePicImageView, profileBannerImage;
    @FXML private Button BackButton;
    @FXML private Label moneyLabel, playerName, levelLabel, DaysLabel, TaskCompletedLabel;
    @FXML private ProgressBar xpBar, atkBar, defBar, velBar;

    // Icone Equipaggiamento (Slot statici)
    @FXML private ImageView hatIcon, hairIcon, armorIcon, swordIcon, shieldIcon;

    // Layer Avatar (Manichino dinamico)
    @FXML private ImageView baseAvatarLayer,ProfileBackground, hairLayer, hatLayer, armorLayer, swordLayer, shieldLayer;

    private Scene homeScene;
    private Tooltip sharedTooltip;
    private final String[] labels = {"Attacco", "Difesa", "Velocità"};

    /**
     * Iniezione della scena principale per la navigazione a ritroso.
     */
    @FXML public void setHomeScene(Scene scene) { this.homeScene = scene; }

    /**
     * INIZIALIZZAZIONE: Configura l'ambiente grafico e attiva i binding.
     * Recupera lo stato attuale dal PlayerModel e lo proietta sulla UI.
     */
    @FXML
    public void initialize() {
        PlayerModel player = GameRepository.getInstance().getPlayer();
        String currentSavedPath = BackgroundService.getInstance().getCurrentBackgroundPath();
        if (currentSavedPath != null) {
            player.setBackgroundPath(currentSavedPath);
        }

        // 1. SETUP GRAFICO E STILE
        drawSpiderChart(player); // Rendering procedurale sul Canvas
        initTooltipSystem(); // Configurazione del sistema di informazioni al passaggio del mouse
        if (rootStackPane != null) StyleManager.getInstance().applyStyle(rootStackPane);


        if (ProfileBackground != null) {

            if (player.getBackgroundPath() == null || player.getBackgroundPath().isEmpty()) {
                player.setBackgroundPath("/org/example/ProgettoUIDFinal/imagini/Backgrounds/sunny.png");
            }

            ProfileBackground.imageProperty().bind(player.backgroundImageProperty());
        }



        // --- BINDING BANNER E AVATAR ---
        if (profileBannerImage != null) {
            profileBannerImage.imageProperty().bind(player.bannerImageProperty());
        }
        if (profilePicImageView != null) {
            profilePicImageView.imageProperty().bind(player.avatarImageProperty());
        }

        // 2. DATA BINDING (Testi)
        bindText(playerName, player.playerNameProperty());
        bindText(levelLabel, player.levelProperty().asString());
        bindText(moneyLabel, player.goldProperty().asString());
        bindText(DaysLabel, player.daysNumberProperty().asString());
        bindText(TaskCompletedLabel, player.taskCompletedProperty().asString());

        // 3. PROGRESS BARS BINDING
        double MAX = 100.0;
        if (xpBar != null) xpBar.progressProperty().bind(player.xpProperty().divide(MAX));
        if (atkBar != null) atkBar.progressProperty().bind(player.atkProperty().divide(MAX));
        if (defBar != null) defBar.progressProperty().bind(player.defProperty().divide(MAX));
        if (velBar != null) velBar.progressProperty().bind(player.velProperty().divide(MAX));

        // 4. BINDING LAYER AVATAR (Manichino dinamico)
        bindLayer(baseAvatarLayer, player.bodyImageProperty());
        bindLayer(hairLayer, player.hairImageProperty());
        bindLayer(hatLayer, player.hatImageProperty());
        bindLayer(armorLayer, player.armorImageProperty());
        bindLayer(swordLayer, player.swordImageProperty());
        bindLayer(shieldLayer, player.shieldImageProperty());

        if (hairLayer != null) {
            hairLayer.visibleProperty().bind(player.isHairVisibleProperty());
        }

        // 5. SETUP ICONE EQUIPAGGIAMENTO E TOOLTIPS
        setupIcon(hatIcon, player.hatIconProperty(), player.hatNameProperty());
        setupIcon(hairIcon, player.hairIconProperty(), player.hairNameProperty());
        setupIcon(armorIcon, player.armorIconProperty(), player.armorNameProperty());
        setupIcon(swordIcon, player.swordIconProperty(), player.swordNameProperty());
        setupIcon(shieldIcon, player.shieldIconProperty(), player.shieldNameProperty());

        BackButton.setCancelButton(true);
    }

    // --- METODI HELPER (Binding & UI Logic) ---

    private void bindText(Label l, ObservableValue<String> p) {
        if (l != null) l.textProperty().bind(p);
    }

    private void bindLayer(ImageView v, ObservableValue<? extends Image> p) {
        if (v != null) v.imageProperty().bind(p);
    }

    /**
     * Configura uno slot di equipaggiamento legando l'immagine e il tooltip informativo.
     */
    private void setupIcon(ImageView iv, ObservableValue<Image> imgP, ObservableValue<String> nameP) {
        if (iv != null) {
            iv.imageProperty().bind(imgP);
            setupTooltip(iv, nameP);
        }
    }


    /**
     * Aggiorna graficamente il banner del profilo caricando la risorsa dal classpath.
     */
    public void updateBannerPicture(String imageUrl) {
        PlayerModel player = GameRepository.getInstance().getPlayer();
        player.setBannerPath(imageUrl);
    }

    /**
     * INTERFACE MODALE: Apre il selettore di immagini/banner sovrapponendolo alla scena attuale.
     * Utilizza un effetto GaussianBlur sulla scena sottostante per enfatizzare il focus.
     */
    @FXML
    protected void handleProfilePicClick(ActionEvent event) {
        if (rootStackPane.lookup("#picChooserPane") != null) return;
        try {
            PlayerModel player = GameRepository.getInstance().getPlayer();
            MusicManager.getInstance().playSoundEffect("change_screen.mp3");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("profilePicChooser.fxml"));
            Parent view = loader.load();

            // Otteniamo il controller del Chooser
            ProfilePicChooserController chooser = loader.getController();

            // Passiamo i dati necessari (ora prendiamo il banner dal modello, non più da Preferences)
            chooser.initData(this, mainContentPane, player.getBannerPath());

            mainContentPane.setEffect(new GaussianBlur(10));
            mainContentPane.setDisable(true);
            rootStackPane.getChildren().add(view);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * RENDERING GRAFICO (Spider Chart): Disegna sul Canvas un grafico poligonale a 3 assi.
     * Utilizza funzioni trigonometriche (Seno/Coseno) per mappare i valori di Atk, Def e Vel
     * in uno spazio bidimensionale.
     */
    private void drawSpiderChart(PlayerModel player) {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        double cx = w / 2, cy = h / 2, radius = Math.min(w, h) / 2 - 40;
        double[] stats = { (double) player.getAtk(), (double) player.getDef(), (double) player.getVel() };
        double angleStep = 2 * Math.PI / 3; // Suddivisione in 3 assi (120 gradi)

        gc.clearRect(0, 0, w, h);

        // Disegno della griglia (Livelli statistici)
        gc.setStroke(Color.LIGHTGRAY);
        for (int i = 1; i <= 3; i++) {
            double r = radius * i / 3;
            gc.beginPath();
            for (int j = 0; j < 3; j++) {
                double x = cx + r * Math.sin(j * angleStep), y = cy - r * Math.cos(j * angleStep);
                if (j == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
            }
            gc.closePath(); gc.stroke();
        }

        // Rendering dell'area statistica effettiva
        gc.setStroke(Color.DODGERBLUE); gc.setFill(Color.rgb(30, 144, 255, 0.4));
        gc.beginPath();
        for (int i = 0; i < 3; i++) {
            double r = radius * Math.min(1.0, stats[i] / 100.0);
            double x = cx + r * Math.sin(i * angleStep), y = cy - r * Math.cos(i * angleStep);
            if (i == 0) gc.moveTo(x, y); else gc.lineTo(x, y);
        }
        gc.closePath(); gc.fill(); gc.stroke();

        // Rendering delle etichette statistiche
        gc.setFill(Color.BLACK);
        Font minecraftFont = Font.loadFont(getClass().getResourceAsStream("/org/example/ProgettoUIDFinal/Minecraft.ttf"), 13);
        gc.setFont(minecraftFont);

        for (int i = 0; i < 3; i++) {
            double x = cx + (radius + 25) * Math.sin(i * angleStep), y = cy - (radius + 25) * Math.cos(i * angleStep);
            gc.fillText(labels[i] + " " + (int)stats[i], x - 20, y + 5);
        }
    }

    // --- TOOLTIP SYSTEM (Informazioni al passaggio del mouse) ---

    private void initTooltipSystem() {
        sharedTooltip = new Tooltip();
        sharedTooltip.setShowDelay(Duration.ZERO);
        sharedTooltip.setHideDelay(Duration.ZERO);
        sharedTooltip.getStyleClass().add("tooltip-custom");
    }

    private void setupTooltip(ImageView target, ObservableValue<String> textProperty) {
        target.setOnMouseEntered(e -> {
            sharedTooltip.textProperty().bind(textProperty);
            sharedTooltip.show(target, e.getScreenX() + 15, e.getScreenY() + 15);
        });
        target.setOnMouseMoved(e -> {
            sharedTooltip.setX(e.getScreenX() + 15);
            sharedTooltip.setY(e.getScreenY() + 15);
        });
        target.setOnMouseExited(e -> {
            sharedTooltip.hide();
            sharedTooltip.textProperty().unbind();
        });
    }
    @FXML
    private void handlePDFprint(ActionEvent event) {
        // 1. Chiedi all'utente dove salvare il file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Profilo PDF");
        fileChooser.setInitialFileName("Profilo_" + playerName.getText() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(rootStackPane.getScene().getWindow());

        if (file != null) {
            try {
                // 2. Cattura lo screenshot del pannello principale
                // Usiamo mainContentPane per evitare di stampare eventuali background esterni del root
                WritableImage snapshot = mainContentPane.snapshot(new SnapshotParameters(), null);

                // 3. Converti lo snapshot in un formato leggibile dal PDF (PNG in memoria)
                ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", byteOutput);

                // 4. Crea il documento PDF in formato A4 Orizzontale
                Document doc = new Document(PageSize.A4.rotate());
                PdfWriter.getInstance(doc, new FileOutputStream(file));
                doc.open();

                // 5. Trasforma i byte in un'immagine per il PDF
                com.lowagie.text.Image pdfImage = com.lowagie.text.Image.getInstance(byteOutput.toByteArray());

                // Scala l'immagine per farla stare bene nella pagina (lasciando un po' di margine)
                pdfImage.scaleToFit(PageSize.A4.rotate().getWidth() - 40, PageSize.A4.rotate().getHeight() - 40);
                pdfImage.setAlignment(com.lowagie.text.Image.ALIGN_CENTER);

                doc.add(pdfImage);
                doc.close();

                System.out.println("PDF generato con successo in: " + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * NAVIGAZIONE: Ripristina la scena principale.
     */
    @FXML
    public void Home() {
        MusicManager.getInstance().playSoundEffect("change_screen.mp3");
        if (homeScene != null) ((Stage) BackButton.getScene().getWindow()).setScene(homeScene);
    }
}