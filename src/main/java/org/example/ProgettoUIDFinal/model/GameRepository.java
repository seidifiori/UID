package org.example.ProgettoUIDFinal.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GameRepository {

    private static GameRepository instance;
    private PlayerModel player;

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
        Properties configProps = loadProperties(basePath + "config.properties");
        Properties equipProps = loadProperties(basePath + "equipment.properties");

        int startGold = Integer.parseInt(configProps.getProperty("player.start.gold", "1000"));
        int startHp = Integer.parseInt(configProps.getProperty("player.start.hp", "100"));
        int startLevel = Integer.parseInt(configProps.getProperty("player.start.level", "1"));

        this.player = new PlayerModel(startGold, startHp, startLevel);

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

    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(fileName)) {
            if (input != null) props.load(input);
            else System.err.println("File non trovato: " + fileName);
        } catch (IOException ex) { ex.printStackTrace(); }
        return props;
    }
}