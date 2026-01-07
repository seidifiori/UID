package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.InputStream;

public class BossModel {
    private static BossModel instance;

    private final StringProperty bossName = new SimpleStringProperty();

    private final IntegerProperty bossHp = new SimpleIntegerProperty();
    private final IntegerProperty bossAtk = new SimpleIntegerProperty();
    private final IntegerProperty bossDef = new SimpleIntegerProperty();
    private final IntegerProperty bossVel = new SimpleIntegerProperty();

    private final ObjectProperty<Image> bossSprite = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> background = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> arena = new SimpleObjectProperty<>();

    private String recommendedLevel;

    public BossModel(String bossName, int bossHp, int bossAtk, int bossDef, int bossVel, String bossSpritePath, String bgPath, String arenaPath, String recommendedLevel) {
        this.bossName.set(bossName);
        this.bossHp.set(bossHp);
        this.bossAtk.set(bossAtk);
        this.bossDef.set(bossDef);
        this.bossVel.set(bossVel);

        this.recommendedLevel = recommendedLevel;

        setBossSpriteImage(bossSpritePath);
        setBackgroundImage(bgPath);
        setArenaImage(arenaPath);
    }

    public StringProperty bossNameProperty() { return bossName; }

    //metodi statistica hp boss
    public IntegerProperty bossHpProperty() { return bossHp; }
    public int getBossHp() { return bossHp.get(); }
    public void setBossHp(int amount) { this.bossHp.set(amount); }

    //metodi statistica atk boss
    public IntegerProperty bossAtkProperty() { return bossAtk; }
    public int getBossAtk() { return bossAtk.get(); }
    public void setBossAtk(int amount) { this.bossAtk.set(amount); }

    //metodi statistica def boss
    public IntegerProperty bossDefProperty() { return bossDef; }
    public int getBossDef() { return bossDef.get(); }
    public void setBossDef(int amount) { this.bossDef.set(amount); }

    //metodi statistica vel boss
    public IntegerProperty bossVelProperty() { return bossVel; }
    public int getBossVel() { return bossVel.get(); }
    public void setBossVel(int amount) { this.bossVel.set(amount); }

    public ObjectProperty<Image> bossSpriteProperty() { return bossSprite; }
    public Image getBossSprite() { return bossSprite.get(); }
    public void setBossSpriteImage(String url) {
        loadImage(bossSprite, url, "Impossibile caricare avatar");
    }

    public ObjectProperty<Image> backgroundProperty() { return background; }
    public Image getBackground() { return background.get(); }
    public void setBackgroundImage(String url) {
        loadImage(background, url, "Impossibile caricare sfondo boss");
    }

    public ObjectProperty<Image> arenaProperty() { return arena; }
    public Image getArena() { return arena.get(); }
    public void setArenaImage(String url) {
        loadImage(arena, url, "Impossibile caricare arena boss");
    }

    public String getRecommendedLevel() { return recommendedLevel; }
    public void setRecommendedLevel(String recommendedLevel) {this.recommendedLevel = recommendedLevel;}

    private void loadImage(ObjectProperty<Image> property, String url, String errorMsg) {
        try {
            if (url != null && !url.isEmpty()) {
                url = url.replace("\"", "").trim();
                InputStream is = getClass().getResourceAsStream(url);
                if (is != null) {
                    property.set(new Image(is));
                }
            }
        } catch (Exception e) {
            System.err.println(errorMsg + ": " + url);
        }
    }
}