package org.example.ProgettoUIDFinal.model;

import javafx.scene.image.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

public class GameRepository {

    private static GameRepository instance;
    private PlayerModel player;
    private BossModel boss;

    private Properties configProps;
    private Properties characterProps;
    private Properties bossProps;

    private final Map<String, ItemModel> allItems = new HashMap<>();

    // Contatore acquisti
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
            // System.out.println("Oggetto " + id + " comprato " + (current + 1) + " volte.");
        }
    }

    public boolean isItemOwned(String id) {
        return getItemCount(id) > 0;
    }

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
        // Percorso base corretto
        String basePath = "/org/example/ProgettoUIDFinal/";

        this.configProps = loadProperties(basePath + "config.properties");
        this.characterProps = loadProperties(basePath + "character.properties");
        this.bossProps = loadProperties(basePath + "boss.properties");
        Properties equipProps = loadProperties(basePath + "equipment.properties");

        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);

        this.player = createPlayerFromProperties(configProps, prefs);
        this.boss = createBossFromProperties(bossProps);

        // Caricamento oggetti shop
        for (String key : equipProps.stringPropertyNames()) {
            String[] parts = key.split("\\.");
            if (parts.length == 2) {
                String type = parts[0];
                String id = parts[1];
                String rawPath = equipProps.getProperty(key);
                String path = (rawPath != null) ? rawPath.replace("\"", "").trim() : "";

                String priceKey = "price." + type + "." + id;
                String rawPrice = configProps.getProperty(priceKey, "100").trim();
                int price = 100;
                try { price = Integer.parseInt(rawPrice); } catch(Exception e){}

                ItemModel item = new ItemModel(id, type, path, price);
                allItems.put(id, item);

                itemCounts.put(item, 0);
            }
        }
    }

    private PlayerModel createPlayerFromProperties(Properties configProps, Preferences prefs) {
        // 1. Parsing dei dati base (Nome)
        String rawName = (characterProps != null) ? characterProps.getProperty("player.name", "monogat.ari") : "monogat.ari";
        String finalName = rawName.replace("\"", "").trim();

        // 2. Parsing Oro
        int defaultGold = 1000;
        try {
            String rawGold = configProps.getProperty("player.start.gold", "1000").trim();
            defaultGold = Integer.parseInt(rawGold);
        } catch (NumberFormatException e) { }

        int currentGold = prefs.getInt("saved.player.gold", defaultGold);

        // 3. Parsing Level
        int level = 1;
        try {
            level = Integer.parseInt(configProps.getProperty("player.start.level", "1"));
        } catch (Exception e) {}

        // Creazione Oggetto Player (Corretta, senza balbuzie 'PPlayer')
        PlayerModel newPlayer = new PlayerModel(finalName, currentGold, level);

        // 4. Parsing Statistiche
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

        // Imposta stats
        newPlayer.setHp(hp);
        newPlayer.setXp(xp);
        newPlayer.setAtk(atk);
        newPlayer.setDef(def);
        newPlayer.setVel(vel);

        // 5. CARICAMENTO LAYERS VISIVI (BODY, HAIR, ECC.)
        Properties source = (characterProps != null && characterProps.containsKey("char.model")) ? characterProps : configProps;

        // Helper locale per pulire le stringhe
        java.util.function.Function<String, String> clean = (k) -> {
            String v = source.getProperty(k);
            return (v != null) ? v.replace("\"", "").trim() : null;
        };

        newPlayer.setBody(clean.apply("char.model"));
        newPlayer.setHair(clean.apply("char.hair"));
        newPlayer.setHat(clean.apply("char.hat"));
        newPlayer.setArmor(clean.apply("char.dres"));
        newPlayer.setWeapon(clean.apply("char.sword"));
        newPlayer.setShield(clean.apply("char.shield"));

        // 6. Carica l'icona profilo (Avatar tondo)
        String defaultAvatarPath = (characterProps != null) ? characterProps.getProperty("profile.pic1") : null;
        String savedAvatar = prefs.get("saved.avatar.path", defaultAvatarPath);

        if (savedAvatar != null) {
            savedAvatar = savedAvatar.replace("\"", "").trim();
            if(savedAvatar.startsWith("@")) savedAvatar = savedAvatar.substring(1);
            newPlayer.setAvatarByPath(savedAvatar);
        }

        // 7. AGGIUNTA LISTENER PER SALVATAGGIO ORO
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
}