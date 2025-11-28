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

    public void setFontSize(String size) {
        this.currentFontSize = size;
    }

    // Questo metodo applica lo stile a qualsiasi nodo radice gli passi
    public void applyStyle(Region root) {
        if (root != null) {
            // Mantiene gli stili esistenti e aggiunge/sovrascrive il font-size
            String currentStyle = root.getStyle();
            // Un modo grezzo ma efficace per i tuoi standard
            root.setStyle(currentStyle + ";" + currentFontSize);
        }
    }

    // Overload per la Scena intera
    public void applyStyle(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            scene.getRoot().setStyle(currentFontSize);
        }
    }
}