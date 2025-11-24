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

    private Properties configProps;
    private Properties characterProps;

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
        this.characterProps = loadProperties(basePath + "character.properties");
        Properties equipProps = loadProperties(basePath + "equipment.properties");

        int defaultGold = 1000;
        try {
            String rawGold = configProps.getProperty("player.start.gold", "1000").trim();
            defaultGold = Integer.parseInt(rawGold);
        } catch (NumberFormatException e) { }

        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);
        int currentGold = prefs.getInt("saved.player.gold", defaultGold);

        int startHp = Integer.parseInt(configProps.getProperty("player.start.hp", "100").trim());
        int startLevel = Integer.parseInt(configProps.getProperty("player.start.level", "1").trim());

        String rawName = (characterProps != null) ? characterProps.getProperty("player.name", "monogat.ari") : "monogat.ari";
        String finalName = rawName.replace("\"", "").trim();

        this.player = new PlayerModel(finalName, currentGold, startHp, startLevel);

        if (characterProps != null) {
            try {
                this.player.setXp(Double.parseDouble(characterProps.getProperty("player.start.xp", "0.0")));
                this.player.setAtk(Double.parseDouble(characterProps.getProperty("player.start.atk", "0.5")));
                this.player.setDef(Double.parseDouble(characterProps.getProperty("player.start.def", "0.2")));
                this.player.setVel(Double.parseDouble(characterProps.getProperty("player.start.vel", "0.3")));
            } catch (Exception e) { }
        }

        this.player.goldProperty().addListener((obs, oldVal, newVal) -> {
            prefs.putInt("saved.player.gold", newVal.intValue());
        });

        String defaultAvatarPath = (characterProps != null) ? characterProps.getProperty("profile.pic1") : null;
        String savedAvatar = prefs.get("saved.avatar.path", defaultAvatarPath);

        if (savedAvatar != null) {
            savedAvatar = savedAvatar.replace("\"", "").trim();
            if(savedAvatar.startsWith("@")) savedAvatar = savedAvatar.substring(1);
            this.player.setAvatarByPath(savedAvatar);
        }

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

    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(fileName)) {
            if (input != null) props.load(input);
            else System.err.println("⚠ File mancante: " + fileName);
        } catch (IOException ex) { ex.printStackTrace(); }
        return props;
    }
}