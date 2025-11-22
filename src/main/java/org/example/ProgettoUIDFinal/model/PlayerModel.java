package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;

public class PlayerModel {
    private static PlayerModel instance;

    private final StringProperty playerName = new SimpleStringProperty();

    // Proprietà numeriche
    private final IntegerProperty gold = new SimpleIntegerProperty();
    private final IntegerProperty hp = new SimpleIntegerProperty();
    private final IntegerProperty level = new SimpleIntegerProperty();

    private final DoubleProperty xp = new SimpleDoubleProperty();
    private final DoubleProperty atk = new SimpleDoubleProperty();
    private final DoubleProperty def = new SimpleDoubleProperty();
    private final DoubleProperty vel = new SimpleDoubleProperty();

    private final ObservableSet<String> inventory = FXCollections.observableSet();

    //immagine profilo
    private final ObjectProperty<Image> avatarImage = new SimpleObjectProperty<>();


    public PlayerModel(String playerName, int startGold, int startHp, int startLevel, double startXp, double startAtk, double startDef, double startVel, String avatarPath) {
        this.playerName.set(playerName);

        this.gold.set(startGold);
        this.hp.set(startHp);
        this.level.set(startLevel);

        //statistiche
        this.xp.set(startXp);
        this.atk.set(startAtk);
        this.def.set(startDef);
        this.vel.set(startVel);

        setAvatarImage(avatarPath);
    }

    public StringProperty playerNameProperty() { return playerName; }

    public IntegerProperty goldProperty() { return gold; }
    public int getGold() { return gold.get(); }
    public void setGold(int amount) { this.gold.set(amount); }

    // Metodi per HP e Livello (Opzionali se servono)
    public IntegerProperty hpProperty() { return hp; }
    public IntegerProperty levelProperty() { return level; }

    //metodi statistica xp
    public DoubleProperty xpProperty() { return xp; }
    public double getXp() { return xp.get(); }
    public void setXp(double amount) { this.xp.set(amount); }

    //metodi statistica attacco
    public DoubleProperty atkProperty() { return atk; }
    public double getAtk() { return atk.get(); }
    public void setAtk(double amount) { this.atk.set(amount); }

    //metodi statistica difesa
    public DoubleProperty defProperty() { return def; }
    public double getDef() { return def.get(); }
    public void setDef(double amount) { this.def.set(amount); }

    //metodi statistica velocità
    public DoubleProperty velProperty() { return vel; }
    public double getVel() { return vel.get(); }
    public void setVel(double amount) { this.vel.set(amount); }

    public void addItem(String itemId) {
        this.inventory.add(itemId);
    }

    // Controlla se possiede l'oggetto
    public boolean hasItem(String itemId) {
        return inventory.contains(itemId);
    }

    public ObservableSet<String> getInventory() {
        return inventory;
    }

    public ObjectProperty<Image> avatarImageProperty() { return avatarImage; }

    //metodo mettere l'immagine
    public void setAvatarImage(String url) {
        try {
            if (url != null && !url.isEmpty()) {
                this.avatarImage.set(new Image(getClass().getResourceAsStream(url)));
            }
        } catch (Exception e) {
            System.err.println("Impossibile caricare avatar: " + url);
        }
    }
}