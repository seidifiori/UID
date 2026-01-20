package org.example.ProgettoUIDFinal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * DTO (Data Transfer Object) utilizzato esclusivamente per il salvataggio su file JSON.
 * * A differenza di {@link PlayerModel}, che usa le "Properties" di JavaFX per l'interfaccia,
 * questa classe usa tipi di dato semplici (String, int, List).
 * La libreria Jackson legge questa classe per scrivere il file  e
 * la riempie quando il gioco viene caricato.
 */

public class PlayerSaveData {

    // --- DATI ANAGRAFICI E TEMPORALI ---
    private String playerName;
    private String gameEpoch;
    private String saveDate;       // Timestamp del salvataggio (es. "2023-12-01T15:30:00")
    private String lastDailyDate;  // Data dell'ultimo login (es. "2023-12-01") per reset daily tasks
    private int daysNumber;        // Giorni passati nel gioco
    private boolean flashEffectsEnabled = true;


    // --- STATISTICHE DI GIOCO ---
    private int gold;
    private int level;
    private int xp;
    private int hp;
    private int atk;
    private int def;
    private int vel;

    // --- STATI BOOLEANI ---
    private boolean isMale;     // true = Maschio, false = Femmina
    private boolean isDefeated; // true = Ha perso contro il boss corrente

    // --- INVENTARIO E PROGRESSI ---
    private List<String> completedDailyTasks; // ID delle task completate oggi
    private int taskCompleted;                // Numero totale task completate (storico)
    private List<String> ownedItems;          // ID di tutti gli oggetti comprati/posseduti
    private Map<String, Integer> powCounts;   // Livelli dei potenziamenti acquistati (es. "sword": 2)

    // --- PERCORSI GRAFICI (SPRITE CORPOREI) ---
    // Questi percorsi permettono di ricaricare l'aspetto esatto del personaggio
    private String avatarPath; // Foto profilo
    private String bannerPath; // Foto banner
    private String bodyPath;   // Corpo base
    private String hairPath;   // Capelli
    private String hatPath;    // Cappello
    private String armorPath;  // Vestito/Armatura
    private String swordPath;  // Arma
    private String shieldPath; // Scudo

    // --- PERCORSI GRAFICI (ICONE INVENTARIO) ---
    // Icone visualizzate negli slot dell'inventario
    private String hairIconPath;
    private String hatIconPath;
    private String armorIconPath;
    private String swordIconPath;
    private String shieldIconPath;

    // --- NOMI DEGLI OGGETTI EQUIPAGGIATI ---
    private String hairName;
    private String hatName;
    private String armorName;
    private String swordName;
    private String shieldName;

    // --- QUESTS ---
    private List<QuestModel> quests;


    // --- AMBIENTE ---
    private String backgroundPath; // Sfondo attuale del guardaroba/negozio
    private List<String> defeatedBossesNames = new ArrayList<>();
    /**
     * Costruttore vuoto.
     * È FONDAMENTALE per la libreria Jackson, che istanzia questa classe
     * prima di riempire i campi leggendo il JSON.
     */

    public PlayerSaveData() {
        this.completedDailyTasks = new ArrayList<>();
        this.ownedItems = new ArrayList<>();
        this.quests = new ArrayList<>();   // <--- aggiungi
    }


    // =================================================================================
    //  GETTERS & SETTERS
    //  Metodi standard per leggere e scrivere i dati.
    // =================================================================================

    // --- Anagrafica ---
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public boolean isFlashEffectsEnabled() {
        return flashEffectsEnabled;
    }

    public void setFlashEffectsEnabled(boolean flashEffectsEnabled) {
        this.flashEffectsEnabled = flashEffectsEnabled;
    }
    public String getSaveDate() { return saveDate; }
    public void setSaveDate(String saveDate) { this.saveDate = saveDate; }

    public String getGameEpoch() { return gameEpoch; }
    public void setGameEpoch(String gameEpoch) { this.gameEpoch = gameEpoch; }

    public String getLastDailyDate() { return lastDailyDate; }
    public void setLastDailyDate(String lastDailyDate) { this.lastDailyDate = lastDailyDate; }

    public int getDaysNumber() { return daysNumber; }
    public void setDaysNumber(int daysNumber) { this.daysNumber = daysNumber; }

    // --- Statistiche ---
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

    // --- Stati ---
    public boolean isMale() { return isMale; }
    public void setMale(boolean male) { isMale = male; }

    public boolean isDefeated() { return isDefeated; }
    public void setDefeated(boolean defeated) { isDefeated = defeated; }
    public List<String> getDefeatedBossesNames() { return defeatedBossesNames; }
    public void setDefeatedBossesNames(List<String> defeatedBossesNames) {
        this.defeatedBossesNames = defeatedBossesNames;
    }

    // --- Progressi e Inventario ---
    public List<String> getCompletedDailyTasks() { return completedDailyTasks; }
    public void setCompletedDailyTasks(List<String> completedDailyTasks) { this.completedDailyTasks = completedDailyTasks; }

    public int getTaskCompleted() { return taskCompleted; }
    public void setTaskCompleted(int taskCompleted) { this.taskCompleted = taskCompleted; }

    public Map<String, Integer> getPowCounts() { return powCounts; }
    public void setPowCounts(Map<String, Integer> powCounts) { this.powCounts = powCounts; }
    public List<QuestModel> getQuests() {
        return quests;
    }

    public void setQuests(List<QuestModel> quests) {
        this.quests = quests;
    }

    /**
     * Restituisce la lista degli oggetti posseduti.
     * Include un controllo di sicurezza per non restituire mai null.
     */
    public List<String> getOwnedItems() {
        if (ownedItems == null) {
            ownedItems = new ArrayList<>();
        }
        return ownedItems;
    }
    public void setOwnedItems(List<String> ownedItems) { this.ownedItems = ownedItems; }

    // --- Percorsi Grafici (Sprite) ---
    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getBannerPath() { return bannerPath; }
    public void setBannerPath(String bannerPath) { this.bannerPath = bannerPath; }

    public String getBodyPath() { return bodyPath; }
    public void setBodyPath(String bodyPath) { this.bodyPath = bodyPath; }

    public String getHatPath() { return hatPath; }
    public void setHatPath(String hatPath) { this.hatPath = hatPath; }

    public String getHairPath() { return hairPath; }
    public void setHairPath(String hairPath) { this.hairPath = hairPath; }

    public String getArmorPath() { return armorPath; }
    public void setArmorPath(String armorPath) { this.armorPath = armorPath; }

    public String getSwordPath() { return swordPath; }
    public void setSwordPath(String swordPath) { this.swordPath = swordPath; }

    public String getShieldPath() { return shieldPath; }
    public void setShieldPath(String shieldPath) { this.shieldPath = shieldPath; }

    // --- Percorsi Grafici (Icone) ---
    public String getHatIconPath() { return hatIconPath; }
    public void setHatIconPath(String hatIconPath) { this.hatIconPath = hatIconPath; }

    public String getHairIconPath() { return hairIconPath; }
    public void setHairIconPath(String hairIconPath) { this.hairIconPath = hairIconPath; }

    public String getArmorIconPath() { return armorIconPath; }
    public void setArmorIconPath(String armorIconPath) { this.armorIconPath = armorIconPath; }

    public String getSwordIconPath() { return swordIconPath; }
    public void setSwordIconPath(String swordIconPath) { this.swordIconPath = swordIconPath; }

    public String getShieldIconPath() { return shieldIconPath; }
    public void setShieldIconPath(String shieldIconPath) { this.shieldIconPath = shieldIconPath; }

    public String getHairName() { return hairName; }
    public void setHairName(String hairName) { this.hairName = hairName; }

    public String getHatName() { return hatName; }
    public void setHatName(String hatName) { this.hatName = hatName; }

    public String getArmorName() { return armorName; }
    public void setArmorName(String armorName) { this.armorName = armorName; }

    public String getSwordName() { return swordName; }
    public void setSwordName(String swordName) { this.swordName = swordName; }

    public String getShieldName() { return shieldName; }
    public void setShieldName(String shieldName) { this.shieldName = shieldName; }

    // --- Sfondo ---
    public String getBackgroundPath() { return backgroundPath; }
    public void setBackgroundPath(String backgroundPath) { this.backgroundPath = backgroundPath; }
}