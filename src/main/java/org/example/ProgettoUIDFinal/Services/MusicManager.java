package org.example.ProgettoUIDFinal.Services;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.prefs.Preferences;

/**
 * SERVIZIO DI GESTIONE AUDIO: Implementa una logica centralizzata per la riproduzione
 * di musica di sottofondo (Background Music) ed effetti sonori (SFX).
 * Utilizza il Pattern SINGLETON per evitare conflitti tra flussi audio sovrapposti.
 */
public class MusicManager {

    private static MusicManager instance;

    // MEDIA PLAYER: Gestisce flussi audio lunghi (musica), supporta loop e controllo volume/muto.
    private MediaPlayer backgroundPlayer;
    private String currentMusicFile = "";

    // PERSISTENZA: Utilizza le API Preferences di Java per salvare le impostazioni
    // nel registro di sistema (Windows) o nei file plist (macOS).
    private final Preferences prefs = Preferences.userNodeForPackage(MusicManager.class);

    // COSTANTI DI CONFIGURAZIONE: Chiavi univoche per il database delle preferenze.
    private static final String MUSIC_MUTED_KEY = "music.isMuted";
    private static final String SFX_MUTED_KEY = "sfx.isMuted";
    private static final String VOLUME_KEY = "music.volume";

    private boolean isMuted;
    private boolean SoundEffectisMuted;
    private double currentVolume;

    /**
     * COSTRUTTORE PRIVATO: Esegue il Bootstrapping delle preferenze utente.
     * Al caricamento del servizio, recupera i valori salvati l'ultima volta.
     */
    private MusicManager() {
        // Caricamento dei parametri dal registro di sistema con valori di default.
        this.isMuted = prefs.getBoolean(MUSIC_MUTED_KEY, false);
        this.SoundEffectisMuted = prefs.getBoolean(SFX_MUTED_KEY, false);
        this.currentVolume = prefs.getDouble(VOLUME_KEY, 0.5);
    }

    /**
     * ACCESSOR SINGLETON: Punto di accesso globale.
     */
    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    /**
     * GESTIONE MUSICA: Riproduce una traccia audio in loop continuo.
     * Implementa una logica di controllo per evitare il riavvio se la traccia richiesta
     * è già in esecuzione, ottimizzando l'uso delle risorse hardware.
     */
    public void playMusic(String fileName) {
        // Impedisce l'interruzione se il file è lo stesso (Anti-Stutter Logic)
        if (fileName.equals(currentMusicFile)) {
            return;
        }

        // Gestione del ciclo di vita del player: ferma e libera la memoria del precedente.
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer.dispose();
        }

        try {
            URL resource = getClass().getResource("/org/example/ProgettoUIDFinal/sounds/" + fileName);
            if (resource == null) {
                System.err.println("MusicManager: Risorsa non trovata: " + fileName);
                return;
            }

            // Inizializzazione MediaPlayer con impostazioni persistenti
            Media media = new Media(resource.toString());
            backgroundPlayer = new MediaPlayer(media);

            // Iniezione delle preferenze caricate
            backgroundPlayer.setVolume(currentVolume);
            backgroundPlayer.setMute(isMuted);

            // Loop infinito per la colonna sonora
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundPlayer.play();

            currentMusicFile = fileName;

        } catch (Exception e) {
            System.err.println("MusicManager: Errore nel caricamento del file Media.");
        }
    }

    /**
     * GESTIONE SFX (Sound Effects): Utilizza AudioClip per la riproduzione a bassa latenza.
     * AudioClip è ottimizzato per file brevi (click, colpi) e permette sovrapposizioni.
     */
    public void playSoundEffect(String fileName) {
        // Controllo logico sullo stato del muting SFX
        if (SoundEffectisMuted) {
            return;
        }

        try {
            URL resource = getClass().getResource("/org/example/ProgettoUIDFinal/sounds/" + fileName);
            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toString());
                clip.setVolume(0.7); // Volume normalizzato per gli effetti
                clip.play();
            }
        } catch (Exception e) {
            System.err.println("MusicManager: Errore nella riproduzione dell'effetto sonoro.");
        }
    }

    // =================================================================================
    //  METODI DI PERSISTENZA E CONTROLLO STATO
    // =================================================================================

    /**
     * TOGGLE SFX: Inverte lo stato del muto per gli effetti e scrive su disco.
     */
    public void toggleSoundEffects() {
        SoundEffectisMuted = !SoundEffectisMuted;
        prefs.putBoolean(SFX_MUTED_KEY, SoundEffectisMuted);
    }

    /**
     * TOGGLE MUSIC: Inverte lo stato del muto per la musica, aggiorna il player
     * in tempo reale e scrive la preferenza nel registro di sistema.
     */
    public void toggleMute() {
        isMuted = !isMuted;

        if (backgroundPlayer != null) {
            backgroundPlayer.setMute(isMuted);
        }

        prefs.putBoolean(MUSIC_MUTED_KEY, isMuted);
    }

    /**
     * VOLUME CONTROL: Regola l'intensità sonora (0.0 a 1.0) e memorizza il valore.
     */
    public void setVolume(double volume) {
        this.currentVolume = volume;
        prefs.putDouble(VOLUME_KEY, volume);

        if (backgroundPlayer != null) {
            backgroundPlayer.setVolume(volume);
        }
    }

    // --- GETTERS PER LA UI (Sincronizzazione Toggle/Checkmark) ---
    public boolean isMusicMuted() { return isMuted; }
    public boolean isSfxMuted() { return SoundEffectisMuted; }
}