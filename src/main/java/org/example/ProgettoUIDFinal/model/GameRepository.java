package org.example.ProgettoUIDFinal.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GameRepository {

    // Singleton: una sola istanza per tutta l'app
    private static GameRepository instance;

    private PlayerModel player;
    private final Map<String, ItemModel> allItems = new HashMap<>();

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

    private void loadData() {
        Properties configProps = loadProperties("/config.properties");
        Properties equipProps = loadProperties("/equipment.properties");

        // 1. Inizializza il Player
        int startGold = Integer.parseInt(configProps.getProperty("player.start.gold", "1000"));
        int startHp = Integer.parseInt(configProps.getProperty("player.start.hp", "100"));
        int startLevel = Integer.parseInt(configProps.getProperty("player.start.level", "1"));

        this.player = new PlayerModel(startGold, startHp, startLevel);

        // 2. Carica gli oggetti (Equipment)
        // Formato atteso: type.id = "path" -> es: hat.cap1="path/img.png"
        for (String key : equipProps.stringPropertyNames()) {
            String[] parts = key.split("\\.");
            if (parts.length == 2) {
                String type = parts[0]; // "hat" o "armor"
                String id = parts[1];   // "cap1"
                String path = equipProps.getProperty(key).replace("\"", ""); // Rimuovi virgolette extra se presenti

                // Cerchiamo il prezzo nel config, altrimenti default a 50
                // Nota: nel tuo config hai "price.hat.elmo_epico", ma nel properties hai "cap1".
                // Dovrai allineare i nomi o usare un default.
                String priceKey = "price." + type + "." + id;
                int price = Integer.parseInt(configProps.getProperty(priceKey, "100"));

                ItemModel item = new ItemModel(id, type, path, price);
                allItems.put(id, item);
            }
        }
    }

    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(fileName)) {
            if (input != null) {
                props.load(input);
            } else {
                System.err.println("File non trovato: " + fileName);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return props;
    }
}