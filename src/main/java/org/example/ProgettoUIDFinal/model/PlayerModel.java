package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public class PlayerModel {
    // Usiamo IntegerProperty per il binding automatico con la UI
    private final IntegerProperty gold = new SimpleIntegerProperty(0);
    private final IntegerProperty hp = new SimpleIntegerProperty(100);
    private final IntegerProperty level = new SimpleIntegerProperty(1);


    // Un Set osservabile per l'inventario (contiene gli ID degli oggetti, es: "cap1")
    private final ObservableSet<String> inventory = FXCollections.observableSet();

    // Costruttore
    public PlayerModel(int startGold, int startHp, int startLevel) {
        this.gold.set(startGold);
        this.hp.set(startHp);
        this.level.set(startLevel);
    }

    // Metodi per i soldi
    public IntegerProperty goldProperty() { return gold; }
    public int getGold() { return gold.get(); }
    public void setGold(int amount) { this.gold.set(amount); }

    public boolean spendGold(int amount) {
        if (getGold() >= amount) {
            setGold(getGold() - amount);
            return true;
        }
        return false;
    }

    // Metodi inventario
    public void addItem(String itemId) {
        inventory.add(itemId);
    }

    public boolean hasItem(String itemId) {
        return inventory.contains(itemId);
    }
}