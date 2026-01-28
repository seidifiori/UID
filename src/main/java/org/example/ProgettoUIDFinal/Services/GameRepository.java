package org.example.ProgettoUIDFinal.Services;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.prefs.Preferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.ProgettoUIDFinal.model.BossModel;
import org.example.ProgettoUIDFinal.model.ItemModel;
import org.example.ProgettoUIDFinal.model.PlayerModel;
import org.example.ProgettoUIDFinal.model.PlayerSaveData;

import java.time.Duration;
import org.example.ProgettoUIDFinal.model.QuestModel;
import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

public class GameRepository {

    private static GameRepository instance;

    private PlayerModel player;
    private BossModel boss;
    private Set<String> defeatedBossesNames = new HashSet<>();
    private final List<QuestModel> quests = new ArrayList<>();


    private final File saveFile = new File("user_save.json"); //File fisico su disco
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT); //Serve per leggere/scrivere sul json

    private Properties configProps; //Prezzi
    private Properties characterProps; //Stats iniziali del personaggio
    private Properties bossProps; //Stats dei boss

    private Map<String, Integer> powCounts = new HashMap<>(); //Conta i potenziamenti
    private final Map<String, ItemModel> allItems = new HashMap<>();
    private final Map<ItemModel, Integer> itemCounts = new HashMap<>(); //Quanti item si possiedono

    private static final int TOTAL_BOSS_TIERS = 3; //Ci sono 3 livelli di boss
    private static final int DAYS_PER_BOSS = 7; //Il boss cambia ogni 7 giorni
    private int currentBossTier = 0; //Livello boss attuale
    private LocalDate gameEpoch; //La data di inizio della partita

    // --- Variabile per la preferenza FLASH ---
    private boolean flashEffectsEnabled = true;

    private GameRepository() {
        this.gameEpoch = LocalDate.now();
        loadData();
    }

    public static GameRepository getInstance() {
        if (instance == null) {
            instance = new GameRepository();
        }
        return instance;
    }

    // --- Getter e Setter per la preferenza ---
    public boolean isFlashEffectsEnabled() {
        return flashEffectsEnabled;
    }


    public void setFlashEffectsEnabled(boolean enabled) {
        this.flashEffectsEnabled = enabled;
    }

    public PlayerModel getPlayer() { return player; }
    public BossModel getBoss() { return boss; }
    public ItemModel getItem(String id) { return allItems.get(id); }
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

    public boolean hasSaveFile() {
        return saveFile != null && saveFile.exists();
    } //Controlla se il file user_save.json esiste sul computer

    public void createNewUser(String username) {
        if (player != null) {
            player.setPlayerName(username);
            this.gameEpoch = LocalDate.now();
            saveGameToJSON();
        }
    }
    public void markBossAsDefeated(String bossName) {
        if (bossName == null) return;
        String cleanName = bossName.replace("\"", "").trim().toLowerCase();
        defeatedBossesNames.add(cleanName);
        saveGameToJSON();
    }

    public boolean isBossDefeated(String bossName) {
        if (bossName == null) return false;
        String cleanName = bossName.replace("\"", "").trim().toLowerCase();
        return defeatedBossesNames.contains(cleanName);
    }
    public List<QuestModel> getQuests() {
        return quests; // restituisco la lista viva
    }

    public void addQuest(QuestModel quest) {
        if (quest == null) return;
        quests.add(quest);
    }

    public void removeQuest(QuestModel quest) {
        if (quest == null) return;
        quests.remove(quest);
    }



    private void loadData() {

        String basePath = "/org/example/ProgettoUIDFinal/properties/";
        this.powCounts = new HashMap<>();
        this.powCounts.put("sword", 0);
        this.powCounts.put("shield", 0);
        this.powCounts.put("boots", 0);

        this.configProps = loadProperties(basePath + "config.properties");
        this.characterProps = loadProperties(basePath + "character.properties");
        this.bossProps = loadProperties(basePath + "boss.properties");
        Properties equipProps = loadProperties(basePath + "equipment.properties");

        Map<String, ItemBuilder> tempItems = new HashMap<>(); //area di appoggio temporanea

        for (String key : equipProps.stringPropertyNames()) {
            String[] parts = key.split("\\.");
            if (parts.length < 2) continue; // Salta righe malformate
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
                case "background":
                    if (key.contains("btn")) {
                        builder.femalePath = value;   // 👉 sfondo globale
                        builder.malePath = value;
                        builder.iconPath = value;
                    }
                    if (key.contains("layer")) {
                        builder.backgroundLayerPath = value; // 👉 overlay
                    }
                    break;
                case "atk": try { builder.atk = Integer.parseInt(value); } catch (Exception e) {} break;
                case "def": try { builder.def = Integer.parseInt(value); } catch (Exception e) {} break;
                case "vel": try { builder.vel = Integer.parseInt(value); } catch (Exception e) {} break;
            }
        }

        for (ItemBuilder b : tempItems.values()) {
            String type = inferTypeFromId(b.id); //Capisce che oggetto è dall'id
            String priceKey = "price." + type + "." + b.id;
            int price = 100;
            try { price = Integer.parseInt(configProps.getProperty(priceKey, "100").trim()); } catch (Exception e) {}

            //Se manca lo sprite maschio, usa quello femmina (e viceversa).
            if (b.malePath == null || b.malePath.isEmpty()) b.malePath = b.femalePath;
            if (b.femalePath == null || b.femalePath.isEmpty()) b.femalePath = b.malePath;

            //creazione dell'oggetto
            ItemModel item = new ItemModel(b.id, type, b.iconPath, b.femalePath, b.malePath,b.backgroundLayerPath, price, b.name, b.atk, b.def, b.vel);
            allItems.put(b.id, item);
            itemCounts.put(item, 0);
        }

        this.player = createPlayerFromProperties(configProps);
        this.currentBossTier = calculateCurrentBossTier();
        this.boss = createBossByTier(this.currentBossTier);

        loadGameFromJSON(); //sovrascrittura dei dati salvati di default con quelli del json
    }

    private String inferTypeFromId(String id) {
        if (id.startsWith("dres") || id.startsWith("armor")) return "armor";
        if (id.startsWith("cap") || id.startsWith("hat")) return "hat";
        if (id.startsWith("har") || id.startsWith("hair")) return "hair";
        if (id.startsWith("sword")) return "sword";
        if (id.startsWith("shield")) return "shield";
        if (id.startsWith("boots")) return "boots";
        if (id.startsWith("btn") || id.startsWith("bg")) return "background";
        return "unknown";
    }

    private static class ItemBuilder {
        String id; String name = ""; String iconPath = ""; String femalePath = ""; String malePath = ""; String backgroundLayerPath = ""; int atk = 0, def = 0, vel = 0;
        public ItemBuilder(String id) { this.id = id; }
    }

    private String cleanPath(String raw) {
        if (raw == null) return "";
        return raw.replace("\"", "").trim();
    }

    private PlayerModel createPlayerFromProperties(Properties configProps) {
        String rawName = (characterProps != null) ? characterProps.getProperty("player.name", "Hero") : "Hero";
        String finalName = rawName.replace("\"", "").trim();
        int defaultGold = 2000;
        int level = 5;

        PlayerModel newPlayer = new PlayerModel(finalName, defaultGold, level);
        int hp = 100, xp = 1, atk = 5, def = 5, vel = 5;
        if (characterProps != null) {
            try {
                xp = Integer.parseInt(characterProps.getProperty("player.xp", "1").trim());
                hp = Integer.parseInt(characterProps.getProperty("player.hp", "100").trim());
                atk = Integer.parseInt(characterProps.getProperty("player.atk", "5").trim());
                def = Integer.parseInt(characterProps.getProperty("player.def", "5").trim());
                vel = Integer.parseInt(characterProps.getProperty("player.vel", "5").trim());
            } catch (Exception e) {}
        }
        newPlayer.setHp(hp); newPlayer.setXp(xp); newPlayer.setAtk(atk); newPlayer.setDef(def); newPlayer.setVel(vel);

        Properties source = (characterProps != null && characterProps.containsKey("char.model")) ? characterProps : configProps;
        newPlayer.setBody(cleanPath(source.getProperty("char.model")));
        newPlayer.setAvatarByPath(cleanPath(source.getProperty("char.avatar")));
        newPlayer.setBannerPath(cleanPath(source.getProperty("char.banner")));
        newPlayer.setHair(cleanPath(source.getProperty("char.hair")));
        newPlayer.setHairIcon(cleanPath(source.getProperty("icon.hair")));
        newPlayer.setHairName(cleanPath(source.getProperty("name.hair")));
        newPlayer.setHat(cleanPath(source.getProperty("char.hat")));
        newPlayer.setHatIcon(cleanPath(source.getProperty("icon.hat")));
        newPlayer.setHatName(cleanPath(source.getProperty("name.hat")));
        newPlayer.setArmor(cleanPath(source.getProperty("char.dres")));
        newPlayer.setArmorIcon(cleanPath(source.getProperty("icon.dres")));
        newPlayer.setArmorName(cleanPath(source.getProperty("name.dres")));
        newPlayer.setSword(cleanPath(source.getProperty("char.sword")));
        newPlayer.setSwordIcon(cleanPath(source.getProperty("icon.sword")));
        newPlayer.setSwordName(cleanPath(source.getProperty("name.sword")));
        newPlayer.setShield(cleanPath(source.getProperty("char.shield")));
        newPlayer.setShieldIcon(cleanPath(source.getProperty("icon.shield")));
        newPlayer.setShieldName(cleanPath(source.getProperty("name.shield")));
        newPlayer.setBackgroundPath(cleanPath(source.getProperty("char.background")));

        return newPlayer;
    }

    public int calculateCurrentBossTier() {
        LocalDate today = LocalDate.now();
        long daysPassed = ChronoUnit.DAYS.between(gameEpoch, today);
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
        LocalDateTime epochStart = gameEpoch.atStartOfDay(); //Prende la data di inizio del gioco e la trasforma in un orario preciso
        long daysFromStart = ChronoUnit.DAYS.between(epochStart, now); //Calcola quanti giorni interi sono trascorsi tra l'inizio del gioco e adesso
        long currentCycle = (daysFromStart < 0) ? -1 : (daysFromStart / DAYS_PER_BOSS); //Determina in quale "settimana dei boss" ci troviamo.
        LocalDateTime nextSwitchDate = epochStart.plusDays((currentCycle + 1) * DAYS_PER_BOSS); //Calcola la data e l'ora esatta in cui arriverà il prossimo boss
        Duration duration = Duration.between(now, nextSwitchDate); //Calcola la differenza
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

    /**
     * SALVATAGGIO JSON: Ora salva anche la preferenza Flash
     */
    public void saveGameToJSON() {
        if (player == null) return;
        try {
            PlayerSaveData data = new PlayerSaveData(); //DTO

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
            data.setHairName(player.getHairName());
            data.setHatName(player.getHatName());
            data.setArmorName(player.getArmorName());
            data.setSwordName(player.getSwordName());
            data.setShieldName(player.getShieldName());
            data.setAvatarPath(player.getAvatarPath());
            data.setBannerPath(player.getBannerPath());
            data.setPowCounts(new HashMap<>(this.powCounts));
            data.setCompletedDailyTasks(new ArrayList<>(player.getCompletedDailyTasksSet()));
            String currentBg = org.example.ProgettoUIDFinal.Services.BackgroundService.getInstance().getCurrentBackgroundPath();
            data.setBackgroundPath(currentBg);
            data.setDefeatedBossesNames(new ArrayList<>(this.defeatedBossesNames));
            data.setQuests(new ArrayList<>(this.quests));


            if (this.gameEpoch != null) { data.setGameEpoch(this.gameEpoch.toString()); }

            data.setFlashEffectsEnabled(this.flashEffectsEnabled);

            objectMapper.writeValue(saveFile, data);
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * CARICAMENTO JSON: Ora legge anche la preferenza Flash
     */
    public void loadGameFromJSON() {
        if (!saveFile.exists()) return;
        try {
            PlayerSaveData data = objectMapper.readValue(saveFile, PlayerSaveData.class); //DTO
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

                if (data.getGameEpoch() != null) { this.gameEpoch = LocalDate.parse(data.getGameEpoch()); }

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
                if (data.getHairName() != null) this.player.setHairName(data.getHairName());
                if (data.getHatName() != null) this.player.setHatName(data.getHatName());
                if (data.getArmorName() != null) this.player.setArmorName(data.getArmorName());
                if (data.getSwordName() != null) this.player.setSwordName(data.getSwordName());
                if (data.getShieldName() != null) this.player.setShieldName(data.getShieldName());

                if (data.getAvatarPath() != null) this.player.setAvatarByPath(data.getAvatarPath());
                if (data.getBannerPath() != null) this.player.setBannerPath(data.getBannerPath());

                player.getOwnedItems().clear();
                if (data.getOwnedItems() != null) player.getOwnedItems().addAll(data.getOwnedItems());
                if (data.getPowCounts() != null) this.powCounts = new HashMap<>(data.getPowCounts());

                if (data.getBackgroundPath() != null && !data.getBackgroundPath().isEmpty()) {
                    org.example.ProgettoUIDFinal.Services.BackgroundService.getInstance().setBackgroundByPath(data.getBackgroundPath());
                }
                if (data.getDefeatedBossesNames() != null) {
                    this.defeatedBossesNames = new HashSet<>(data.getDefeatedBossesNames());
                }

                this.flashEffectsEnabled = data.isFlashEffectsEnabled();

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
            this.quests.clear();
            if (data.getQuests() != null) {
                this.quests.addAll(data.getQuests());
            }

        } catch (IOException e) { e.printStackTrace(); }
    }


     //CHEAT: Sblocca tutti gli oggetti disponibili nel gioco aggiungendoli all'inventario del player.
    /*
    public void unlockAllItems() {
        if (player == null) return;

        // Itera su tutti gli ID degli oggetti caricati nel gioco
        for (String itemId : allItems.keySet()) {
            // Se il player non possiede l'oggetto, aggiungilo
            if (!player.hasItem(itemId)) {
                player.addOwnedItem(itemId);
                incrementItemCount(itemId); // Aggiorna anche il contatore numerico
            }
        }

        // Salva immediatamente le modifiche nel JSON
        saveGameToJSON();
    }*/
}