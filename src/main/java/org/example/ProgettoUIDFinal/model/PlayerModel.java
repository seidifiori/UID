package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;

public class PlayerModel {

    private final StringProperty playerName = new SimpleStringProperty();

    // Proprietà numeriche
    private final IntegerProperty gold = new SimpleIntegerProperty();
    private final IntegerProperty hp = new SimpleIntegerProperty();
    private final IntegerProperty level = new SimpleIntegerProperty();

    // Statistiche (0.0 - 1.0)
    private final IntegerProperty xp = new SimpleIntegerProperty();
    private final IntegerProperty atk = new SimpleIntegerProperty();
    private final IntegerProperty def = new SimpleIntegerProperty();
    private final IntegerProperty vel = new SimpleIntegerProperty();

    private final ObservableSet<String> inventory = FXCollections.observableSet();

    // --- IMMAGINI E PERCORSI ---
    private final ObjectProperty<Image> avatarImage = new SimpleObjectProperty<>();

    private final ObjectProperty<Image> hatImage = new SimpleObjectProperty<>();
    private final StringProperty hatPath = new SimpleStringProperty();

    private final ObjectProperty<Image> armorImage = new SimpleObjectProperty<>();
    private final StringProperty armorPath = new SimpleStringProperty();


    public PlayerModel(String name, int startGold, int startLevel) {
        this.playerName.set(name);
        this.gold.set(startGold);
        this.level.set(startLevel);
    }

    // --- GETTERS E PROPERTY METHODS ---

    // Player Name
    public StringProperty playerNameProperty() { return playerName; }
    public void setPlayerName(String name) { this.playerName.set(name); }
    public String getPlayerName() { return playerName.get(); } // Utile averlo

    // Gold
    public IntegerProperty goldProperty() { return gold; }
    public int getGold() { return gold.get(); }
    public void setGold(int amount) { this.gold.set(amount); }

    // Hp & Level (Aggiungi i get se servono, ma per ora ok)
    public IntegerProperty hpProperty() { return hp; }
    public int getHp() { return hp.get(); }
    public void setHp(int value) { this.hp.set(value); }

    // XP
    public IntegerProperty xpProperty() { return xp; }
    public int getXp() { return xp.get(); }
    public void setXp(int value) { this.xp.set(value); }

    // ATK
    public IntegerProperty atkProperty() { return atk; }
    public int getAtk() { return atk.get(); }
    public void setAtk(int value) { this.atk.set(value); }

    // DEF
    public IntegerProperty defProperty() { return def; }
    public int getDef() { return def.get(); }
    public void setDef(int value) { this.def.set(value); }

    // VEL
    public IntegerProperty velProperty() { return vel; }
    public int getVel() { return vel.get(); }
    public void setVel(int value) { this.vel.set(value); }


    // Inventory
    public void addItem(String itemId) { this.inventory.add(itemId); }
    public boolean hasItem(String itemId) { return inventory.contains(itemId); }

    public ObjectProperty<Image> avatarImageProperty() { return avatarImage; }
    public void setAvatarImage(Image img) { this.avatarImage.set(img); }

    // Sostituisci il vecchio setAvatarByPath con questo:
    public void setAvatarByPath(String url) {
        if (url == null || url.isEmpty()) return;

        String fixedUrl = url;
        if (!fixedUrl.startsWith("/")) {
            fixedUrl = "/" + fixedUrl;
        }

        try {
            // Carica l'immagine usando il percorso corretto
            Image img = new Image(getClass().getResourceAsStream(fixedUrl));
            this.avatarImage.set(img);
        } catch (Exception e) {
            System.err.println("PlayerModel: Impossibile caricare avatar. Path originale: " + url + " | Path tentato: " + fixedUrl);
        }
    }

    // 2. HAT (Cappello)
    public ObjectProperty<Image> hatImageProperty() { return hatImage; }
    public StringProperty hatPathProperty() { return hatPath; }

    public void setHat(String url) {
        this.hatPath.set(url);
        if (url == null || url.isEmpty()) {
            this.hatImage.set(null);
        } else {
            try {
                this.hatImage.set(new Image(getClass().getResourceAsStream(url)));
            } catch (Exception e) {
                System.err.println("PlayerModel: Errore caricamento cappello: " + url);
                this.hatImage.set(null);
            }
        }
    }

    // 3. ARMOR (Armatura/Dress)
    public ObjectProperty<Image> armorImageProperty() { return armorImage; }
    public StringProperty armorPathProperty() { return armorPath; }

    public void setArmor(String url) {
        this.armorPath.set(url);
        if (url == null || url.isEmpty()) {
            this.armorImage.set(null);
        } else {
            try {
                this.armorImage.set(new Image(getClass().getResourceAsStream(url)));
            } catch (Exception e) {
                System.err.println("PlayerModel: Errore caricamento armatura: " + url);
                this.armorImage.set(null);
            }
        }
    }
}