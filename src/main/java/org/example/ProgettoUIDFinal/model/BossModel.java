package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.InputStream;

public class BossModel {
    private static BossModel instance;

    private final StringProperty bossName = new SimpleStringProperty();

    private final DoubleProperty bossHp = new SimpleDoubleProperty();
    private final DoubleProperty bossAtk = new SimpleDoubleProperty();
    private final DoubleProperty bossDef = new SimpleDoubleProperty();
    private final DoubleProperty bossVel = new SimpleDoubleProperty();

    private final ObjectProperty<Image> bossSprite = new SimpleObjectProperty<>();

    public BossModel(String bossName, Double bossHp, Double bossAtk, Double bossDef, Double bossVel, String bossSpritePath) {
        this.bossName.set(bossName);

        this.bossHp.set(bossHp);
        this.bossAtk.set(bossAtk);
        this.bossDef.set(bossDef);
        this.bossVel.set(bossVel);

        setBossSpriteImage(bossSpritePath);
    }

    public StringProperty bossNameProperty() { return bossName; }

    //metodi statistica hp boss
    public DoubleProperty bossHpProperty() { return bossHp; }
    public double getBossHp() { return bossHp.get(); }
    public void setBossHp(double amount) { this.bossHp.set(amount); }

    //metodi statistica atk boss
    public DoubleProperty bossAtkProperty() { return bossAtk; }
    public double getBossAtk() { return bossAtk.get(); }
    public void setBossAtk(double amount) { this.bossAtk.set(amount); }

    //metodi statistica def boss
    public DoubleProperty bossDefProperty() { return bossDef; }
    public double getBossDef() { return bossDef.get(); }
    public void setBossDef(double amount) { this.bossDef.set(amount); }

    //metodi statistica vel boss
    public DoubleProperty bossVelProperty() { return bossVel; }
    public double getBossVel() { return bossVel.get(); }
    public void setBossVel(double amount) { this.bossVel.set(amount); }

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
