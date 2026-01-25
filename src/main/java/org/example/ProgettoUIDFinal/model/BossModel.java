package org.example.ProgettoUIDFinal.model;

import javafx.beans.property.*;
import javafx.scene.image.Image;

import java.io.InputStream;
/**
 * Rappresentazione boss all'interno del gioco.
 * Contiene le informazioni visive (background e sprite) e le rispettive statistiche (lvl, hp, atk, def, vel).
 */

public class BossModel {
    private final StringProperty bossName = new SimpleStringProperty();

    private final StringProperty musicPath = new SimpleStringProperty();

    private final IntegerProperty bossHp = new SimpleIntegerProperty();
    private final IntegerProperty bossAtk = new SimpleIntegerProperty();
    private final IntegerProperty bossDef = new SimpleIntegerProperty();
    private final IntegerProperty bossVel = new SimpleIntegerProperty();

    private final ObjectProperty<Image> bossSprite = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> background = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> arena = new SimpleObjectProperty<>();

    private final String recommendedLevel;

    // costruttore
    public BossModel(String bossName, int bossHp, int bossAtk, int bossDef, int bossVel,
                     String bossSpritePath, String bgPath, String arenaPath,
                     String recommendedLevel, String musicPath) {

        this.bossName.set(bossName);
        this.bossHp.set(bossHp);
        this.bossAtk.set(bossAtk);
        this.bossDef.set(bossDef);
        this.bossVel.set(bossVel);
        this.recommendedLevel = recommendedLevel;

        // Imposta il percorso della musica
        this.musicPath.set(musicPath);

        setBossSpriteImage(bossSpritePath);
        setBackgroundImage(bgPath);
        setArenaImage(arenaPath);
    }

    // --- METODI NOME BOSS ---
    public StringProperty bossNameProperty() { return bossName; }
    public String getBossName() { return bossName.get(); }

    // --- METODI MUSICA ---
    public String getMusicPath() { return musicPath.get(); }
    // --- METODI STATISTICHE HP ---
    public int getBossHp() { return bossHp.get(); }
    // --- METODI STATISTICHE ATK ---
    public int getBossAtk() { return bossAtk.get(); }
    // --- METODI STATISTICHE DEF ---
    public int getBossDef() { return bossDef.get(); }
    // --- METODI STATISTICHE VEL ---
    public IntegerProperty bossVelProperty() { return bossVel; }
    public int getBossVel() { return bossVel.get(); }
    // --- METODI IMMAGINI (SPRITE, BG, ARENA) ---
    public ObjectProperty<Image> bossSpriteProperty() { return bossSprite; }
    public void setBossSpriteImage(String url) {
        loadImage(bossSprite, url, "Impossibile caricare avatar boss");
    }

    public ObjectProperty<Image> backgroundProperty() { return background; }
    public Image getBackground() { return background.get(); }
    public void setBackgroundImage(String url) {
        loadImage(background, url, "Impossibile caricare sfondo boss");
    }

    public ObjectProperty<Image> arenaProperty() { return arena; }
    public void setArenaImage(String url) {
        loadImage(arena, url, "Impossibile caricare arena boss");
    }

    // --- LIVELLO CONSIGLIATO ---
    public String getRecommendedLevel() { return recommendedLevel; }


    // --- HELPER CARICAMENTO IMMAGINI ---
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