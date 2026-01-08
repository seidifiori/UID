package org.example.ProgettoUIDFinal.model;

import javafx.scene.image.Image;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.Duration;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.LocalDate;

public class GameRepository {

    private static GameRepository instance;
    private PlayerModel player;
    private BossModel boss;
    private final File saveFile = new File("user_save.json");
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private Properties configProps;
    private Properties characterProps;
    private Properties bossProps;

    private Map<String, Integer> powCounts = new HashMap<>();
    private final Map<String, ItemModel> allItems = new HashMap<>();
    private final Map<ItemModel, Integer> itemCounts = new HashMap<>();

    private static final LocalDate GAME_EPOCH = LocalDate.of(2025, 12, 1);
    private static final int TOTAL_BOSS_TIERS = 3;
    private static final int DAYS_PER_BOSS = 21;
    private int currentBossTier = 0;


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
    public BossModel getBoss() { return boss; }
    public ItemModel getItem(String id) { return allItems.get(id); }

    // --- METODI GESTIONE OGGETTI ---
    public int getPowCounts(String key) {
        return powCounts.getOrDefault(key, 0);
    }
    public void setPowCounts(String key, int level) {
        powCounts.put(key, level);
    }


    public int getItemCount(String id) {
        ItemModel item = allItems.get(id);
        if (item == null) return 0;
        return itemCounts.getOrDefault(item, 0);
    }

    public void incrementItemCount(String id) {
        ItemModel item = allItems.get(id);
        if (item != null) {
            int current = itemCounts.getOrDefault(item, 0);
            itemCounts.put(item, current + 1);
        }
    }

    public boolean isItemOwned(String id) {
        boolean boughtJustNow = getItemCount(id) > 0;
        boolean inInventory = (player != null) && player.hasItem(id);
        return boughtJustNow || inInventory;
    }

    public String getAvatarPathByKey(String key) {
        if (characterProps == null) return null;
        String val = characterProps.getProperty(key);
        return (val != null) ? val.replace("\"", "").trim() : null;
    }

    public void changePlayerAvatar(String fullPath) {
        if (fullPath == null || player == null) return;
        player.setAvatarByPath(fullPath);
        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);
        prefs.put("saved.avatar.path", fullPath);
    }

    private void loadData() {
        // --- MODIFICA APPLICATA QUI: Aggiunta la cartella "properties/" ---
        String basePath = "/org/example/ProgettoUIDFinal/properties/";

        this.powCounts = new HashMap<>();
        this.powCounts.put("sword", 0);
        this.powCounts.put("shield", 0);
        this.powCounts.put("boots", 0);

        this.configProps = loadProperties(basePath + "config.properties");
        this.characterProps = loadProperties(basePath + "character.properties");
        this.bossProps = loadProperties(basePath + "boss.properties");
        Properties equipProps = loadProperties(basePath + "equipment.properties");

        Preferences prefs = Preferences.userNodeForPackage(GameRepository.class);

        Map<String, ItemBuilder> tempItems = new HashMap<>();

        for (String key : equipProps.stringPropertyNames()) {
            String[] parts = key.split("\\.");
            if (parts.length < 2) continue;

            String prefix = parts[0];
            String id = parts[1];

            tempItems.putIfAbsent(id, new ItemBuilder(id));
            ItemBuilder builder = tempItems.get(id);
            String value = cleanPath(equipProps.getProperty(key));

            switch (prefix) {
                case "name": builder.name = value; break;
                case "icon": builder.iconPath = value; break;
                case "female": builder.femalePath = value; break;
                case "male": builder.malePath = value; break;
                case "atk": try { builder.atk = Integer.parseInt(value); } catch (Exception e) {} break;
                case "def": try { builder.def = Integer.parseInt(value); } catch (Exception e) {} break;
                case "vel": try { builder.vel = Integer.parseInt(value); } catch (Exception e) {} break;
            }
        }

        for (ItemBuilder b : tempItems.values()) {
            String type = inferTypeFromId(b.id);
            String priceKey = "price." + type + "." + b.id;
            int price = 100;
            try { price = Integer.parseInt(configProps.getProperty(priceKey, "100").trim()); } catch (Exception e) {}

            if (b.malePath == null || b.malePath.isEmpty()) b.malePath = b.femalePath;
            if (b.femalePath == null || b.femalePath.isEmpty()) b.femalePath = b.malePath;

            ItemModel item = new ItemModel(b.id, type, b.iconPath, b.femalePath, b.malePath, price, b.name, b.atk, b.def, b.vel);
            allItems.put(b.id, item);
            itemCounts.put(item, 0);
        }

        this.player = createPlayerFromProperties(configProps, prefs);
        this.currentBossTier = calculateCurrentBossTier();
        this.boss = createBossByTier(this.currentBossTier);

        loadGameFromJSON();
    }

    private String inferTypeFromId(String id) {
        if (id.startsWith("dres") || id.startsWith("armor")) return "armor";
        if (id.startsWith("cap") || id.startsWith("hat")) return "hat";
        if (id.startsWith("har") || id.startsWith("hair")) return "hair";
        if (id.startsWith("sword")) return "sword";
        if (id.startsWith("shield")) return "shield";
        if (id.startsWith("boots")) return "boots";
        return "unknown";
    }

    private static class ItemBuilder {
        String id; String name = ""; String iconPath = ""; String femalePath = ""; String malePath = ""; int atk = 0, def = 0, vel = 0;
        public ItemBuilder(String id) { this.id = id; }
    }

    private String cleanPath(String raw) {
        if (raw == null) return "";
        return raw.replace("\"", "").trim();
    }

    private PlayerModel createPlayerFromProperties(Properties configProps, Preferences prefs) {
        String rawName = (characterProps != null) ? characterProps.getProperty("player.name", "monogat.ari") : "monogat.ari";
        String finalName = rawName.replace("\"", "").trim();
        int defaultGold = 1000;
        try { defaultGold = Integer.parseInt(configProps.getProperty("player.start.gold", "1000").trim()); } catch (Exception e) {}
        int currentGold = prefs.getInt("saved.player.gold", defaultGold);
        int level = 1;
        try { level = Integer.parseInt(configProps.getProperty("player.start.level", "1")); } catch (Exception e) {}

        PlayerModel newPlayer = new PlayerModel(finalName, currentGold, level);
        int hp = 100, xp = 1, atk = 1, def = 1, vel = 1;
        if (characterProps != null) {
            try {
                xp = Integer.parseInt(characterProps.getProperty("player.xp", "1").trim());
                hp = Integer.parseInt(characterProps.getProperty("player.hp", "100").trim());
                atk = Integer.parseInt(characterProps.getProperty("player.atk", "1").trim());
                def = Integer.parseInt(characterProps.getProperty("player.def", "1").trim());
                vel = Integer.parseInt(characterProps.getProperty("player.vel", "1").trim());
            } catch (Exception e) {}
        }

        newPlayer.setHp(hp); newPlayer.setXp(xp); newPlayer.setAtk(atk); newPlayer.setDef(def); newPlayer.setVel(vel);
        Properties source = (characterProps != null && characterProps.containsKey("char.model")) ? characterProps : configProps;
        newPlayer.setBody(cleanPath(source.getProperty("char.model")));
        newPlayer.setHair(cleanPath(source.getProperty("char.hair")));
        newPlayer.setHairIcon(cleanPath(source.getProperty("icon.hair")));
        newPlayer.setHat(cleanPath(source.getProperty("char.hat")));
        newPlayer.setHatIcon(cleanPath(source.getProperty("icon.hat")));
        newPlayer.setArmor(cleanPath(source.getProperty("char.dres")));
        newPlayer.setArmorIcon(cleanPath(source.getProperty("icon.dres")));
        newPlayer.setSword(cleanPath(source.getProperty("char.sword")));
        newPlayer.setSwordIcon(cleanPath(source.getProperty("icon.sword")));
        newPlayer.setShield(cleanPath(source.getProperty("char.shield")));
        newPlayer.setShieldIcon(cleanPath(source.getProperty("icon.shield")));

        String defaultAvatarPath = (characterProps != null) ? characterProps.getProperty("profile.pic1") : null;
        String savedAvatar = prefs.get("saved.avatar.path", defaultAvatarPath);
        if (savedAvatar != null) {
            savedAvatar = savedAvatar.replace("\"", "").trim();
            if(savedAvatar.startsWith("@")) savedAvatar = savedAvatar.substring(1);
            newPlayer.setAvatarByPath(savedAvatar);
        }
        newPlayer.goldProperty().addListener((obs, oldVal, newVal) -> prefs.putInt("saved.player.gold", newVal.intValue()));
        return newPlayer;
    }

    public int calculateCurrentBossTier() {
        LocalDate today = LocalDate.now();
        long daysPassed = ChronoUnit.DAYS.between(GAME_EPOCH, today);
        if (daysPassed < 0) return 0;
        return (int) ((daysPassed / DAYS_PER_BOSS) % TOTAL_BOSS_TIERS);
    }

    private BossModel createBossByTier(int tier) {
        String name = "Boss Default";
        int hp = 100, atk = 20, def = 10, vel = 5;
        String spritePath = null, bgPath = null, arenaPath = null, recommendedLevel = "1", musicPath = "default_boss.mp3";

        if (bossProps != null) {
            String suffix = String.valueOf(tier);
            name = bossProps.getProperty("boss.name" + suffix, name).replace("\"", "").trim();
            try {
                hp = Integer.parseInt(bossProps.getProperty("boss.hp" + suffix, "100").trim());
                atk = Integer.parseInt(bossProps.getProperty("boss.atk" + suffix, "20").trim());
                def = Integer.parseInt(bossProps.getProperty("boss.def" + suffix, "10").trim());
                vel = Integer.parseInt(bossProps.getProperty("boss.vel" + suffix, "50").trim());
            } catch (Exception e) {}

            spritePath = cleanPath(bossProps.getProperty("boss.sprite" + suffix));
            bgPath = cleanPath(bossProps.getProperty("boss.bg" + suffix));
            arenaPath = cleanPath(bossProps.getProperty("boss.arena" + suffix));
            recommendedLevel = bossProps.getProperty("boss.rlevel" + suffix, "1").replace("\"", "").trim();

            musicPath = bossProps.getProperty("boss.music" + suffix, "default_boss.mp3").replace("\"", "").trim();
        }

        return new BossModel(name, hp, atk, def, vel, spritePath, bgPath, arenaPath, recommendedLevel, musicPath);
    }

    public boolean checkForBossUpdate() {
        int actualTier = calculateCurrentBossTier();
        if (actualTier != this.currentBossTier) {
            this.getPlayer().setDefeated(false);
            this.currentBossTier = actualTier;
            this.boss = createBossByTier(actualTier);
            return true;
        }
        return false;
    }

    public String getTimeUntilNextBossFormatted() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime epochStart = GAME_EPOCH.atStartOfDay();
        long daysFromStart = ChronoUnit.DAYS.between(epochStart, now);
        long currentCycle = (daysFromStart < 0) ? -1 : (daysFromStart / DAYS_PER_BOSS);
        LocalDateTime nextSwitchDate = epochStart.plusDays((currentCycle + 1) * DAYS_PER_BOSS);
        Duration duration = Duration.between(now, nextSwitchDate);
        if (duration.isNegative() || duration.isZero()) return "00g 00h 00m 00s";
        return String.format("%02dg %02dh %02dm %02ds", duration.toDays(), duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
    }

    private Properties loadProperties(String fileName) {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(fileName)) {
            if (input != null) props.load(input);
            else System.err.println("Impossibile trovare il file properties: " + fileName);
        } catch (IOException ex) { ex.printStackTrace(); }
        return props;
    }

    public void saveGameToJSON() {
        if (player == null) return;
        try {
            PlayerSaveData data = new PlayerSaveData();
            data.setPlayerName(player.getPlayerName());
            data.setSaveDate(LocalDateTime.now().toString());
            data.setLastDailyDate(LocalDate.now().toString());
            data.setOwnedItems(new ArrayList<>(player.getOwnedItems()));
            data.setGold(player.getGold());
            data.setLevel(player.getLevel());
            data.setXp(player.getXp());
            data.setHp(player.getHp());
            data.setAtk(player.getAtk());
            data.setDef(player.getDef());
            data.setVel(player.getVel());
            data.setDaysNumber(player.getDaysNumber());
            data.setTaskCompleted(player.getTaskCompleted());
            data.setMale(player.isMale());
            data.setDefeated(player.isDefeated());
            data.setBodyPath(player.bodyPathProperty().get());
            data.setHatPath(player.hatPathProperty().get());
            data.setArmorPath(player.armorPathProperty().get());
            data.setHairPath(player.hairPathProperty().get());
            data.setSwordPath(player.swordPathProperty().get());
            data.setShieldPath(player.shieldPathProperty().get());
            data.setHatIconPath(player.hatIconPathProperty().get());
            data.setArmorIconPath(player.armorIconPathProperty().get());
            data.setHairIconPath(player.hairIconPathProperty().get());
            data.setSwordIconPath(player.swordIconPathProperty().get());
            data.setShieldIconPath(player.shieldIconPathProperty().get());
            data.setPowCounts(new HashMap<>(this.powCounts));
            data.setCompletedDailyTasks(new ArrayList<>(player.getCompletedDailyTasksSet()));
            objectMapper.writeValue(saveFile, data);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void loadGameFromJSON() {
        if (!saveFile.exists()) return;
        try {
            PlayerSaveData data = objectMapper.readValue(saveFile, PlayerSaveData.class);
            if (this.player != null) {
                this.player.setPlayerName(data.getPlayerName());
                this.player.setGold(data.getGold());
                this.player.setLevel(data.getLevel());
                this.player.setXp(data.getXp());
                this.player.setHp(data.getHp());
                this.player.setAtk(data.getAtk());
                this.player.setDef(data.getDef());
                this.player.setVel(data.getVel());
                this.player.setDaysNumber(data.getDaysNumber());
                this.player.setTaskCompleted(data.getTaskCompleted());
                this.player.isMaleProperty().set(data.isMale());
                this.player.isDefeatedProperty().set(data.isDefeated());
                if (data.getBodyPath() != null) this.player.setBody(data.getBodyPath());
                if (data.getHatPath() != null) this.player.setHat(data.getHatPath());
                if (data.getArmorPath() != null) this.player.setArmor(data.getArmorPath());
                if (data.getHairPath() != null) this.player.setHair(data.getHairPath());
                if (data.getSwordPath() != null) this.player.setSword(data.getSwordPath());
                if (data.getShieldPath() != null) this.player.setShield(data.getShieldPath());
                if (data.getHatIconPath() != null) this.player.setHatIcon(data.getHatIconPath());
                if (data.getArmorIconPath() != null) this.player.setArmorIcon(data.getArmorIconPath());
                if (data.getHairIconPath() != null) this.player.setHairIcon(data.getHairIconPath());
                if (data.getSwordIconPath() != null) this.player.setSwordIcon(data.getSwordIconPath());
                if (data.getShieldIconPath() != null) this.player.setShieldIcon(data.getShieldIconPath());
                player.getOwnedItems().clear();
                if (data.getOwnedItems() != null) player.getOwnedItems().addAll(data.getOwnedItems());
                if (data.getPowCounts() != null) this.powCounts = new HashMap<>(data.getPowCounts());
                String todayDate = LocalDate.now().toString();
                String savedDate = data.getLastDailyDate();
                if (savedDate != null && savedDate.equals(todayDate)) {
                    player.setCompletedDailyTasks(data.getCompletedDailyTasks());
                } else {
                    player.resetDailyTasks();
                    if (savedDate != null && LocalDate.parse(savedDate).isBefore(LocalDate.now())) {
                        player.setDaysNumber(player.getDaysNumber() + 1);
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}