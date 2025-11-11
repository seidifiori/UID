package org.example.uididididii;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class InventoryService {

    private static InventoryService instance;
    private final Map<String, Image> ownedItems = new HashMap<>();

    private InventoryService() {}

    public static InventoryService getInstance() {
        if (instance == null) instance = new InventoryService();
        return instance;
    }

    public void addItem(String id, Image image) {
        ownedItems.put(id, image);
    }

    public Image getItemImage(String id) {
        return ownedItems.get(id);
    }

    public boolean hasItem(String id) {
        return ownedItems.containsKey(id);
    }
}
