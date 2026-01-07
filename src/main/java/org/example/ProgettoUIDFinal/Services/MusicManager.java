package org.example.ProgettoUIDFinal.Services;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;
import java.util.prefs.Preferences;

public class MusicManager {

    private static MusicManager instance;
    private MediaPlayer backgroundPlayer;
    private String currentMusicFile = "";

    // Le preferenze saranno gestite da questa classe
    private final Preferences prefs = Preferences.userNodeForPackage(MusicManager.class);

    // CHIAVI DI SALVATAGGIO
    private static final String MUSIC_MUTED_KEY = "music.isMuted";
    private static final String SFX_MUTED_KEY = "sfx.isMuted";
    private static final String VOLUME_KEY = "music.volume";

    private boolean isMuted;
    private boolean SoundEffectisMuted;
    private double currentVolume;

    private MusicManager() {
        // --- CARICAMENTO PREFERENZE AL LANCIO ---
        // Legge le impostazioni salvate, usa il default se non esistono
        this.isMuted = prefs.getBoolean(MUSIC_MUTED_KEY, false);
        this.SoundEffectisMuted = prefs.getBoolean(SFX_MUTED_KEY, false);
        this.currentVolume = prefs.getDouble(VOLUME_KEY, 0.5);
    }

    /**
     * Implementazione del pattern Singleton.
     */
    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void playMusic(String fileName) {
        if (fileName.equals(currentMusicFile)) {
            return;
        }

        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
            backgroundPlayer.dispose();
        }

        try {
            URL resource = getClass().getResource("/org/example/ProgettoUIDFinal/sounds/" + fileName);
            if (resource == null) {
                System.err.println("Musica non trovata: " + fileName);
                return;
            }

            Media media = new Media(resource.toString());
            backgroundPlayer = new MediaPlayer(media);

            // --- APPLICA LE IMPOSTAZIONI CARICATE ---
            backgroundPlayer.setVolume(currentVolume);
            backgroundPlayer.setMute(isMuted);
            // ----------------------------------------

            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            backgroundPlayer.play();

            currentMusicFile = fileName;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playSoundEffect(String fileName) {

        if (SoundEffectisMuted) {
            return;
        }

        try {
            URL resource = getClass().getResource("/org/example/ProgettoUIDFinal/sounds/" + fileName);
            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toString());
                // Usa un volume fisso o applica il currentVolume, a tua scelta.
                clip.setVolume(0.7);
                clip.play();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- METODI PER GESTIRE IL MUTO ---

    /**
     * Attiva/Disattiva il muting degli effetti sonori e salva la preferenza.
     */
    public void toggleSoundEffects() {
        SoundEffectisMuted = !SoundEffectisMuted;
        // SALVA LA PREFERENZA
        prefs.putBoolean(SFX_MUTED_KEY, SoundEffectisMuted);

        System.out.println("Effetti sonori mutati: " + SoundEffectisMuted);
    }

    /**
     * Attiva/Disattiva il muting della musica e salva la preferenza.
     */
    public void toggleMute() {
        isMuted = !isMuted;

        if (backgroundPlayer != null) {
            backgroundPlayer.setMute(isMuted);
        }

        // SALVA LA PREFERENZA
        prefs.putBoolean(MUSIC_MUTED_KEY, isMuted);

        System.out.println("Muto attivato: " + isMuted);
    }

    // --- GETTERS STATO ---

    public boolean isMusicMuted() {
        return isMuted;
    }

    public boolean isSfxMuted(){
        return SoundEffectisMuted;
    }

    // --- GESTIONE VOLUME ---

    public void setVolume(double volume) {
        this.currentVolume = volume;

        // SALVA LA PREFERENZA
        prefs.putDouble(VOLUME_KEY, volume);

        if (backgroundPlayer != null) {
            backgroundPlayer.setVolume(volume);
        }
    }
}