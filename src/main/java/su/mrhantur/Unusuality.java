package su.mrhantur;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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

public final class Unusuality extends JavaPlugin implements Listener {

    // ──────────────────────────────────────────────
    // Константы
    // ──────────────────────────────────────────────

    /** Максимальный суммарный прогресс за сутки на один IP */
    private static final double DAILY_CHANCE_LIMIT = 35.0;

    /** Шанс случайного тика прогресса (за 10 000 тиков ≈ один раз) */
    private static final double PROGRESS_TICK_CHANCE = 0.0001;

    /** Минимальный прирост прогресса за один случайный тик */
    private static final double PROGRESS_DELTA_MIN = 0.01;

    /** Максимальный прирост прогресса за один случайный тик */
    private static final double PROGRESS_DELTA_MAX = 5.0;

    /** Период анимации эффектов в тиках (12 000 = 10 минут) */
    private static final int EFFECT_TIMER_PERIOD = 12_000;

    /** Радиус поиска стоек брони вокруг игрока (по горизонтали) */
    private static final double STAND_SEARCH_RADIUS_H = 40.0;

    /** Радиус поиска стоек брони вокруг игрока (по вертикали) */
    private static final double STAND_SEARCH_RADIUS_V = 20.0;

    /** Тики в одних реальных сутках */
    private static final long TICKS_PER_REAL_DAY = 20L * 60 * 60 * 24;

    // ──────────────────────────────────────────────
    // GUI / менеджеры
    // ──────────────────────────────────────────────

    private MainUnusual mainUnusual;
    private UnusualityDataManager dataManager;
    private SeriesManager seriesManager;
    private CaseSelection caseSelection;
    private Settings settings;
    private KeyConverter keyConverter;
    private HelmetExtractor helmetExtractor;
    private TestEnchantment testEnchantment;
    private ExchangeGUI exchangeGUI;

    // ──────────────────────────────────────────────
    // Данные эффектов / прогресса
    // ──────────────────────────────────────────────

    private final Map<Enchantment, UnusualEffect> effects = new HashMap<>();
    private final Map<String, Double> todayGain = new HashMap<>();
    private final Map<String, String> playerIPs = new HashMap<>();
    private final Set<String> ipUsedToday = new HashSet<>();

    private final Random random = new Random();

    // ──────────────────────────────────────────────
    // Жизненный цикл плагина
    // ──────────────────────────────────────────────

    @Override
    public void onEnable() {
        getLogger().info("★UNUSUALITY★");
        getLogger().info("♥♦♣♠ IS ♠♣♦♥");
        getLogger().info("[■##] WORKING");

        // Инициализация dataManager ДО всего остального
        this.dataManager = new UnusualityDataManager(this);

        this.seriesManager    = new SeriesManager(this);
        CaseOpener caseOpener = new CaseOpener(this);
        this.caseSelection    = new CaseSelection(this, caseOpener);
        this.settings         = new Settings(this);
        this.keyConverter     = new KeyConverter(this);
        this.helmetExtractor  = new HelmetExtractor(this);
        this.testEnchantment  = new TestEnchantment(this);
        this.exchangeGUI      = new ExchangeGUI(this);
        this.mainUnusual      = new MainUnusual(this);

        registerEffects();
        registerCommands();

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new AnvilConflictHandler(), this);
        getServer().getPluginManager().registerEvents(new bloodPactListener(), this);
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent e) {
                Enchantment graveEnch = Enchantment.getByKey(NamespacedKey.fromString("unusuality:own_grave"));
                if (graveEnch != null && effects.containsKey(graveEnch)) {
                    UnusualEffect effect = effects.get(graveEnch);
                    if (effect instanceof ownGrave grave) {
                        grave.cleanup(e.getPlayer());
                    }
                }
            }
        }, this);

        startEffectLoop();
        scheduleDailyReset();
    }

    @Override
    public void onDisable() {
        UnusualEffect memory = effects.get(
                Enchantment.getByKey(NamespacedKey.fromString("unusuality:memory_leak")));
        if (memory instanceof memoryLeak leak) {
            leak.clearDisplays();
        }

        // Очистка ownGrave
        Enchantment graveEnch = Enchantment.getByKey(NamespacedKey.fromString("unusuality:own_grave"));
        if (graveEnch != null && effects.containsKey(graveEnch)) {
            if (effects.get(graveEnch) instanceof ownGrave grave) {
                grave.cleanupAll();
            }
        }
    }

    // ──────────────────────────────────────────────
    // Главный игровой цикл эффектов
    // ──────────────────────────────────────────────

    private void startEffectLoop() {
        new BukkitRunnable() {
            int timer = 0;

            // Стойки - это пиздец, оно страшно неоптимизировано
            private final Set<ArmorStand> processedStands = new HashSet<>();

            @Override
            public void run() {
                timer = (timer + 1) % EFFECT_TIMER_PERIOD;

                tickRandomProgress();

                processedStands.clear();

                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Эффект на игроке
                    if (dataManager.getShowEffectPlayer(player.getName().toLowerCase())) {
                        ItemStack helmet = player.getInventory().getHelmet();
                        if (helmet.hasItemMeta()) {
                            applyEffectsToEntity(player, helmet.getItemMeta(), timer, false);
                        }
                    }

                    for (Entity nearby : player.getNearbyEntities(
                            STAND_SEARCH_RADIUS_H, STAND_SEARCH_RADIUS_V, STAND_SEARCH_RADIUS_H)) {
                        if (!(nearby instanceof ArmorStand stand)) continue;
                        if (!processedStands.add(stand)) continue; // уже обработана

                        ItemStack standHelmet = stand.getHelmet();
                        if (standHelmet.hasItemMeta()) {
                            applyEffectsToEntity(stand, standHelmet.getItemMeta(), timer, false);
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    /** Случайный прирост прогресса для всех онлайн-игроков */
    private void tickRandomProgress() {
        if (random.nextDouble() >= PROGRESS_TICK_CHANCE) return;

        double delta = PROGRESS_DELTA_MIN + random.nextDouble() * (PROGRESS_DELTA_MAX - PROGRESS_DELTA_MIN);
        for (Player player : Bukkit.getOnlinePlayers()) {
            addProgressDaily(player, delta);
        }
    }

    // ──────────────────────────────────────────────
    // Сброс суточных лимитов (раз в реальные сутки)
    // ──────────────────────────────────────────────

    private void scheduleDailyReset() {
        new BukkitRunnable() {
            @Override
            public void run() {
                todayGain.clear();
                ipUsedToday.clear();
                getLogger().info("[Unusuality] Суточные лимиты прогресса сброшены.");
            }
        }.runTaskTimer(this, TICKS_PER_REAL_DAY, TICKS_PER_REAL_DAY);
    }

    // ──────────────────────────────────────────────
    // Применение эффектов
    // ──────────────────────────────────────────────

    /**
     * Применяет необычные эффекты к сущности (игроку или стойке брони).
     *
     * @param entity         Сущность, к которой применяются эффекты
     * @param meta           Метаданные шлема
     * @param timer          Таймер анимации
     * @param applyAllEffects Если true — применяются все найденные эффекты, иначе только первый
     */
    private void applyEffectsToEntity(Entity entity, ItemMeta meta,
                                      int timer, boolean applyAllEffects) {
        if (meta == null) return;

        List<Player> viewers = buildViewerList(entity);
        if (viewers.isEmpty()) return;

        for (Map.Entry<Enchantment, UnusualEffect> entry : effects.entrySet()) {
            if (!meta.hasEnchant(entry.getKey())) continue;
            entry.getValue().apply(entity, timer, viewers);
            if (!applyAllEffects) break;
        }
    }

    /** Составляет список игроков, которым видны эффекты данной сущности */
    private List<Player> buildViewerList(Entity entity) {
        List<Player> viewers = new ArrayList<>();

        if (entity instanceof Player target) {
            String targetName = target.getName().toLowerCase();
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                String viewerName = viewer.getName().toLowerCase();
                if (!dataManager.getShowEffectPlayer(viewerName)) continue;

                if (viewer.equals(target)) {
                    if (dataManager.getCanSeeMyEffect(targetName)) viewers.add(viewer);
                } else {
                    if (dataManager.getShowAllEffects(viewerName)) viewers.add(viewer);
                }
            }
        } else if (entity instanceof ArmorStand stand) {
            for (Player viewer : stand.getWorld().getNearbyPlayers(stand.getLocation(), 50)) {
                String viewerName = viewer.getName().toLowerCase();
                if (!dataManager.getShowEffectPlayer(viewerName)) continue;
                if (!dataManager.getShowAllEffects(viewerName)) continue;
                viewers.add(viewer);
            }
        }

        return viewers;
    }

    // ──────────────────────────────────────────────
    // Прогресс и ключи
    // ──────────────────────────────────────────────

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

    // ──────────────────────────────────────────────
    // Регистрация эффектов
    // ──────────────────────────────────────────────

    private void registerEffects() {
        register("fireflies",            new fireflies());
        register("confetti",             new confetti());
        register("green_energy",         new greenEnergy());
        register("galaxy",               new galaxy());
        register("restless_souls",       new restlessSouls());
        register("astral_step",          new astralStep());

        register("stormcloud",           new stormcloud());
        register("memory_leak",          new memoryLeak());
        register("neutron_star",         new neutronStar());
        register("flaming_lantern",      new flamingLantern());
        register("bubbling",             new bubbling());
        register("orbiting_fire",        new orbitingFire());
        register("mountain_halo",        new mountainHalo());
        register("miami_nights",         new miamiNights());

        register("carousel",             new carousel());
        register("tornado",              new tornado());
        register("rejection",            new rejection());
        register("stargazer",            new stargazer());
        register("devil_horns",          new devilHorns());
        register("neon_electricity",     new neonElectricity());
        register("radiation",            new radiation());
        register("blood_pact",           new bloodPact());

        register("sputnik",              new sputnik());
        register("own_grave",            new ownGrave());
        register("silent_nights",        new silentNights());
        register("rockets",              new rockets());
        register("sakura_trails",        new sakuraTrails());
        register("no_sound_no_memory",   new noSoundNoMemory());
    }

    private void register(String key, UnusualEffect effect) {
        NamespacedKey nsKey = NamespacedKey.fromString("unusuality:" + key);
        Enchantment enchant = Enchantment.getByKey(nsKey);
        if (enchant != null) {
            effects.put(enchant, effect);
        } else {
            getLogger().warning("Failed to register effect '" + key +
                    "': Enchantment not found. Make sure it's registered in your plugin.");
        }
    }

    // ──────────────────────────────────────────────
    // Регистрация команд
    // ──────────────────────────────────────────────

    private void registerCommands() {
        registerCommand(new GiveUnusual(this));
        registerCommand(new UnusualChance(this));
        registerCommand(new UnusualChance(this, "uc"));
        registerCommand(new UnusualChance(this, "гс"));
        registerCommand(new UnusualChance(this, "uk"));
        registerCommand(new UnusualChance(this, "гл"));
    }

    private void registerCommand(Command command) {
        try {
            var commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            commandMap.register(getDescription().getName().toLowerCase(), command);
        } catch (Exception e) {
            getLogger().warning("Failed to register command: " + command.getName());
            e.printStackTrace();
        }
    }

    // ──────────────────────────────────────────────
    // События
    // ──────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getAddress() == null) return;
        String ip = player.getAddress().getAddress().getHostAddress();
        playerIPs.put(player.getName().toLowerCase(), ip);
    }

    // ──────────────────────────────────────────────
    // Публичное API
    // ──────────────────────────────────────────────

    public MainUnusual getMainUnusualGUI()          { return mainUnusual; }
    public SeriesManager getSeriesManager()          { return seriesManager; }
    public CaseSelection getCaseSelectionGUI()       { return caseSelection; }
    public Settings getSettingsGUI()                 { return settings; }
    public ExchangeGUI getExchangeGUI()              { return exchangeGUI; }
    public KeyConverter getKeyConverter()            { return keyConverter; }
    public HelmetExtractor getHelmetExtractorGUI()   { return helmetExtractor; }
    public UnusualityDataManager getDataManager()    { return dataManager; }
    public TestEnchantment getTestEnchantment()      { return testEnchantment; }

    /** @deprecated используй {@link #getDataManager()} */
    @Deprecated
    public UnusualityDataManager getPlayerData()     { return dataManager; }

    public List<Enchantment> getUnusualEnchantments() {
        return new ArrayList<>(effects.keySet());
    }

    public boolean isUnusualEnchantment(Enchantment enchantment) {
        return effects.containsKey(enchantment);
    }

    public Enchantment getRandomUnusualEnchantment() {
        List<Enchantment> list = getUnusualEnchantments();
        if (list.isEmpty()) return null;
        return list.get(random.nextInt(list.size()));
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
}