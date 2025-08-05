package su.mrhantur;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import su.mrhantur.commands.GiveUnusual;
import su.mrhantur.commands.UnusualChance;
import su.mrhantur.effects.*;
import su.mrhantur.gui.*;
import su.mrhantur.series.SeriesManager;

import java.util.*;

public final class Unusuality extends JavaPlugin {

    private MainUnusual mainUnusual;
    private UnusualityDataManager dataManager;
    private SeriesManager seriesManager;
    private CaseSelection caseSelection;
    private Settings settings;
    private KeyConverter keyConverter;
    private HelmetExtractor helmetExtractor;
    private TestEnchantment testEnchantment;
    private ExchangeGUI exchangeGUI;

    private final Map<Enchantment, UnusualEffect> effects = new HashMap<>();
    private final Map<String, Double> todayGain = new HashMap<>();
    private static final double DAILY_CHANCE_LIMIT = 35.0;
    private final Map<String, String> playerIPs = new HashMap<>();
    private final Set<String> ipUsedToday = new HashSet<>();

    private final Random random = new Random();

    @Override
    public void onEnable() {
        getLogger().info("★UNUSUALITY★");
        getLogger().info("♥♦♣♠ IS ♠♣♦♥");
        getLogger().info("[■##] WORKING");

        // Инициализация dataManager ДО всего остального
        this.dataManager = new UnusualityDataManager(this);

        // Инициализация новых компонентов
        this.seriesManager = new SeriesManager(this);
        CaseOpener caseOpener = new CaseOpener(this);
        this.caseSelection = new CaseSelection(this, caseOpener);
        this.settings = new Settings(this);
        this.keyConverter = new KeyConverter(this);
        this.helmetExtractor = new HelmetExtractor(this);
        this.testEnchantment = new TestEnchantment(this);
        this.exchangeGUI = new ExchangeGUI(this);

        registerEffects();
        registerCommand(new GiveUnusual(this));
        registerCommand(new UnusualChance(this));
        registerCommand(new UnusualChance(this, "uc"));
        registerCommand(new UnusualChance(this, "гс"));
        registerCommand(new UnusualChance(this, "uk"));
        registerCommand(new UnusualChance(this, "гл"));

        mainUnusual = new MainUnusual(this);

        getServer().getPluginManager().registerEvents(new AnvilConflictHandler(), this);
        getServer().getPluginManager().registerEvents(new su.mrhantur.effects.bloodPactListener(), this);

        new BukkitRunnable() {
            int timer = 0;
            private final Set<ArmorStand> processedStands = new HashSet<>();

            @Override
            public void run() {
                timer = (timer + 1) % 12000;
                processedStands.clear();
                // КАКАЯ ЖЕ ЭТО ПОЕБОТА СО СТОЙКАМИ
                // ЭТО ПИЗДЕЦ КАК НЕПРОИЗВОДИТЕЛЬНО
                // ЕСЛИ ЧТО НАДО УДАЛИТЬ ЭТО НАХЕР

                // Случайное добавление прогресса
                if (random.nextDouble() < 0.0001) { // 1/10000
                    double delta = 0.01 + random.nextDouble() * 4.99;
                    Bukkit.getOnlinePlayers().forEach(player -> addProgressDaily(player, delta));
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Обработка эффектов на игроке
                    if (getDataManager().getShowEffectPlayer(player.getName().toLowerCase())) {
                        ItemStack helmet = player.getInventory().getHelmet();
                        if (helmet != null && helmet.hasItemMeta()) {
                            applyEffectsToEntity(player, helmet.getItemMeta(), timer, false);
                        }
                    }

                    // Обработка эффектов на стойках брони
                    for (Entity nearby : player.getNearbyEntities(40, 20, 40)) {
                        if (!(nearby instanceof ArmorStand)) continue;
                        ArmorStand stand = (ArmorStand) nearby;
                        if (processedStands.contains(stand)) continue;
                        processedStands.add(stand);

                        ItemStack standHelmet = stand.getHelmet();
                        if (standHelmet != null && standHelmet.hasItemMeta()) {
                            applyEffectsToEntity(stand, standHelmet.getItemMeta(), timer, false);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    /**
     * Применяет необычные эффекты к сущности (игроку или стойке брони)
     * @param entity Сущность, к которой применяются эффекты
     * @param meta Метаданные предмета (шлема)
     * @param timer Таймер для анимации эффектов
     * @param applyAllEffects Если true, применяются все эффекты, иначе только первый найденный
     */
    private void applyEffectsToEntity(Entity entity, ItemMeta meta, int timer, boolean applyAllEffects) {
        if (meta == null) return;

        List<Player> viewers = new ArrayList<>();
        UnusualityDataManager data = getDataManager();

        // Определяем зрителей в зависимости от типа сущности
        if (entity instanceof Player) {
            Player player = (Player) entity;
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (!data.getShowEffectPlayer(viewer.getName().toLowerCase())) continue;
                if (viewer.equals(player)) {
                    if (data.getCanSeeMyEffect(player.getName().toLowerCase())) {
                        viewers.add(viewer);
                    }
                } else {
                    if (data.getShowAllEffects(viewer.getName().toLowerCase())) {
                        viewers.add(viewer);
                    }
                }
            }
        } else if (entity instanceof ArmorStand) {
            ArmorStand stand = (ArmorStand) entity;
            for (Player viewer : stand.getWorld().getNearbyPlayers(stand.getLocation(), 50)) {
                if (!data.getShowEffectPlayer(viewer.getName().toLowerCase())) continue;
                if (!data.getShowAllEffects(viewer.getName().toLowerCase())) continue;
                viewers.add(viewer);
            }
        }

        // Применяем эффекты
        for (Map.Entry<Enchantment, UnusualEffect> entry : effects.entrySet()) {
            if (meta.hasEnchant(entry.getKey())) {
                entry.getValue().apply(entity, timer, viewers);
                if (!applyAllEffects) {
                    break; // Прерываем после первого эффекта, если не нужно применять все
                }
            }
        }
    }

    private void registerEffects() {
        register("fireflies", new fireflies());
        register("confetti", new confetti());
        register("green_energy", new greenEnergy());
        register("galaxy", new galaxy());
        register("restless_souls", new restlessSouls());
        register("astral_step", new astralStep());

        register("stormcloud", new stormcloud());
        register("memory_leak", new memoryLeak());
        register("neutron_star", new neutronStar());
        register("flaming_lantern", new flamingLantern());
        register("bubbling", new bubbling());
        register("orbiting_fire", new orbitingFire());
        register("mountain_halo", new mountainHalo());
        register("miami_nights", new miamiNights());

        register("carousel", new carousel());
        register("tornado", new tornado());
        register("rejection", new rejection());
        register("stargazer", new stargazer());
        register("devil_horns", new devilHorns());
        register("neon_electricity", new neonElectricity());
        register("radiation", new radiation());
        register("blood_pact", new bloodPact());
    }

    private void register(String key, UnusualEffect effect) {
        NamespacedKey nsKey = NamespacedKey.fromString("unusuality:" + key);
        Enchantment enchant = Enchantment.getByKey(nsKey);
        if (enchant != null) {
            effects.put(enchant, effect);
        } else {
            getLogger().warning("Failed to register effect '" + key +
                    "': Enchantment not found. " +
                    "Make sure it's properly registered in your plugin.");
        }
    }

    private void registerCommand(Command command) {
        try {
            var commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            commandMap.register(getDescription().getName().toLowerCase(), command);
        } catch (Exception e) {
            getLogger().warning("Failed to register command: " + command.getName().toLowerCase());
            e.printStackTrace();
        }
    }

    public MainUnusual getMainUnusualGUI() {
        return mainUnusual;
    }

    public SeriesManager getSeriesManager() {
        return seriesManager;
    }

    public CaseSelection getCaseSelectionGUI() {
        return caseSelection;
    }

    public Settings getSettingsGUI() {
        return settings;
    }

    public ExchangeGUI getExchangeGUI() {
        return exchangeGUI;
    }

    public KeyConverter getKeyConverter() {
        return keyConverter;
    }

    public HelmetExtractor getHelmetExtractorGUI() {
        return helmetExtractor;
    }

    public UnusualityDataManager getDataManager() {
        return dataManager;
    }

    public TestEnchantment getTestEnchantment() {return testEnchantment;}

    // Добавленный метод для совместимости
    public UnusualityDataManager getPlayerData() {
        return dataManager;
    }

    public List<Enchantment> getUnusualEnchantments() {
        return new ArrayList<>(effects.keySet());
    }

    public boolean isUnusualEnchantment(Enchantment enchantment) {
        return effects.containsKey(enchantment);
    }

    public Enchantment getRandomUnusualEnchantment() {
        List<Enchantment> list = getUnusualEnchantments();
        if (list.isEmpty()) return null;
        return list.get(new Random().nextInt(list.size()));
    }

    public ItemStack createUnusualBook(Enchantment enchantment) {
        if (enchantment == null || !isUnusualEnchantment(enchantment)) {
            throw new IllegalArgumentException("Invalid or unknown unusual enchantment: " + enchantment);
        }

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(enchantment, 1, true);
        book.setItemMeta(meta);
        return book;
    }

    private void addProgressDaily(Player player, double delta) {
        String playerName = player.getName().toLowerCase();

        String ip = playerIPs.getOrDefault(playerName, "");
        if (ip.isEmpty() || ipUsedToday.contains(ip)) return;

        double gainedToday = todayGain.getOrDefault(playerName, 0.0);
        if (gainedToday >= DAILY_CHANCE_LIMIT) return;

        double allowed = Math.min(delta, DAILY_CHANCE_LIMIT - gainedToday);
        if (allowed <= 0) return;

        double newProgress = dataManager.getProgress(playerName) + allowed;
        int keys = dataManager.getKeys(playerName);

        while (newProgress >= 1.0) {
            newProgress -= 1.0;
            keys++;
        }

        dataManager.setKeys(playerName, keys);
        dataManager.setProgress(playerName, newProgress);
        todayGain.put(playerName, gainedToday + allowed);
        ipUsedToday.add(ip);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String ip = player.getAddress().getAddress().getHostAddress();
        playerIPs.put(player.getName().toLowerCase(), ip);
    }

    @Override
    public void onDisable() {
        UnusualEffect memory = effects.get(Enchantment.getByKey(NamespacedKey.fromString("unusuality:memory_leak")));
        if (memory instanceof memoryLeak leak) {
            leak.clearDisplays();
        }
    }
}