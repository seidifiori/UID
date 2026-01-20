package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.image.Image;
import org.example.ProgettoUIDFinal.Services.MusicManager;

import java.io.InputStream;
import java.util.List;

/**
 * Modello principale che rappresenta lo stato del Giocatore.
 * Utilizza le Properties di JavaFX per permettere all'interfaccia grafica
 * di aggiornarsi automaticamente quando i dati cambiano.
 */
public class PlayerModel {

    // --- DATI FONDAMENTALI ---
    private final StringProperty playerName = new SimpleStringProperty();
    private final BooleanProperty isMale = new SimpleBooleanProperty(false); // false = Femmina, true = Maschio
    private final BooleanProperty isDefeated = new SimpleBooleanProperty(false); // Stato sconfitta boss

    // Gestione visibilità (es. nascondere capelli con elmo integrale)
    private final BooleanProperty isHairVisible = new SimpleBooleanProperty(true);

    // --- COLLEZIONI (Inventario e Task) ---
    // ObservableSet notifica automaticamente la UI quando un elemento viene aggiunto
    private final ObservableSet<String> ownedItems = FXCollections.observableSet();
    private final ObservableSet<String> completedDailyTasks = FXCollections.observableSet();

    // --- LIVELLI GRAFICI (Sprite del personaggio) ---
    // Ogni parte del corpo ha un percorso file (String) e l'immagine caricata (Image)

    private final ObjectProperty<Image> bodyImage = new SimpleObjectProperty<>();
    private final StringProperty bodyPath = new SimpleStringProperty();

    private final ObjectProperty<Image> hairImage = new SimpleObjectProperty<>();
    private final StringProperty hairPath = new SimpleStringProperty();
    private final StringProperty hairName = new SimpleStringProperty("Nessuna acconciatura");

    private final ObjectProperty<Image> hatImage = new SimpleObjectProperty<>();
    private final StringProperty hatPath = new SimpleStringProperty();
    private final StringProperty hatName = new SimpleStringProperty("Nessun cappello");

    private final ObjectProperty<Image> armorImage = new SimpleObjectProperty<>();
    private final StringProperty armorPath = new SimpleStringProperty();
    private final StringProperty armorName = new SimpleStringProperty("Nessuna armatura");

    private final ObjectProperty<Image> swordImage = new SimpleObjectProperty<>();
    private final StringProperty swordPath = new SimpleStringProperty();
    private final StringProperty swordName = new SimpleStringProperty("Nessuna spada");

    private final ObjectProperty<Image> shieldImage = new SimpleObjectProperty<>();
    private final StringProperty shieldPath = new SimpleStringProperty();
    private final StringProperty shieldName = new SimpleStringProperty("Nessuno scudo");

    // --- ICONE INVENTARIO ---
    // Mostrate negli slot dell'equipaggiamento

    private final ObjectProperty<Image> hairIcon = new SimpleObjectProperty<>();
    private final StringProperty hairIconPath = new SimpleStringProperty();

    private final ObjectProperty<Image> hatIcon = new SimpleObjectProperty<>();
    private final StringProperty hatIconPath = new SimpleStringProperty();

    private final ObjectProperty<Image> armorIcon = new SimpleObjectProperty<>();
    private final StringProperty armorIconPath = new SimpleStringProperty();

    private final ObjectProperty<Image> swordIcon = new SimpleObjectProperty<>();
    private final StringProperty swordIconPath = new SimpleStringProperty();

    private final ObjectProperty<Image> shieldIcon = new SimpleObjectProperty<>();
    private final StringProperty shieldIconPath = new SimpleStringProperty();

    // Immagine del profilo
    private final ObjectProperty<Image> avatarImage = new SimpleObjectProperty<>();
    private final StringProperty avatarPath = new SimpleStringProperty();

    // Banner del profilo
    private final ObjectProperty<Image> bannerImage = new SimpleObjectProperty<>();
    private final StringProperty bannerPath = new SimpleStringProperty();

    // Background
    private final ObjectProperty<Image> backgroundImage = new SimpleObjectProperty<>();
    private final StringProperty backgroundlayerPath = new SimpleStringProperty();

    // --- STATISTICHE DI GIOCO ---
    private final IntegerProperty gold = new SimpleIntegerProperty();
    private final IntegerProperty level = new SimpleIntegerProperty();
    private final IntegerProperty xp = new SimpleIntegerProperty();
    private final IntegerProperty hp = new SimpleIntegerProperty();

    private final IntegerProperty atk = new SimpleIntegerProperty();
    private final IntegerProperty def = new SimpleIntegerProperty();
    private final IntegerProperty vel = new SimpleIntegerProperty();

    private final IntegerProperty daysNumber = new SimpleIntegerProperty();
    private final IntegerProperty taskCompleted = new SimpleIntegerProperty();

    /**
     * Costruttore: Inizializza il giocatore con i valori di base.
     */
    public PlayerModel(String name, int startGold, int startLevel) {
        this.playerName.set(name);
        this.gold.set(startGold);
        this.level.set(startLevel);
        this.daysNumber.set(1);
        this.taskCompleted.set(0);
    }


    //  METODI DI EQUIPAGGIAMENTO
    //  Aggiornano sia il percorso (per il salvataggio) che l'immagine (per la vista).

    public void setBody(String url) {
        this.bodyPath.set(url);
        loadImage(this.bodyImage, url);
    }

    /**
     * Imposta il cappello. Include logica per nascondere i capelli se l'elmo è integrale.
     */
    public void setHat(String url) {
        this.hatPath.set(url);
        loadImage(this.hatImage, url);

        if (url == null) {
            this.isHairVisible.set(true);
        } else {
            // Controlla se l'elmo copre tutta la testa in base al nome del file
            boolean isFullHelmet = url.contains("Sprite-female-helmet-iron") ||
                    url.contains("Sprite-female-helmet-gold.png") ||
                    url.contains("Sprite-female-hood") ||
                    url.contains("Sprite-male-helmet-iron") ||
                    url.contains("Sprite-male-helmet-gold.png") ||
                    url.contains("Sprite-male-hood");

            this.isHairVisible.set(!isFullHelmet);
        }
    }

    public void setArmor(String url) {
        this.armorPath.set(url);
        loadImage(this.armorImage, url);
    }

    public void setHair(String url) {
        this.hairPath.set(url);
        loadImage(this.hairImage, url);
    }

    public void setSword(String url) {
        this.swordPath.set(url);
        loadImage(this.swordImage, url);
    }

    public void setShield(String url) {
        this.shieldPath.set(url);
        loadImage(this.shieldImage, url);
    }

    // --- Setters per le Icone ---
    public void setHairIcon(String url){ this.hairIconPath.set(url); loadImage(this.hairIcon, url); }
    public void setHatIcon(String url) { this.hatIconPath.set(url); loadImage(this.hatIcon, url); }
    public void setArmorIcon(String url) { this.armorIconPath.set(url); loadImage(this.armorIcon, url); }
    public void setSwordIcon(String url) { this.swordIconPath.set(url); loadImage(this.swordIcon, url); }
    public void setShieldIcon(String url) { this.shieldIconPath.set(url); loadImage(this.shieldIcon, url); }

    public void setAvatarByPath(String url) {
        this.avatarPath.set(url); // Ora salviamo anche il percorso!
        loadImage(this.avatarImage, url);
    }

    private final StringProperty backgroundPath = new SimpleStringProperty("");

    public ObjectProperty<Image> backgroundImageProperty() { return backgroundImage; }


    public void setBannerPath(String url) {
        this.bannerPath.set(url);
        loadImage(this.bannerImage, url);
    }

    public String getAvatarPath() { return avatarPath.get(); }
    public String getBannerPath() { return bannerPath.get(); }
    public String getBackgroundPath() { return backgroundPath.get(); }
    public void setBackgroundPath(String path) {
        this.backgroundPath.set(path);
        loadImage(this.backgroundImage, path); // Usa il tuo metodo helper interno
    }


    //  HELPER PRIVATI

    /**
     * Carica un'immagine in memoria gestendo errori e percorsi nulli.
     */
    private void loadImage(ObjectProperty<Image> property, String url) {
        if (url == null || url.isEmpty()) {
            property.set(null);
            return;
        }

        // Pulizia della stringa percorso
        String fixedUrl = url.replace("\"", "").replace("\\", "/").trim();
        if (!fixedUrl.startsWith("/")) {
            fixedUrl = "/" + fixedUrl;
        }

        try {
            InputStream stream = getClass().getResourceAsStream(fixedUrl);
            if (stream != null) {
                property.set(new Image(stream));
            } else {
                property.set(null);
            }
        } catch (Exception e) {
            property.set(null);
        }
    }

    private String cleanName(String input) {
        if (input == null) return "";
        return input.replace("\"", "").trim();
    }


    //  LOGICA DI GIOCO (XP, Gender, Tasks)

    public void increaseXp(int val) {
        int currentXp = this.xp.get() + val;
        int maxExp = 100;

        while (currentXp >= maxExp) {
            currentXp = currentXp - maxExp;
            levelUp();
        }
        this.xp.set(currentXp);
    }

    private void levelUp() {
        MusicManager.getInstance().playSoundEffect("level_up.mp3");
        this.level.set(this.level.get() + 1);

        // Incremento statistiche al level up
        this.atk.set(this.atk.get() + 1);
        this.def.set(this.def.get() + 1);
        this.vel.set(this.vel.get() + 1);
        System.out.println("LEVEL UP! Nuovo Livello: " + this.level.get());
    }

    /**
     * Inverte il sesso del personaggio e aggiorna dinamicamente
     * tutti gli sprite equipaggiati (sostituendo "female" con "male").
     */
    public void toggleGender() {
        isMale.set(!isMale.get());

        updatePathForGender(bodyPath, bodyImage);
        updatePathForGender(hatPath, hatImage);
        updatePathForGender(armorPath, armorImage);
        updatePathForGender(hairPath, hairImage);
        updatePathForGender(swordPath, swordImage);
        updatePathForGender(shieldPath, shieldImage);
    }

    private void updatePathForGender(StringProperty pathProp, ObjectProperty<Image> imgProp) {
        String current = pathProp.get();
        if (current == null || current.isEmpty()) return;

        String newVal = current;
        if (isMale.get()) {
            newVal = current.replace("female", "male");
        } else {
            newVal = current.replace("male", "female");
        }

        if (!newVal.equals(current)) {
            pathProp.set(newVal);
            loadImage(imgProp, newVal);
        }
    }

    // --- Gestione Daily Tasks ---
    public boolean isTaskCompleted(String taskId) { return completedDailyTasks.contains(taskId); }
    public void completeTask(String taskId) { completedDailyTasks.add(taskId); }
    public ObservableSet<String> getCompletedDailyTasksSet() { return completedDailyTasks; }

    public void setCompletedDailyTasks(List<String> tasks) {
        this.completedDailyTasks.clear();
        if (tasks != null) this.completedDailyTasks.addAll(tasks);
    }
    public void resetDailyTasks() { this.completedDailyTasks.clear(); }

    // --- Gestione Inventario ---
    public ObservableSet<String> getOwnedItems() { return ownedItems; }
    public void addOwnedItem(String itemId) { ownedItems.add(itemId); }
    public boolean hasItem(String itemId) { return ownedItems.contains(itemId); }


    //  GETTERS & SETTERS (Properties)

    // Property Accessors (Per binding UI)
    public ObjectProperty<Image> bodyImageProperty() { return bodyImage; }
    public ObjectProperty<Image> hairImageProperty() { return hairImage; }
    public ObjectProperty<Image> hatImageProperty() { return hatImage; }
    public ObjectProperty<Image> armorImageProperty() { return armorImage; }
    public ObjectProperty<Image> swordImageProperty() { return swordImage; }
    public ObjectProperty<Image> shieldImageProperty() { return shieldImage; }
    public ObjectProperty<Image> avatarImageProperty() { return avatarImage; }
    public ObjectProperty<Image> bannerImageProperty() { return bannerImage; }

    public BooleanProperty isHairVisibleProperty() { return isHairVisible; }
    public BooleanProperty isMaleProperty() { return isMale; }
    public BooleanProperty isDefeatedProperty() { return isDefeated; }

    public ObjectProperty<Image> hairIconProperty() { return hairIcon; }
    public ObjectProperty<Image> hatIconProperty() { return hatIcon; }
    public ObjectProperty<Image> armorIconProperty() { return armorIcon; }
    public ObjectProperty<Image> swordIconProperty() { return swordIcon; }
    public ObjectProperty<Image> shieldIconProperty() { return shieldIcon; }

    public StringProperty bodyPathProperty() { return bodyPath; }
    public StringProperty hatPathProperty() { return hatPath; }
    public StringProperty armorPathProperty() { return armorPath; }
    public StringProperty hairPathProperty() { return hairPath; }
    public StringProperty swordPathProperty() { return swordPath; }
    public StringProperty shieldPathProperty() { return shieldPath; }

    public StringProperty hatIconPathProperty() { return hatIconPath; }
    public StringProperty armorIconPathProperty() { return armorIconPath; }
    public StringProperty hairIconPathProperty() { return hairIconPath; }
    public StringProperty swordIconPathProperty() { return swordIconPath; }
    public StringProperty shieldIconPathProperty() { return shieldIconPath; }

    public StringProperty hairNameProperty() { return hairName; }
    public StringProperty hatNameProperty() { return hatName; }
    public StringProperty armorNameProperty() { return armorName; }
    public StringProperty swordNameProperty() { return swordName; }
    public StringProperty shieldNameProperty() { return shieldName; }

    public StringProperty playerNameProperty() { return playerName; }
    public IntegerProperty goldProperty() { return gold; }
    public IntegerProperty hpProperty() { return hp; }
    public IntegerProperty xpProperty() { return xp; }
    public IntegerProperty atkProperty() { return atk; }
    public IntegerProperty defProperty() { return def; }
    public IntegerProperty velProperty() { return vel; }
    public IntegerProperty levelProperty() { return level; }
    public IntegerProperty daysNumberProperty() { return daysNumber; }
    public IntegerProperty taskCompletedProperty() { return taskCompleted; }

    // Value Getters (Per logica interna)

    public String getHair() { return hairPath.get(); }
    public String getHat() { return hatPath.get(); }
    public String getArmor() { return armorPath.get(); }
    public String getSword() { return swordPath.get(); }
    public String getShield() { return shieldPath.get(); }

    public String getHairIcon() { return hairIconPath.get(); }
    public String getHatIcon() { return hatIconPath.get(); }
    public String getArmorIcon() { return armorIconPath.get(); }
    public String getSwordIcon() { return swordIconPath.get(); }
    public String getShieldIcon() { return shieldIconPath.get(); }

    public String getHairName() { return hairName.get(); }
    public String getHatName() { return hatName.get(); }
    public String getArmorName() { return armorName.get(); }
    public String getSwordName() { return swordName.get(); }
    public String getShieldName() { return shieldName.get(); }

    public String getPlayerName() { return playerName.get(); }
    public int getGold() { return gold.get(); }
    public int getHp() { return hp.get(); }
    public int getXp() { return xp.get(); }
    public int getAtk() { return atk.get(); }
    public int getDef() { return def.get(); }
    public int getVel() { return vel.get(); }
    public int getLevel() { return level.get(); }
    public int getDaysNumber() { return daysNumber.get(); }
    public int getTaskCompleted() { return taskCompleted.get(); }
    public boolean isMale() { return isMale.get(); }
    public boolean isDefeated(){return isDefeated.get();}
    public boolean isHairVisible() { return isHairVisible.get(); }

    // Value Setters
    public void setHairName(String name) { this.hairName.set(cleanName(name)); }
    public void setHatName(String name) { this.hatName.set(cleanName(name)); }
    public void setArmorName(String name) { this.armorName.set(cleanName(name)); }
    public void setSwordName(String name) { this.swordName.set(cleanName(name)); }
    public void setShieldName(String name) { this.shieldName.set(cleanName(name)); }

    public void setPlayerName(String name) { this.playerName.set(name); }
    public void setGold(int amount) { this.gold.set(amount); }
    public void setHp(int value) { this.hp.set(value); }
    public void setXp(int value) { this.xp.set(value); }
    public void setAtk(int value) { this.atk.set(value); }
    public void setDef(int value) { this.def.set(value); }
    public void setVel(int value) { this.vel.set(value); }
    public void setLevel(int value) { this.level.set(value); }
    public void setDaysNumber(int value) { this.daysNumber.set(value); }
    public void setTaskCompleted(int value) { this.taskCompleted.set(value); }
    public void setDefeated(boolean defeated) { isDefeated.set(defeated); }
}