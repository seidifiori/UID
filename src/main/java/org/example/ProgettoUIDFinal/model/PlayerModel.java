package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;
import org.example.ProgettoUIDFinal.MusicManager;

import java.io.InputStream;

public class PlayerModel {

    private final StringProperty playerName = new SimpleStringProperty();
    private final BooleanProperty isHairVisible = new SimpleBooleanProperty(true);

    // --- IMMAGINI (LAYERS) ---
    // 1. BODY (Base)
    private final ObjectProperty<Image> bodyImage = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> hairImage = new SimpleObjectProperty<>();

    private final ObjectProperty<Image> hatImage = new SimpleObjectProperty<>();
    private final StringProperty hatPath = new SimpleStringProperty();

    private final ObjectProperty<Image> armorImage = new SimpleObjectProperty<>();
    private final StringProperty armorPath = new SimpleStringProperty();

    private final ObjectProperty<Image> swordImage = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> shieldImage = new SimpleObjectProperty<>();

    // 2. ICON3
    private final ObjectProperty<Image> hairIcon = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> hatIcon = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> armorIcon = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> swordIcon = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> shieldIcon = new SimpleObjectProperty<>();

    private final StringProperty hairName = new SimpleStringProperty("Nessuna acconciatura");
    private final StringProperty hatName = new SimpleStringProperty("Nessun elmo");
    private final StringProperty armorName = new SimpleStringProperty("Nessuna armatura");
    private final StringProperty swordName = new SimpleStringProperty("Nessuna spada");
    private final StringProperty shieldName = new SimpleStringProperty("Nessuno scudo");

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
    public ObjectProperty<Image> swordImageProperty() { return swordImage; }
    public ObjectProperty<Image> shieldImageProperty() { return shieldImage; }
    public ObjectProperty<Image> avatarImageProperty() { return avatarImage; }

    public BooleanProperty isHairVisibleProperty() { return isHairVisible; }

    public ObjectProperty<Image> hairIconProperty() { return hairIcon; }
    public ObjectProperty<Image> hatIconProperty() { return hatIcon; }
    public ObjectProperty<Image> armorIconProperty() { return armorIcon; }
    public ObjectProperty<Image> swordIconProperty() { return swordIcon; }
    public ObjectProperty<Image> shieldIconProperty() { return shieldIcon; }

    public StringProperty hairNameProperty() { return hairName; }
    public StringProperty hatNameProperty() { return hatName; }
    public StringProperty armorNameProperty() { return armorName; }
    public StringProperty swordNameProperty() { return swordName; }
    public StringProperty shieldNameProperty() { return shieldName; }

    // Getter per i Path (Stringhe)
    public StringProperty hatPathProperty() { return hatPath; }
    public StringProperty armorPathProperty() { return armorPath; }


    // --- SETTERS UNIFICATI (Usano tutti loadLayer ora) ---
    public void setBody(String url) { loadImage(this.bodyImage, url); }
    public void setHair(String url) { loadImage(this.hairImage, url); }
    public void setSword(String url) { loadImage(this.swordImage, url); }
    public void setShield(String url) { loadImage(this.shieldImage, url); }
    // In PlayerModel.java, add these fields and methods:
    private final ObservableSet<String> completedTasks = FXCollections.observableSet();

    public ObservableSet<String> getCompletedTasks() {
        return completedTasks;
    }

    public void completeTask(String taskId) {
        completedTasks.add(taskId);
    }

    public boolean isTaskCompleted(String taskId) {
        return completedTasks.contains(taskId);
    }

    public void setHairName(String name) { this.hairName.set(cleanName(name)); }
    public void setHatName(String name) { this.hatName.set(cleanName(name)); }
    public void setArmorName(String name) { this.armorName.set(cleanName(name)); }
    public void setSwordName(String name) { this.swordName.set(cleanName(name)); }
    public void setShieldName(String name) { this.shieldName.set(cleanName(name)); }

    private String cleanName(String input) {
        if (input == null) return "";
        // Rimuove le virgolette e gli spazi vuoti all'inizio/fine
        return input.replace("\"", "").trim();
    }

    // Hat e Armor aggiornano ANCHE la stringa del percorso
    public void setHat(String url) {
        this.hatPath.set(url);
        loadImage(this.hatImage, url);

        // LOGICA PER NASCONDERE I CAPELLI
        if (url == null) {
            this.isHairVisible.set(true);
        } else {
            if (url.contains("Sprite-female-helmet-iron") || url.contains("Sprite-female-helmet-gold.png") || url.contains("Sprite-female-hood")) {
                this.isHairVisible.set(false); // Nascondi
            } else {
                this.isHairVisible.set(true);  // Mostra per tutti gli altri cappelli
            }
        }
    }

    public void setArmor(String url) {
        this.armorPath.set(url);
        loadImage(this.armorImage, url);
    }

    public void setHairIcon(String url){ loadImage(this.hairIcon, url); }
    public void setHatIcon(String url) { loadImage(this.hatIcon, url); }
    public void setArmorIcon(String url) { loadImage(this.armorIcon, url); }
    public void setSwordIcon(String url) { loadImage(this.swordIcon, url); }
    public void setShieldIcon(String url) { loadImage(this.shieldIcon, url); }

    // Avatar
    public void setAvatarImage(Image img) { this.avatarImage.set(img); }

    public void setAvatarByPath(String url) {
        // Uso loadLayer anche qui per coerenza e sicurezza
        loadImage(this.avatarImage, url);
    }

    // --- HELPER PRIVATO (IL CERVELLO DELLE OPERAZIONI) ---
    private void loadImage(ObjectProperty<Image> property, String url) {
        if (url == null || url.isEmpty()) {
            property.set(null);
            return;
        }

        String fixedUrl = url.replace("\"", "").replace("\\", "/").trim();

        if (!fixedUrl.startsWith("/")) {
            fixedUrl = "/" + fixedUrl;
        }

        try {
            InputStream stream = getClass().getResourceAsStream(fixedUrl);

            if (stream != null) {
                property.set(new Image(stream));
            } else {
                // File non trovato: imposta null senza stampare errori
                property.set(null);
            }
        } catch (Exception e) {
            // Eccezione gestita silenziosamente
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
    public IntegerProperty levelProperty() { return level; }
    public int getlevel() { return level.get(); }
    public void setLevel(int value) { this.level.set(value); }

    // Inventory
    public void addItem(String itemId) { this.inventory.add(itemId); }
    public boolean hasItem(String itemId) { return inventory.contains(itemId); }

    public void increaseXp(int val) {
        int currentXp = this.xp.get() + val;
        int maxExp = 100; // La soglia per livellare (la barra piena)

        // Usiamo un while nel caso guadagni così tanta XP da salire di 2 livelli in un colpo
        while (currentXp >= maxExp) {
            currentXp = currentXp - maxExp; // Sottrae 100, facendo "tornare la barra" a zero (o all'eccesso)
            levelUp(); // Chiama il metodo per aumentare le statistiche
        }

        this.xp.set(currentXp);
    }

    /**
     * Metodo privato che gestisce l'aumento delle statistiche
     */
    private void levelUp() {
        // Aumenta il livello di 1
        MusicManager.getInstance().playSoundEffect("level_up.mp3");
        this.level.set(this.level.get() + 1);

        // Aumenta Attacco, Difesa e Velocità di 1
        this.atk.set(this.atk.get() + 1);
        this.def.set(this.def.get() + 1);
        this.vel.set(this.vel.get() + 1);

        // Se vuoi rigenerare anche la vita quando livelli, togli il commento sotto:
        // this.hp.set(100);

        System.out.println("LEVEL UP! Nuovo Livello: " + this.level.get());
    }
}

