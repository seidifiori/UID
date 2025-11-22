package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;

public class PlayerModel {
    private static PlayerModel instance;
    // Proprietà numeriche
    private final IntegerProperty gold = new SimpleIntegerProperty(0);
    private final IntegerProperty hp = new SimpleIntegerProperty(100);
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final ObservableSet<String> inventory = FXCollections.observableSet();

    //immagine profilo
    private final ObjectProperty<Image> avatarImage = new SimpleObjectProperty<>();


    public PlayerModel(int startGold, int startHp, int startLevel, String avatarPath) {
        this.gold.set(startGold);
        this.hp.set(startHp);
        this.level.set(startLevel);

        setAvatarImage(avatarPath);
    }

    public IntegerProperty goldProperty() { return gold; }
    public int getGold() { return gold.get(); }
    public void setGold(int amount) { this.gold.set(amount); }

    // Metodi per HP e Livello (Opzionali se servono)
    public IntegerProperty hpProperty() { return hp; }
    public IntegerProperty levelProperty() { return level; }


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

    public ObjectProperty<Image> avatarImageProperty() {
        return avatarImage;
    }

    // Metodo per cambiarla successivamente (dal ProfilePicChooser)
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