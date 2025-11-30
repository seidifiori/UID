package org.example.ProgettoUIDFinal;

import javafx.scene.media.AudioClip; // Nota: Usiamo AudioClip per gli effetti brevi, è meglio!
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class MusicManager {
    private static MusicManager instance;
    private MediaPlayer backgroundPlayer; // Rinominato per chiarezza

    private MusicManager() {}

    public static MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    // --- GESTIONE MUSICA DI SOTTOFONDO (BGM) ---
    public void playMusic(String fileName) {
        if (backgroundPlayer != null) {
            return; // Se c'è già musica, non fare nulla
        }
        try {
            URL resource = getClass().getResource("/org/example/ProgettoUIDFinal/sounds/" + fileName);
            if (resource == null) { System.err.println("Musica non trovata: " + fileName); return; }

            Media media = new Media(resource.toString());
            backgroundPlayer = new MediaPlayer(media);
            backgroundPlayer.setVolume(0.5);
            backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop infinito
            backgroundPlayer.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- GESTIONE EFFETTI SONORI (SFX) - NUOVO METODO! ---
    public void playSoundEffect(String fileName) {
        try {
            URL resource = getClass().getResource("/org/example/ProgettoUIDFinal/sounds/" + fileName);
            if (resource == null) {
                System.err.println("SFX non trovato: " + fileName);
                return;
            }

            // Usiamo AudioClip per gli effetti: è fatto apposta per suoni brevi e sovrapposti!
            AudioClip clip = new AudioClip(resource.toString());
            clip.setVolume(0.7); // Un po' più alto della musica
            clip.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- ALTRI METODI ---
    public void setVolume(double volume) {
        if (backgroundPlayer != null) backgroundPlayer.setVolume(volume);
    }

    public void toggleMute() {
        if (backgroundPlayer != null) backgroundPlayer.setMute(!backgroundPlayer.isMute());
    }
}