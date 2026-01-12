package org.example.ProgettoUIDFinal.Services;

import javafx.scene.Scene;
import javafx.scene.layout.Region;

/**
 * SERVIZIO DI GESTIONE STILE: Gestisce l'aspetto visivo dell'interfaccia utente (UI).
 * Implementa il Pattern SINGLETON per centralizzare le impostazioni di accessibilità,
 * come la dimensione globale del carattere, assicurando coerenza visiva tra le scene.
 */
public class StyleManager {

    // ISTANZA SINGLETON: Punto di accesso unico per la gestione dei fogli di stile
    private static StyleManager instance;

    /** * STATO DELLO STILE: Memorizza la regola CSS per la dimensione del font.
     * Valore predefinito impostato a 14px.
     */
    private String currentFontSize = "-fx-font-size: 14px;";

    // COSTRUTTORE PRIVATO: Impedisce l'istanziazione multipla
    private StyleManager() {}

    /**
     * ACCESSOR SINGLETON: Restituisce l'istanza globale del gestore stili.
     */
    public static StyleManager getInstance() {
        if (instance == null) {
            instance = new StyleManager();
        }
        return instance;
    }

    // --- GETTERS E SETTERS ---

    public String getFontSize() {
        return this.currentFontSize;
    }

    /**
     * Aggiorna il valore della dimensione del carattere da applicare.
     */
    public void setFontSize(String size) {
        this.currentFontSize = size;
    }

    // --- LOGICA DI APPLICAZIONE DELLO STILE ---

    /**
     * STYLE OVERRIDE (SCENE): Applica i parametri di stile correnti alla scena passata.
     * Recupera il nodo radice (Root Node) e ne aggiorna le proprietà CSS.
     */
    public void applyStyle(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            // Downcasting sicuro del nodo radice a Region per l'applicazione dello stile
            applyStyle((Region) scene.getRoot());
        }
    }

    /**
     * STYLE OVERRIDE (REGION): Implementa la logica di iniezione CSS dinamica.
     * Questo metodo manipola la stringa di stile inline del componente:
     * 1. Recupera lo stile esistente.
     * 2. Esegue una pulizia tramite Regex per rimuovere vecchie definizioni di font-size.
     * 3. Concatena la nuova regola per evitare conflitti o accumuli di stringhe.
     */
    public void applyStyle(Region root) {
        if (root != null) {
            String currentStyle = root.getStyle();

            // Null-safety check per lo stile corrente
            if (currentStyle == null) currentStyle = "";

            /**
             * STRING CLEANING (Regex): Utilizza un'espressione regolare per individuare
             * ed eliminare definizioni "-fx-font-size" preesistenti.
             * Questo previene la saturazione della stringa di stile e garantisce
             * che l'ultima impostazione scelta sia quella effettivamente renderizzata.
             */
            String cleanStyle = currentStyle.replaceAll("-fx-font-size:.*?;", "").trim();

            // Applicazione della nuova regola CSS inline
            root.setStyle(cleanStyle + (cleanStyle.isEmpty() ? "" : "; ") + currentFontSize);
        }
    }
}