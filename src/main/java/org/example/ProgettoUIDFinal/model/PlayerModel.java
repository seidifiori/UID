package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;

public class PlayerModel {

    private final StringProperty playerName = new SimpleStringProperty();

    // --- IMMAGINI (LAYERS) ---
    // 1. BODY (Base)
    private final ObjectProperty<Image> bodyImage = new SimpleObjectProperty<>();
    // 2. HAIR
    private final ObjectProperty<Image> hairImage = new SimpleObjectProperty<>();

    // 3. HAT (Con Path salvato per logiche di salvataggio/negozio)
    private final ObjectProperty<Image> hatImage = new SimpleObjectProperty<>();
    private final StringProperty hatPath = new SimpleStringProperty();

    // 4. ARMOR (Con Path salvato)
    private final ObjectProperty<Image> armorImage = new SimpleObjectProperty<>();
    private final StringProperty armorPath = new SimpleStringProperty();

    // 5. WEAPON
    private final ObjectProperty<Image> weaponImage = new SimpleObjectProperty<>();
    // 6. SHIELD
    private final ObjectProperty<Image> shieldImage = new SimpleObjectProperty<>();

    // Avatar Icon (Profile pic)
    private final ObjectProperty<Image> avatarImage = new SimpleObjectProperty<>();

    // --- STATISTICHE ---
    private final IntegerProperty gold = new SimpleIntegerProperty();
    private final IntegerProperty hp = new SimpleIntegerProperty();
    private final IntegerProperty level = new SimpleIntegerProperty();
    private final IntegerProperty xp = new SimpleIntegerProperty();
    private final IntegerProperty atk = new SimpleIntegerProperty();
    private final IntegerProperty def = new SimpleIntegerProperty();
    private final IntegerProperty vel = new SimpleIntegerProperty();

    private final ObservableSet<String> inventory = FXCollections.observableSet();

    public PlayerModel(String name, int startGold, int startLevel) {
        this.playerName.set(name);
        this.gold.set(startGold);
        this.level.set(startLevel);
    }

    // --- GETTERS PROPERTY (IMMAGINI) ---
    public ObjectProperty<Image> bodyImageProperty() { return bodyImage; }
    public ObjectProperty<Image> hairImageProperty() { return hairImage; }
    public ObjectProperty<Image> hatImageProperty() { return hatImage; }
    public ObjectProperty<Image> armorImageProperty() { return armorImage; }
    public ObjectProperty<Image> weaponImageProperty() { return weaponImage; }
    public ObjectProperty<Image> shieldImageProperty() { return shieldImage; }
    public ObjectProperty<Image> avatarImageProperty() { return avatarImage; }

    // Getter per i Path (Stringhe)
    public StringProperty hatPathProperty() { return hatPath; }
    public StringProperty armorPathProperty() { return armorPath; }

    // --- SETTERS UNIFICATI (Usano tutti loadLayer ora) ---

    public void setBody(String url) { loadLayer(this.bodyImage, url); }
    public void setHair(String url) { loadLayer(this.hairImage, url); }
    public void setWeapon(String url) { loadLayer(this.weaponImage, url); }
    public void setShield(String url) { loadLayer(this.shieldImage, url); }

    // Hat e Armor aggiornano ANCHE la stringa del percorso
    public void setHat(String url) {
        this.hatPath.set(url);
        loadLayer(this.hatImage, url);
    }

    public void setArmor(String url) {
        this.armorPath.set(url);
        loadLayer(this.armorImage, url);
    }

    // Avatar
    public void setAvatarImage(Image img) { this.avatarImage.set(img); }

    public void setAvatarByPath(String url) {
        // Uso loadLayer anche qui per coerenza e sicurezza
        loadLayer(this.avatarImage, url);
    }

    // --- HELPER PRIVATO (IL CERVELLO DELLE OPERAZIONI) ---
    private void loadLayer(ObjectProperty<Image> property, String url) {
        if (url == null || url.isEmpty()) {
            property.set(null);
            return;
        }

        // Pulizia stringa: rimuove virgolette e converte backslash in slash
        String fixedUrl = url.replace("\"", "").replace("\\", "/").trim();

        // Aggiunge lo slash iniziale se manca
        if (!fixedUrl.startsWith("/")) {
            fixedUrl = "/" + fixedUrl;
        }

        try {
            // Tenta di caricare
            Image img = new Image(getClass().getResourceAsStream(fixedUrl));
            if (img.isError()) {
                throw new Exception("Image loading failed internally");
            }
            property.set(img);
        } catch (Exception e) {
            System.err.println("Impossibile caricare texture: " + fixedUrl + ". (Input originale: " + url + ")");
            property.set(null);
        }
    }

    // --- ALTRI GETTERS/SETTERS (STATS) ---
    public StringProperty playerNameProperty() { return playerName; }
    public void setPlayerName(String name) { this.playerName.set(name); }
    public String getPlayerName() { return playerName.get(); }

    public IntegerProperty goldProperty() { return gold; }
    public int getGold() { return gold.get(); }
    public void setGold(int amount) { this.gold.set(amount); }

    public IntegerProperty hpProperty() { return hp; }
    public int getHp() { return hp.get(); }
    public void setHp(int value) { this.hp.set(value); }

    public IntegerProperty xpProperty() { return xp; }
    public int getXp() { return xp.get(); }
    public void setXp(int value) { this.xp.set(value); }

    public IntegerProperty atkProperty() { return atk; }
    public int getAtk() { return atk.get(); }
    public void setAtk(int value) { this.atk.set(value); }

    public IntegerProperty defProperty() { return def; }
    public int getDef() { return def.get(); }
    public void setDef(int value) { this.def.set(value); }

    public IntegerProperty velProperty() { return vel; }
    public int getVel() { return vel.get(); }
    public void setVel(int value) { this.vel.set(value); }

    // Inventory
    public void addItem(String itemId) { this.inventory.add(itemId); }
    public boolean hasItem(String itemId) { return inventory.contains(itemId); }
}