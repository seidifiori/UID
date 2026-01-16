package org.example.ProgettoUIDFinal.model;

/**
 * Rappresenta un singolo oggetto all'interno del gioco.
 * Questa classe funge da modello dati per tutto ciò che è acquistabile o equipaggiabile:
 * armi, armature, cappelli, acconciature e sfondi.
 * Contiene le informazioni visive (icone e sprite), economiche (prezzo) e statistiche (atk, def, vel).
 */
public class ItemModel {

    // Dati identificativi
    private final String id;       // Identificativo univoco (es: "cap1", "sword2")
    private final String type;     // Categoria dell'oggetto (es: "hat", "armor", "background")
    private final String name;     // Nome visibile al giocatore (es: "Elmo di Ferro")

    // Dati visivi
    private final String iconPath;        // Percorso dell'icona quadrata mostrata nel Negozio e Inventario
    private final String layerPathFemale; // Percorso dello sprite per il modello femminile
    private final String layerPathMale;   // Percorso dello sprite per il modello maschile
    private final String backgroundLayerPath;
    // Dati di gioco
    private int price; // Costo in monete d'oro
    private int atk;   // Punti Attacco conferiti
    private int def;   // Punti Difesa conferiti
    private int vel;   // Punti Velocità conferiti

    /**
     * Costruttore completo per inizializzare un nuovo oggetto.
     */
    public ItemModel(String id, String type, String iconPath, String layerPathFemale, String layerPathMale,String backgroundLayerPath, int price, String name, int atk, int def, int vel) {
        this.id = id;
        this.type = type;
        this.iconPath = iconPath;
        this.layerPathFemale = layerPathFemale;
        this.layerPathMale = layerPathMale;
        this.backgroundLayerPath = backgroundLayerPath;
        this.price = price;
        this.name = name;
        this.atk = atk;
        this.def = def;
        this.vel = vel;
    }

    // --- GETTERS: Identità e Info Base ---

    public String getId() { return id; }
    public String getType() { return type; }
    public String getName() { return name; }
    public int getPrice() { return price; }

    // --- GETTERS: Risorse Grafiche ---

    /**
     * Restituisce il percorso dell'icona da mostrare nelle interfacce UI (Shop/Closet).
     */
    public String getIconPath() { return iconPath; }
    public String getBackgroundLayerPath() {
        return backgroundLayerPath;
    }
    /**
     * Restituisce il percorso grezzo per lo sprite femminile.
     * Usato anche per gli sfondi (Background), che non hanno genere.
     */
    public String getLayerPathFemale() { return layerPathFemale; }

    /**
     * Restituisce il percorso grezzo per lo sprite maschile.
     */
    public String getLayerPathMale() { return layerPathMale; }

    /**
     * Logica intelligente per ottenere lo sprite da indossare.
     * Restituisce il percorso corretto in base al sesso del personaggio.
     * Se la versione maschile non esiste (null), usa quella femminile come fallback.
     *
     * @param isMale true se il personaggio è maschio, false se femmina.
     * @return Il percorso stringa dell'immagine da caricare.
     */
    public String getLayerPath(boolean isMale) {
        if (isMale) {
            return (layerPathMale != null && !layerPathMale.isEmpty()) ? layerPathMale : layerPathFemale;
        } else {
            return layerPathFemale;
        }
    }

    // --- GETTERS: Statistiche ---

    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getVel() { return vel; }

    // --- SETTERS: Modifica valori ---
    // Nota: ID, Type e percorsi immagini sono 'final' e non modificabili dopo la creazione.

    public void setPrice(int newPrice) { this.price = newPrice; }
    public void setAtk(int atk) { this.atk = atk; }
    public void setDef(int def) { this.def = def; }
    public void setVel(int vel) { this.vel = vel; }
}