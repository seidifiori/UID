package org.example.ProgettoUIDFinal.model;

import javafx.scene.image.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.time.LocalDate;

public class GameRepository {

    private static GameRepository instance;
    private PlayerModel player;
    private BossModel boss;
    private final File saveFile = new File("user_save.json");
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private Properties configProps;
    private Properties characterProps;
    private Properties bossProps;
    private Map<String, Integer> powCounts = new HashMap<>();


    private final Map<String, ItemModel> allItems = new HashMap<>();

    // Contatore acquisti (Sessione corrente)
    private final Map<ItemModel, Integer> itemCounts = new HashMap<>();

    private GameRepository() {
        loadData();
    }

    public static GameRepository getInstance() {
        if (instance == null) {
            instance = new GameRepository();
        }
        return instance;
    }

    public PlayerModel getPlayer() { return player; }
    public BossModel getBoss() { return boss; }

    public ItemModel getItem(String id) { return allItems.get(id); }

    // --- METODI GESTIONE OGGETTI ---
    public int getPowCounts(String key) {
        return powCounts.getOrDefault(key, 0);
    }

    public void setPowCounts(String key, int level) {
        powCounts.put(key, level);
    }


    public int getItemCount(String id) {
        ItemModel item = allItems.get(id);
        if (item == null) return 0;
        return itemCounts.getOrDefault(item, 0);
    }

    public void incrementItemCount(String id) {
        ItemModel item = allItems.get(id);
        if (item != null) {
            int current = itemCounts.getOrDefault(item, 0);
            itemCounts.put(item, current + 1);
        }
    }

    // --- LA CORREZIONE È QUI ---
    public boolean isItemOwned(String id) {
        // 1. Controlla se l'hai comprato ORA (nella sessione corrente)
        boolean boughtJustNow = getItemCount(id) > 0;

        // 2. Controlla se l'hai comprato PRIMA (nel salvataggio del player)
        boolean inInventory = (player != null) && player.hasItem(id);

        // Se è vero uno dei due, l'oggetto è tuo.
        return boughtJustNow || inInventory;
    }
    // ----------------------------

    // -------------------------------------------------------------------------

    public String getAvatarPathByKey(String key) {
        if (characterProps == null) return null;
        String val = characterProps.getProperty(key);
        return (val != null) ? val.replace("\"", "").trim() : null;
    }

    public void changePlayerAvatar(String fullPath) {
        if (fullPath == null || player == null) return;
        player.setAvatarByPath(fullPath);
        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);
        prefs.put("saved.avatar.path", fullPath);
    }

    private void loadData() {
        // Percorso base
        String basePath = "/org/example/ProgettoUIDFinal/";

        this.powCounts = new HashMap<>();
        this.powCounts.put("sword", 0);
        this.powCounts.put("shield", 0);

        this.configProps = loadProperties(basePath + "config.properties");
        this.characterProps = loadProperties(basePath + "character.properties");
        this.bossProps = loadProperties(basePath + "boss.properties");
        Properties equipProps = loadProperties(basePath + "equipment.properties");

        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);

        // =============================================================
        // 1. CARICAMENTO ITEM MODEL
        // =============================================================
        for (String key : equipProps.stringPropertyNames()) {
            String[] parts = key.split("\\.");

            if (parts.length == 2) {
                String type = parts[0];
                String id = parts[1];

                if (type.equals("icon") || type.equals("name") ||
                        type.equals("atk") || type.equals("def") || type.equals("vel")) {
                    continue;
                }

                String rawLayerPath = equipProps.getProperty(key);
                String layerPath = cleanPath(rawLayerPath);

                String iconKey = "icon." + id;
                String rawIconPath = equipProps.getProperty(iconKey);
                String iconPath = (rawIconPath != null) ? cleanPath(rawIconPath) : layerPath;

                String nameKey = "name." + id;
                String rawName = equipProps.getProperty(nameKey);
                String name = (rawName != null) ? cleanPath(rawName) : "";

                String priceKey = "price." + type + "." + id;
                int price = 100;
                try {
                    price = Integer.parseInt(configProps.getProperty(priceKey, "100").trim());
                } catch (Exception e) {}

                int atk = 0, def = 0, vel = 0;

                if (type.equals("sword")) {
                    try { atk = Integer.parseInt(equipProps.getProperty("atk." + id, "0").trim()); }
                    catch (NumberFormatException e) {}
                } else if (type.equals("shield")) {
                    try { def = Integer.parseInt(equipProps.getProperty("def." + id, "0").trim()); }
                    catch (NumberFormatException e) {}
                }

                System.out.println(id + " " + name);

                ItemModel item = new ItemModel(id, type, iconPath, layerPath, price, name, atk, def, vel);
                allItems.put(id, item);
                itemCounts.put(item, 0);
            }
        }

        this.player = createPlayerFromProperties(configProps, prefs);
        this.boss = createBossFromProperties(bossProps);

        // Carica il salvataggio DOPO aver creato il player
        loadGameFromJSON();
    }

    private String cleanPath(String raw) {
        if (raw == null) return "";
        return raw.replace("\"", "").trim();
    }

    private PlayerModel createPlayerFromProperties(Properties configProps, Preferences prefs) {
        String rawName = (characterProps != null) ? characterProps.getProperty("player.name", "monogat.ari") : "monogat.ari";
        String finalName = rawName.replace("\"", "").trim();

        int defaultGold = 1000;
        try {
            String rawGold = configProps.getProperty("player.start.gold", "1000").trim();
            defaultGold = Integer.parseInt(rawGold);
        } catch (NumberFormatException e) { }

        int currentGold = prefs.getInt("saved.player.gold", defaultGold);

        int level = 1;
        try {
            level = Integer.parseInt(configProps.getProperty("player.start.level", "1"));
        } catch (Exception e) {}

        PlayerModel newPlayer = new PlayerModel(finalName, currentGold, level);

        int hp = 100, xp = 1, atk = 1, def = 1, vel = 1;
        if (characterProps != null) {
            try {
                xp = Integer.parseInt(characterProps.getProperty("player.xp", "1").trim());
                hp = Integer.parseInt(characterProps.getProperty("player.hp", "100").trim());
                atk = Integer.parseInt(characterProps.getProperty("player.atk", "1").trim());
                def = Integer.parseInt(characterProps.getProperty("player.def", "1").trim());
                vel = Integer.parseInt(characterProps.getProperty("player.vel", "1").trim());
            } catch (Exception e) {
                System.err.println("Errore lettura statistiche character. Uso default.");
            }
        }

        newPlayer.setHp(hp);
        newPlayer.setXp(xp);
        newPlayer.setAtk(atk);
        newPlayer.setDef(def);
        newPlayer.setVel(vel);

        Properties source = (characterProps != null && characterProps.containsKey("char.model")) ? characterProps : configProps;

        newPlayer.setBody(cleanPath(source.getProperty("char.model")));
        newPlayer.setHair(cleanPath(source.getProperty("char.hair")));
        newPlayer.setHairIcon(cleanPath(source.getProperty("icon.hair")));
        newPlayer.setHat(cleanPath(source.getProperty("char.hat")));
        newPlayer.setHatIcon(cleanPath(source.getProperty("icon.hat")));
        newPlayer.setArmor(cleanPath(source.getProperty("char.dres")));
        newPlayer.setArmorIcon(cleanPath(source.getProperty("icon.dres")));
        newPlayer.setSword(cleanPath(source.getProperty("char.sword")));
        newPlayer.setSwordIcon(cleanPath(source.getProperty("icon.sword")));
        newPlayer.setShield(cleanPath(source.getProperty("char.shield")));
        newPlayer.setShieldIcon(cleanPath(source.getProperty("icon.shield")));

        String defaultAvatarPath = (characterProps != null) ? characterProps.getProperty("profile.pic1") : null;
        String savedAvatar = prefs.get("saved.avatar.path", defaultAvatarPath);

        if (savedAvatar != null) {
            savedAvatar = savedAvatar.replace("\"", "").trim();
            if(savedAvatar.startsWith("@")) savedAvatar = savedAvatar.substring(1);
            newPlayer.setAvatarByPath(savedAvatar);
        }

        newPlayer.goldProperty().addListener((obs, oldVal, newVal) -> {
            prefs.putInt("saved.player.gold", newVal.intValue());
        });

        return newPlayer;
    }

    private BossModel createBossFromProperties(Properties bossProps) {
        String bossName = "Boss Default";
        String bossSprite = null;
        int hp = 500, atk = 20, def = 10, vel = 5;

        if (bossProps != null) {
            bossName = bossProps.getProperty("boss.name", bossName).replace("\"", "").trim();
            bossSprite = bossProps.getProperty("boss.sprite");
            if (bossSprite != null) bossSprite = bossSprite.replace("\"", "").trim();

            try {
                hp = Integer.parseInt(bossProps.getProperty("boss.hp").trim());
                atk = Integer.parseInt(bossProps.getProperty("boss.atk").trim());
                def = Integer.parseInt(bossProps.getProperty("boss.def").trim());
                vel = Integer.parseInt(bossProps.getProperty("boss.vel").trim());
            } catch (Exception e) { System.err.println("Errore parsing boss stats, uso default."); }
        }

        return new BossModel(bossName, hp, atk, def, vel, bossSprite);
    }

    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(fileName)) {
            if (input != null) props.load(input);
            else System.err.println("⚠ File mancante: " + fileName);
        } catch (IOException ex) { ex.printStackTrace(); }
        return props;
    }

    // --- SALVATAGGIO PULITO E CORRETTO ---
    public void saveGameToJSON() {
        if (player == null) return;

        try {
            PlayerSaveData data = new PlayerSaveData();

            // 1. Dati Anagrafici
            data.setPlayerName(player.getPlayerName());
            data.setSaveDate(LocalDateTime.now().toString());
            data.setLastDailyDate(LocalDate.now().toString());

            // 2. Oggetti Posseduti (COPIA LA LISTA DAL PLAYER)
            data.setOwnedItems(new ArrayList<>(player.getOwnedItems()));

            // 3. Statistiche
            data.setGold(player.getGold());
            data.setLevel(player.getLevel());
            data.setXp(player.getXp());
            data.setHp(player.getHp());
            data.setAtk(player.getAtk());
            data.setDef(player.getDef());
            data.setVel(player.getVel());
            data.setDaysNumber(player.getDaysNumber());
            data.setTaskCompleted(player.getTaskCompleted());

            // 4. Percorsi Visivi
            data.setHatPath(player.hatPathProperty().get());
            data.setArmorPath(player.armorPathProperty().get());
            data.setHairPath(player.hairPathProperty().get());
            data.setSwordPath(player.swordPathProperty().get());
            data.setShieldPath(player.shieldPathProperty().get());

            data.setHatIconPath(player.hatIconPathProperty().get());
            data.setArmorIconPath(player.armorIconPathProperty().get());
            data.setHairIconPath(player.hairIconPathProperty().get());
            data.setSwordIconPath(player.swordIconPathProperty().get());
            data.setShieldIconPath(player.shieldIconPathProperty().get());

            data.setPowCounts(new HashMap<>(this.powCounts));
            // 5. Daily Tasks
            data.setCompletedDailyTasks(new ArrayList<>(player.getCompletedDailyTasksSet()));

            // SCRITTURA SU FILE (Una volta sola)
            objectMapper.writeValue(saveFile, data);
            System.out.println("Salvataggio completato. Days: " + data.getDaysNumber() + ", Owned Items: " + data.getOwnedItems().size());

        } catch (IOException e) {
            System.err.println("Errore durante il salvataggio JSON: " + e.getMessage());
        }
    }

    public void loadGameFromJSON() {
        if (!saveFile.exists()) {
            System.out.println("ℹ️ Nessun salvataggio JSON trovato. Carico default.");
            return;
        }

        try {
            PlayerSaveData data = objectMapper.readValue(saveFile, PlayerSaveData.class);

            if (this.player != null) {
                // --- CARICAMENTO STATISTICHE ---
                this.player.setPlayerName(data.getPlayerName());
                this.player.setGold(data.getGold());
                this.player.setLevel(data.getLevel());
                this.player.setXp(data.getXp());
                this.player.setHp(data.getHp());
                this.player.setAtk(data.getAtk());
                this.player.setDef(data.getDef());
                this.player.setVel(data.getVel());

                int savedDays = data.getDaysNumber();
                this.player.setDaysNumber(savedDays);

                int savedTaskCompleted = data.getTaskCompleted();
                this.player.setTaskCompleted(savedTaskCompleted);

                //caricamento layer
                if (data.getHatPath() != null) this.player.setHat(data.getHatPath());
                if (data.getArmorPath() != null) this.player.setArmor(data.getArmorPath());
                if (data.getHairPath() != null) this.player.setHair(data.getHairPath());
                if (data.getSwordPath() != null) this.player.setSword(data.getSwordPath());
                if (data.getShieldPath() != null) this.player.setShield(data.getShieldPath());

                if (data.getHatIconPath() != null) this.player.setHatIcon(data.getHatIconPath());
                if (data.getArmorIconPath() != null) this.player.setArmorIcon(data.getArmorIconPath());
                if (data.getHairIconPath() != null) this.player.setHairIcon(data.getHairIconPath());
                if (data.getSwordIconPath() != null) this.player.setSwordIcon(data.getSwordIconPath());
                if (data.getShieldIconPath() != null) this.player.setShieldIcon(data.getShieldIconPath());

                // --- CARICAMENTO OGGETTI POSSEDUTI ---
                player.getOwnedItems().clear();
                if (data.getOwnedItems() != null) {
                    player.getOwnedItems().addAll(data.getOwnedItems());
                }

                // =============================================================
                // NUOVO: CARICAMENTO LIVELLI PROGRESSIVI (powCounts)
                // =============================================================
                if (data.getPowCounts() != null) {
                    this.powCounts = new HashMap<>(data.getPowCounts());
                }
                // =============================================================

                // --- LOGICA DAILY TASKS E CONTATORE GIORNI ---
                String todayDate = LocalDate.now().toString();
                String savedDate = data.getLastDailyDate();

                if (savedDate != null) {
                    if (savedDate.equals(todayDate)) {
                        player.setCompletedDailyTasks(data.getCompletedDailyTasks());
                    } else {
                        player.resetDailyTasks();
                        try {
                            LocalDate lastDate = LocalDate.parse(savedDate);
                            LocalDate currentDate = LocalDate.now();
                            if (lastDate.isBefore(currentDate)) {
                                int currentDays = player.getDaysNumber();
                                player.setDaysNumber(currentDays + 1);
                                System.out.println("Nuovo giorno! Giorno " + player.getDaysNumber());
                            }
                        } catch (Exception e) {
                            System.err.println("Errore nel parsing delle date: " + e.getMessage());
                        }
                    }
                } else {
                    player.resetDailyTasks();
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Errore caricamento JSON: " + e.getMessage());
        }
    }
}