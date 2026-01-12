package org.example.ProgettoUIDFinal.Services;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.io.InputStream;

public class BackgroundService {
    private static final BackgroundService INSTANCE = new BackgroundService();

    // Questa proprietà contiene l'immagine vera e propria per la grafica
    private final ObjectProperty<Image> background = new SimpleObjectProperty<>(null);

    // NUOVO: Questa stringa ricorda il PERCORSO del file (fondamentale per il salvataggio JSON)
    private String currentBackgroundPath = null;

    private BackgroundService() {}

    public static BackgroundService getInstance() {
        return INSTANCE;
    }

    public Image getBackground() { return background.get(); }
    public ObjectProperty<Image> backgroundProperty() { return background; }

    // --- NUOVO METODO: Restituisce il percorso (Stringa) ---
    // Usato da GameRepository.saveGameToJSON()
    public String getCurrentBackgroundPath() {
        return currentBackgroundPath;
    }

    // --- NUOVO METODO: Imposta sfondo tramite percorso ---
    // Usato da GameRepository.loadGameFromJSON() e dai bottoni del Closet
    public void setBackgroundByPath(String path) {
        if (path == null || path.isEmpty()) return;

        // 1. Salviamo il percorso nella memoria
        this.currentBackgroundPath = path;

        // 2. Carichiamo l'immagine e aggiorniamo la grafica
        try {
            // Rimuovi eventuali doppi slash o virgolette se presenti
            String cleanPath = path.replace("\"", "").trim();
            InputStream is = getClass().getResourceAsStream(cleanPath);
            if (is != null) {
                Image img = new Image(is);
                setBackground(img);
            } else {
                System.err.println("BackgroundService: Impossibile trovare immagine: " + cleanPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo base per impostare l'immagine direttamente
    public void setBackground(Image image) {
        if (Platform.isFxApplicationThread()) {
            background.set(image);
        } else {
            Platform.runLater(() -> background.set(image));
        }
    }
}