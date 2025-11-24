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

    // --- MODIFICA 1: Ora contiamo (Integer), non controlliamo solo se esiste (Boolean) ---
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
    public BossModel getBoss() {return boss; }

    public ItemModel getItem(String id) { return allItems.get(id); }

    // --- NUOVI METODI PER CONTARE ---

    /**
     * Restituisce quante volte è stato comprato un oggetto.
     */
    public int getItemCount(String id) {
        ItemModel item = allItems.get(id);
        if (item == null) return 0;
        return itemCounts.getOrDefault(item, 0);
    }

    /**
     * Incrementa il contatore di acquisto di 1.
     * (Equivale a "Ho comprato una copia")
     */
    public void incrementItemCount(String id) {
        ItemModel item = allItems.get(id);
        if (item != null) {
            int current = itemCounts.getOrDefault(item, 0);
            itemCounts.put(item, current + 1);
            System.out.println("Oggetto " + id + " comprato " + (current + 1) + " volte.");
        }
    }

    // Manteniamo questo per retro-compatibilità, ma ora controlla se ne hai almeno 1
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
        String basePath = "/org/example/ProgettoUIDFinal/";

        this.configProps = loadProperties(basePath + "config.properties");
        this.characterProps = loadProperties(basePath + "characters.properties");
        this.bossProps = loadProperties(basePath + "boss.properties");
        Properties equipProps = loadProperties(basePath + "equipment.properties");

        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);

        this.player = createPlayerFromProperties(configProps, prefs);
        this.boss = createBossFromProperties(bossProps);

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

                // --- MODIFICA 2: Inizializziamo il contatore a 0 ---
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

        // 3. Parsing Statistiche (Con gestione errori per evitare crash se il file manca)
        int hp = 100;
        int level = 1;
        try {
            hp = Integer.parseInt(configProps.getProperty("player.start.hp", "100"));
            level = Integer.parseInt(configProps.getProperty("player.start.level", "1"));
        } catch (Exception e) {}

        double xp = 0.0;
        double atk = 0.0;
        double def = 0.0;
        double vel = 0.0;

        if (characterProps != null) {
            try {
                xp = Double.parseDouble(characterProps.getProperty("player.xp").trim());
                atk = Double.parseDouble(characterProps.getProperty("player.atk").trim());
                def = Double.parseDouble(characterProps.getProperty("player.def").trim());
                vel = Double.parseDouble(characterProps.getProperty("player.vel").trim());
            } catch (Exception e) {
                System.err.println("Errore lettura statistiche character (xp/atk/def/vel). Uso default.");
            }
        }

        // 4. Parsing Avatar
        String defaultAvatarPath = (characterProps != null) ? characterProps.getProperty("profile.pic1") : null;
        String savedAvatar = prefs.get("saved.avatar.path", defaultAvatarPath);

        if (savedAvatar != null) {
            savedAvatar = savedAvatar.replace("\"", "").trim();
            if(savedAvatar.startsWith("@")) savedAvatar = savedAvatar.substring(1);
        }

        PlayerModel newPlayer = new PlayerModel(finalName, currentGold, hp, level);

        // Imposto i valori extra tramite setter se non sono nel costruttore base
        newPlayer.setXp(xp);
        newPlayer.setAtk(atk);
        newPlayer.setDef(def);
        newPlayer.setVel(vel);
        if (savedAvatar != null) newPlayer.setAvatarByPath(savedAvatar);

        // 6. AGGIUNTA LISTENER (Ora newPlayer esiste!)
        newPlayer.goldProperty().addListener((obs, oldVal, newVal) -> {
            prefs.putInt("saved.player.gold", newVal.intValue());
        });

        return newPlayer;
    }

    private BossModel createBossFromProperties(Properties bossProps) {
        String bossName = "Boss Default";
        String bossSprite = null;
        double hp = 500, atk = 20, def = 10, vel = 5;

        if (bossProps != null) {
            bossName = bossProps.getProperty("boss.name", bossName).replace("\"", "").trim();
            bossSprite = bossProps.getProperty("boss.sprite");
            if (bossSprite != null) bossSprite = bossSprite.replace("\"", "").trim();

            try {
                hp = Double.parseDouble(bossProps.getProperty("boss.hp").trim());
                atk = Double.parseDouble(bossProps.getProperty("boss.atk").trim());
                def = Double.parseDouble(bossProps.getProperty("boss.def").trim());
                vel = Double.parseDouble(bossProps.getProperty("boss.vel").trim());
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