package org.example.ProgettoUIDFinal.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

public class GameRepository {

    private static GameRepository instance;
    private PlayerModel player;
    private Properties characterProps;

    private static final String PREF_AVATAR_KEY = "saved.avatar.path";

    // Le tue mappe
    private final Map<String, ItemModel> allItems = new HashMap<>();
    private final Map<ItemModel, Boolean> HasItem = new HashMap<>(); // <--- La mappa che vuoi usare

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
        if (item == null) return false; // Se l'oggetto non esiste, non possiamo averlo
        return HasItem.getOrDefault(item, false);
    }

    public void markItemAsOwned(String id) {
        ItemModel item = allItems.get(id);
        if (item != null) {
            HasItem.put(item, true);
        }
    }

    private void loadData() {
        String basePath = "/org/example/ProgettoUIDFinal/";
        this.characterProps = loadProperties(basePath + "characters.properties");

        Properties configProps = loadProperties(basePath + "config.properties");
        Properties equipProps = loadProperties(basePath + "equipment.properties");
        Properties charProps = loadProperties(basePath + "characters.properties");

        int startGold = Integer.parseInt(configProps.getProperty("player.start.gold", "1000"));
        int startHp = Integer.parseInt(configProps.getProperty("player.start.hp", "100"));
        int startLevel = Integer.parseInt(configProps.getProperty("player.start.level", "1"));

        String defaultKey = "profile.pic1"; // O leggilo da configProps se preferisci
        String defaultPath = getAvatarPathByKey(defaultKey);

        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);
        String avatarToLoad = prefs.get(PREF_AVATAR_KEY, defaultPath);

        this.player = new PlayerModel(startGold, startHp, startLevel, avatarToLoad);


        for (String key : equipProps.stringPropertyNames()) {
            String[] parts = key.split("\\.");
            if (parts.length == 2) {
                String type = parts[0];
                String id = parts[1];

                String rawPath = equipProps.getProperty(key);
                String path = (rawPath != null) ? rawPath.replace("\"", "").trim() : "";

                String priceKey = "price." + type + "." + id;
                int price = Integer.parseInt(configProps.getProperty(priceKey, "100"));

                ItemModel item = new ItemModel(id, type, path, price);
                allItems.put(id, item);

                // Inizializziamo la mappa a FALSE per tutti gli oggetti caricati
                HasItem.put(item, false);
            }
        }

    }

    public String getAvatarPathByKey(String key) {
        String rawValue = characterProps.getProperty(key);

        if (rawValue == null) return null;

        // 1. Rimuove le virgolette
        String cleanPath = rawValue.replace("\"", "").trim();

        // 2. Aggiunge il percorso base del package
        return "/org/example/ProgettoUIDFinal/" + cleanPath;
    }

    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(fileName)) {
            if (input != null) props.load(input);
            else System.err.println("File non trovato: " + fileName);
        } catch (IOException ex) { ex.printStackTrace(); }
        return props;
    }

    public void changePlayerAvatar(String newPath) {
// A. Aggiorna la grafica corrente
        if (player != null) {
            player.setAvatarImage(newPath);
        }

        // B. Salva la preferenza nel sistema operativo
        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);
        prefs.put(PREF_AVATAR_KEY, newPath);

        System.out.println("Avatar salvato nelle Preferenze: " + newPath);
    }


}