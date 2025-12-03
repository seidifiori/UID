package org.example.ProgettoUIDFinal.model;

public class ItemModel {
    private final String id;       // es: "cap1", "dres1"
    private final String type;     // es: "hat", "armor"
    private final String iconPath;
    private final String layerPath;
    private int price;

    public ItemModel(String id, String type, String iconPath, String layerPath, int price) {
        this.id = id;
        this.type = type;
        this.iconPath = iconPath;
        this.layerPath = layerPath;
        this.price = price;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getIconPath() { return iconPath; }
    public String getLayerPath() { return layerPath; }
    public int getPrice() { return price; }
    public void setPrice(int newPrice) {
        this.price = newPrice;
    }
}