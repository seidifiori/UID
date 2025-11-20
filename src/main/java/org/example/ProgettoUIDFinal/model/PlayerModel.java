package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public class PlayerModel {

    // Proprietà numeriche
    private final IntegerProperty gold = new SimpleIntegerProperty(0);
    private final IntegerProperty hp = new SimpleIntegerProperty(100);
    private final IntegerProperty level = new SimpleIntegerProperty(1);


    private final ObservableSet<String> inventory = FXCollections.observableSet();


    public PlayerModel(int startGold, int startHp, int startLevel) {
        this.gold.set(startGold);
        this.hp.set(startHp);
        this.level.set(startLevel);
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
}