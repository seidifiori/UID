package org.example.ProgettoUIDFinal.model;

public class ItemModel {
    private final String id;       // es: "cap1", "dres1"
    private final String type;     // es: "hat", "armor"
    private final String imagePath;
    private int price;

    public ItemModel(String id, String type, String imagePath, int price) {
        this.id = id;
        this.type = type;
        this.imagePath = imagePath;
        this.price = price;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getImagePath() { return imagePath; }
    public int getPrice() { return price; }
    public void setPrice(int newPrice) {
        this.price = newPrice;
    }
}