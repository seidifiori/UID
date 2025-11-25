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

    public BossModel(String bossName, int bossHp, int bossAtk, int bossDef, int bossVel, String bossSpritePath) {
        this.bossName.set(bossName);

        this.bossHp.set(bossHp);
        this.bossAtk.set(bossAtk);
        this.bossDef.set(bossDef);
        this.bossVel.set(bossVel);

        setBossSpriteImage(bossSpritePath);
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

    //mette lo sprite del boss
    public void setBossSpriteImage(String url) {
        try {
            if (url != null && !url.isEmpty()) {
                this.bossSprite.set(new Image(getClass().getResourceAsStream(url)));
            }
        } catch (Exception e) {
            System.err.println("Impossibile caricare avatar: " + url);
        }
    }
}