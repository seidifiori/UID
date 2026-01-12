package org.example.ProgettoUIDFinal.Services;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import java.io.InputStream;

/**
 * SERVIZIO CENTRALIZZATO: Gestisce lo stato dello sfondo dell'applicazione.
 * Implementa il Pattern SINGLETON per garantire un unico punto di accesso globale
 * e la coerenza dei dati tra i diversi controller (Shop, Closet, Home).
 */
public class BackgroundService {

    // ISTANZA STATICA: Unica istanza del servizio condivisa nell'intero ciclo di vita del software.
    private static final BackgroundService INSTANCE = new BackgroundService();

    /**
     * PROPRIETÀ OSSERVABILE: Contiene l'istanza dell'immagine corrente.
     * Utilizza il Pattern OBSERVER tramite JavaFX Properties per notificare
     * automaticamente i componenti UI legati allo sfondo.
     */
    private final ObjectProperty<Image> background = new SimpleObjectProperty<>(null);

    /**
     * PERSISTENZA STATO: Stringa del percorso relativo della risorsa.
     * Necessaria per la serializzazione nel file JSON di salvataggio.
     */
    private String currentBackgroundPath = null;

    // COSTRUTTORE PRIVATO: Impedisce l'istanziazione esterna (Pattern Singleton).
    private BackgroundService() {}

    /**
     * ACCESSOR SINGLETON: Restituisce il punto di accesso globale al servizio.
     */
    public static BackgroundService getInstance() {
        return INSTANCE;
    }

    // --- METODI DI INTERFACCIA ---

    public Image getBackground() { return background.get(); }

    public ObjectProperty<Image> backgroundProperty() { return background; }

    /**
     * DATA RETRIEVAL: Restituisce il percorso testuale dello sfondo.
     * Utilizzato dal GameRepository durante la procedura di salvataggio dati.
     */
    public String getCurrentBackgroundPath() {
        return currentBackgroundPath;
    }

    // --- LOGICA DI CARICAMENTO E AGGIORNAMENTO ---

    /**
     * RESOURCE RESOLUTION: Carica lo sfondo tramite il percorso della risorsa.
     * Gestisce il parsing della stringa e l'apertura dello stream I/O per
     * convertire il file statico in un oggetto Image utilizzabile dalla UI.
     */
    public void setBackgroundByPath(String path) {
        if (path == null || path.isEmpty()) return;

        // Aggiornamento dello stato del percorso per la persistenza
        this.currentBackgroundPath = path;

        try {
            // Normalizzazione della stringa (rimozione quote e whitespace)
            String cleanPath = path.replace("\"", "").trim();

            // Reperimento dello stream dalla risorsa nel classpath
            InputStream is = getClass().getResourceAsStream(cleanPath);
            if (is != null) {
                Image img = new Image(is);
                setBackground(img);
            } else {
                System.err.println("BackgroundService: Risorsa non trovata al percorso: " + cleanPath);
            }
        } catch (Exception e) {
            System.err.println("BackgroundService: Errore critico nel caricamento dell'immagine.");
        }
    }

    /**
     * UI THREAD MANAGEMENT: Imposta l'immagine nella proprietà osservabile.
     * Implementa un controllo sul thread corrente per garantire che l'aggiornamento
     * della grafica avvenga esclusivamente sul JavaFX Application Thread,
     * evitando eccezioni di tipo "Not on FX application thread".
     */
    public void setBackground(Image image) {
        if (Platform.isFxApplicationThread()) {
            background.set(image);
        } else {
            // Delega l'aggiornamento alla coda di esecuzione della UI
            Platform.runLater(() -> background.set(image));
        }
    }
}