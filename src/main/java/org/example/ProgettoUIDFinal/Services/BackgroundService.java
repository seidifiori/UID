package org.example.ProgettoUIDFinal.Services;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * SERVIZIO CENTRALIZZATO: Gestisce lo stato dello sfondo dell'applicazione.
 * Implementa il Pattern SINGLETON per garantire un unico punto di accesso globale
 * e la coerenza dei dati tra i diversi controller (Shop, Closet, Home).
 */
public class BackgroundService {

    // ISTANZA STATICA: Unica istanza del servizio condivisa nell'intero ciclo di vita del software.
    private static final BackgroundService INSTANCE = new BackgroundService();

    /**
     * PROPRIETÀ OSSERVABILI:
     * - background: immagine corrente caricata
     * - currentBackgroundPath: percorso testuale dello sfondo corrente
     */
    private final ObjectProperty<Image> background = new SimpleObjectProperty<>(null);
    private final StringProperty currentBackgroundPath = new SimpleStringProperty("");

    // Lista di listener per notifiche personalizzate
    private final Set<BackgroundChangeListener> listeners = new HashSet<>();

    // COSTRUTTORE PRIVATO: Impedisce l'istanziazione esterna (Pattern Singleton).
    private BackgroundService() {
        // Aggiungi un listener alla property per notificare i cambiamenti
        currentBackgroundPath.addListener((observable, oldValue, newValue) -> {
            notifyBackgroundPathChanged(newValue);
        });
    }

    /**
     * INTERFACCIA per i listener personalizzati
     */
    public interface BackgroundChangeListener {
        void onBackgroundPathChanged(String newPath);
        void onBackgroundImageChanged(Image newImage);
    }

    /**
     * ACCESSOR SINGLETON: Restituisce il punto di accesso globale al servizio.
     */
    public static BackgroundService getInstance() {
        return INSTANCE;
    }

    // --- METODI DI INTERFACCIA ---

    public Image getBackground() { return background.get(); }
    public ObjectProperty<Image> backgroundProperty() { return background; }

    public String getCurrentBackgroundPath() {
        return currentBackgroundPath.get();
    }

    public StringProperty currentBackgroundPathProperty() {
        return currentBackgroundPath;
    }

    // --- GESTIONE LISTENER ---

    public void addListener(BackgroundChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(BackgroundChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyBackgroundPathChanged(String newPath) {
        for (BackgroundChangeListener listener : listeners) {
            listener.onBackgroundPathChanged(newPath);
        }
    }

    private void notifyBackgroundImageChanged(Image newImage) {
        for (BackgroundChangeListener listener : listeners) {
            listener.onBackgroundImageChanged(newImage);
        }
    }

    // --- LOGICA DI CARICAMENTO E AGGIORNAMENTO ---

    /**
     * RESOURCE RESOLUTION: Carica lo sfondo tramite il percorso della risorsa.
     * Gestisce il parsing della stringa e l'apertura dello stream I/O per
     * convertire il file statico in un oggetto Image utilizzabile dalla UI.
     */
    public void setBackgroundByPath(String path) {
        if (path == null || path.isEmpty()) {
            System.err.println("BackgroundService: Percorso nullo o vuoto");
            return;
        }

        // Verifica se il percorso è già quello corrente
        if (path.equals(currentBackgroundPath.get())) {
            System.out.println("BackgroundService: Il percorso è già quello corrente: " + path);
            return;
        }


        try {
            // Normalizzazione della stringa (rimozione quote e whitespace)
            String cleanPath = path.replace("\"", "").trim();

            // Aggiorna prima il percorso (questo attiverà i listener)
            Platform.runLater(() -> {
                currentBackgroundPath.set(cleanPath);
            });

            // Poi carica l'immagine
            loadBackgroundImage(cleanPath);

        } catch (Exception e) {
            System.err.println("BackgroundService: Errore critico nel caricamento dell'immagine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carica l'immagine dallo stream e la imposta
     */
    private void loadBackgroundImage(String cleanPath) {
        try {
            // Reperimento dello stream dalla risorsa nel classpath
            InputStream is = getClass().getResourceAsStream(cleanPath);
            if (is != null) {
                Image img = new Image(is);
                if (img.isError()) {
                    System.err.println("BackgroundService: Errore nel caricamento dell'immagine: " + img.getException().getMessage());
                } else {
                    setBackground(img);
                    notifyBackgroundImageChanged(img);
                }
            } else {
                System.err.println("BackgroundService: Risorsa non trovata al percorso: " + cleanPath);

                // Prova alternativa: cerca nel filesystem
                try {
                    Image img = new Image("file:" + cleanPath);
                    if (!img.isError()) {
                        setBackground(img);
                        notifyBackgroundImageChanged(img);
                    } else {
                        System.err.println("BackgroundService: Impossibile caricare dal filesystem: " + cleanPath);
                    }
                } catch (Exception e2) {
                    System.err.println("BackgroundService: Fallito anche il caricamento dal filesystem: " + e2.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("BackgroundService: Eccezione durante il caricamento: " + e.getMessage());
            e.printStackTrace();
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

    /**
     * Verifica se uno sfondo specifico è attualmente equipaggiato
     */
    public boolean isBackgroundEquipped(String path) {
        if (path == null || currentBackgroundPath.get() == null) {
            return false;
        }
        String cleanPath1 = path.replace("\"", "").trim();
        String cleanPath2 = currentBackgroundPath.get().replace("\"", "").trim();
        return cleanPath1.equals(cleanPath2);
    }
}