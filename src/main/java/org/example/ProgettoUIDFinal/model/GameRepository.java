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
    private final Map<ItemModel, Boolean> HasItem = new HashMap<>();

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

    public boolean isItemOwned(String id) {
        ItemModel item = allItems.get(id);
        if (item == null) return false;
        return HasItem.getOrDefault(item, false);
    }

    public void markItemAsOwned(String id) {
        ItemModel item = allItems.get(id);
        if (item != null) {
            HasItem.put(item, true);
        }
    }

    public String getAvatarPathByKey(String key) {
        if (characterProps == null) return null;
        String val = characterProps.getProperty(key);
        return (val != null) ? val.replace("\"", "").trim() : null;
    }

    public void changePlayerAvatar(String fullPath) {
        if (fullPath == null || player == null) return;
        // Usiamo il metodo helper che accetta la STRINGA
        player.setAvatarByPath(fullPath);

        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);
        prefs.put("saved.avatar.path", fullPath);
    }

    // ------------------------------------------------------

    private void loadData() {
        String basePath = "/org/example/ProgettoUIDFinal/";

        this.configProps = loadProperties(basePath + "config.properties");
        this.characterProps = loadProperties(basePath + "character.properties");
        Properties equipProps = loadProperties(basePath + "equipment.properties");

        // 1. CARICAMENTO DATI BASE
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

        // 2. CREAZIONE PLAYER (SOLO 4 PARAMETRI, COME VUOLE IL COSTRUTTORE)
        this.player = new PlayerModel(finalName, currentGold, startHp, startLevel);

        // 3. SETTAGGIO STATISTICHE AGGIUNTIVE
        // Le leggiamo dal file o usiamo i default
        if (characterProps != null) {
            try {
                this.player.setXp(Double.parseDouble(characterProps.getProperty("player.start.xp", "0.0")));
                this.player.setAtk(Double.parseDouble(characterProps.getProperty("player.start.atk", "0.5")));
                this.player.setDef(Double.parseDouble(characterProps.getProperty("player.start.def", "0.2")));
                this.player.setVel(Double.parseDouble(characterProps.getProperty("player.start.vel", "0.3")));
            } catch (Exception e) {
                System.err.println("Errore lettura statistiche da file, uso default.");
            }
        }

        // Listener per salvare i soldi
        this.player.goldProperty().addListener((obs, oldVal, newVal) -> {
            prefs.putInt("saved.player.gold", newVal.intValue());
        });

        // 4. CARICAMENTO AVATAR
        // Cerchiamo il default
        String defaultAvatarPath = (characterProps != null) ? characterProps.getProperty("profile.pic1") : null;
        // Cerchiamo se c'è un salvataggio
        String savedAvatar = prefs.get("saved.avatar.path", defaultAvatarPath);

        if (savedAvatar != null) {
            savedAvatar = savedAvatar.replace("\"", "").trim();
            if(savedAvatar.startsWith("@")) savedAvatar = savedAvatar.substring(1);

            // CORREZIONE ERRORE 2: Usiamo il metodo che accetta la Stringa
            this.player.setAvatarByPath(savedAvatar);
        }

        // 5. CARICAMENTO OGGETTI
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
                HasItem.put(item, false);
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