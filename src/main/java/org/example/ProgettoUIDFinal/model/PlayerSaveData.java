package org.example.ProgettoUIDFinal.model;

import java.util.List;
import java.util.ArrayList;

public class PlayerSaveData {
    // Dati base
    private String playerName;
    private String saveDate; // Per salvare la data
    private String lastDailyDate; // Esempio: "2023-11-20"
    private List<String> completedDailyTasks;
    private int daysNumber;
    private int taskCompleted;
    private List<String> ownedItems;
    private int gold;
    private int level;
    private int xp;
    private int hp;
    private int atk;
    private int def;
    private int vel;

    // Percorsi Immagini (per ricaricare l'aspetto)
    private String avatarPath;
    private String hatPath;
    private String armorPath;
    private String hairPath;

    // Inventario (opzionale, se vuoi salvare gli ID degli oggetti)


    // Costruttore vuoto (necessario per Jackson)
    public PlayerSaveData() {
        // Inizializza la lista per evitare NullPointerException
        this.completedDailyTasks = new ArrayList<>();
    }

    // Getters e Setters (necessari per Jackson)
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getLastDailyDate() { return lastDailyDate; }

    public void setLastDailyDate(String lastDailyDate) { this.lastDailyDate = lastDailyDate; }

    public List<String> getCompletedDailyTasks() { return completedDailyTasks; }
    public void setCompletedDailyTasks(List<String> completedDailyTasks) { this.completedDailyTasks = completedDailyTasks; }

    public String getSaveDate() { return saveDate; }
    public void setSaveDate(String saveDate) { this.saveDate = saveDate; }
    public int getTaskCompleted() { return taskCompleted; }
    public void setTaskCompleted(int taskCompleted) { this.taskCompleted = taskCompleted; }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    public int getAtk() { return atk; }
    public void setAtk(int atk) { this.atk = atk; }

    public int getDef() { return def; }
    public void setDef(int def) { this.def = def; }

    public int getVel() { return vel; }
    public void setVel(int vel) { this.vel = vel; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getHatPath() { return hatPath; }
    public void setHatPath(String hatPath) { this.hatPath = hatPath; }
    public String getHairPath() { return hairPath; }
    public void setHairPath(String hairPath) { this.hairPath = hairPath; }

    public String getArmorPath() { return armorPath; }
    public void setArmorPath(String armorPath) { this.armorPath = armorPath; }

    public List<String> getOwnedItems() {
        if (ownedItems == null) {
            ownedItems = new ArrayList<>(); // Evitiamo NullPointerException, per favore
        }
        return ownedItems;
    }

    public void setOwnedItems(List<String> ownedItems) {
        this.ownedItems = ownedItems;
    }

    public int getDaysNumber() { 
        return daysNumber; 
    }
    
    public void setDaysNumber(int daysNumber) { 
        this.daysNumber = daysNumber; 
    }
}