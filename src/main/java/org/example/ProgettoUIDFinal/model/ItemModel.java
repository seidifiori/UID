package org.example.ProgettoUIDFinal.model;

public class ItemModel {
    private final String id;       // es: "cap1", "dres1"
    private final String type;     // es: "hat", "armor"
    private final String iconPath;

    private final String layerPathFemale;
    private final String layerPathMale;

    private final String name;
    private int price;
    private int atk;
    private int def;
    private int vel;

    public ItemModel(String id, String type, String iconPath, String layerPathFemale, String layerPathMale, int price, String name, int atk, int def, int vel) {
        this.id = id;
        this.type = type;
        this.iconPath = iconPath;
        this.layerPathFemale = layerPathFemale;
        this.layerPathMale = layerPathMale;
        this.price = price;
        this.name = name;
        this.atk = atk;
        this.def = def;
        this.vel = vel;
    }

    //Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public String getIconPath() { return iconPath; }
    public int getPrice() { return price; }
    public String getName() { return name; }

    public String getLayerPath(boolean isMale) {
        if (isMale) {
            return (layerPathMale != null) ? layerPathMale : layerPathFemale;
        } else {
            return layerPathFemale;
        }
    }

    public int getAtk() { return atk; }
    public int getDef() { return def; }
    public int getVel() { return vel; }

    //Setters
    public void setPrice(int newPrice) {
        this.price = newPrice;
    }

    public void setAtk(int atk) { this.atk = atk; }
    public void setDef(int def) { this.def = def; }
    public void setVel(int vel) { this.vel = vel; }
}