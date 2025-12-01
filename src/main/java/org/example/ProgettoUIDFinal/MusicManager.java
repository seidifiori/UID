package org.example.ProgettoUIDFinal;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class MusicManager {
    private static MusicManager instance;
    private MediaPlayer backgroundPlayer;
    private String currentMusicFile = "";

    // --- NUOVE VARIABILI PER RICORDARE LE IMPOSTAZIONI ---
    private boolean isMuted = false;
    private boolean SoundEffectisMuted=false;
    // Default: non mutato
    private double currentVolume = 0.5;    // Default: 50%

    private MusicManager() {}

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

            // --- QUI APPLICHIAMO LE IMPOSTAZIONI SALVATE ---
            backgroundPlayer.setVolume(currentVolume);
            backgroundPlayer.setMute(isMuted);
            // -----------------------------------------------

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
                clip.setVolume(0.7);
                clip.play();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- NUOVI METODI PER GESTIRE IL MUTO ---
    public void toggleSoundEffects() { // Ho rinominato per chiarezza (opzionale)
        // 1. Invertiamo la variabile degli effetti
        SoundEffectisMuted = !SoundEffectisMuted;

        // Nota: Non serve toccare backgroundPlayer qui, perché gli AudioClip
        // sono "usa e getta". Il controllo avviene nel metodo playSoundEffect.

        System.out.println("Effetti sonori mutati: " + SoundEffectisMuted);
    }
    public void toggleMute() {
        // 1. Invertiamo la variabile salvata
        isMuted = !isMuted;

        // 2. Se c'è musica che sta suonando, aggiorniamola subito
        if (backgroundPlayer != null) {
            backgroundPlayer.setMute(isMuted);
        }

        System.out.println("Muto attivato: " + isMuted);
    }

    public boolean isMuted() {
        return isMuted;
    }
    public boolean SoundEffectisMuted(){
        return SoundEffectisMuted;
    }

    // --- GESTIONE VOLUME ---

    public void setVolume(double volume) {
        // Salviamo il volume nella variabile per il futuro
        this.currentVolume = volume;

        // Se c'è musica ora, aggiorniamola
        if (backgroundPlayer != null) {
            backgroundPlayer.setVolume(volume);
        }
    }
}