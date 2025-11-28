package org.example.ProgettoUIDFinal;

import javafx.scene.Scene;
import javafx.scene.layout.Region;

public class StyleManager {
    private static StyleManager instance;
    private String currentFontSize = "-fx-font-size: 14px;"; // Default

    private StyleManager() {}

    public static StyleManager getInstance() {
        if (instance == null) {
            instance = new StyleManager();
        }
        return instance;
    }
    public String getFontSize() {
        return this.currentFontSize;
    }

    public void setFontSize(String size) {
        this.currentFontSize = size;
    }

    // Questo metodo applica lo stile a qualsiasi nodo radice gli passi
    // In StyleManager.java

    // Metodo ottimizzato per la Scena
    public void applyStyle(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            // Delega al metodo che gestisce la Region, così usa la stessa logica "sicura"
            applyStyle((Region) scene.getRoot());
        }
    }

    // Metodo per la Region (già presente, ma assicuriamoci sia robusto)
    public void applyStyle(Region root) {
        if (root != null) {
            String currentStyle = root.getStyle();
            // Evita di aggiungere "null" se non c'era stile prima
            if (currentStyle == null) currentStyle = "";

            // Rimuove eventuali vecchie definizioni di font-size per non accumulare stringhe infinite
            // (Opzionale ma consigliato: pulizia stringa)
            String cleanStyle = currentStyle.replaceAll("-fx-font-size:.*?;", "").trim();

            root.setStyle(cleanStyle + (cleanStyle.isEmpty() ? "" : "; ") + currentFontSize);
        }
    }
}