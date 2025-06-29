package su.mrhantur;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import su.mrhantur.gui.MainUnusualGUI;

public final class Unusuality extends JavaPlugin {

    private MainUnusualGUI mainUnusualGUI;

    private final Map<Enchantment, UnusualEffect> effects = new HashMap<>();

    private final Map<String, Double> todayGain = new HashMap<>();
    private static final double DAILY_CHANCE_LIMIT = 5.0; // максимум 5% прироста в день
    private final Map<String, String> playerIPs = new HashMap<>(); // name -> IP
    private final Set<String> ipUsedToday = new HashSet<>();       // IPs которые уже получили шанс

    @Override
    public void onEnable() {
        getLogger().info("★UNUSUALITY★");
        getLogger().info("♥♦♣♠ IS ♠♣♦♥");
        getLogger().info("[■##] WORKING");

        registerEffects();
        registerCommand(new GiveUnusual(this));
        registerCommand(new UnusualChance(this));
        registerCommand(new UnusualChance(this, "uc"));
        registerCommand(new UnusualChance(this, "гс"));

        mainUnusualGUI = new MainUnusualGUI(this);

        getServer().getPluginManager().registerEvents(new AnvilConflictHandler(), this);

        loadChances();

        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                timer = (timer + 1) % 12000;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack helmet = player.getInventory().getHelmet();
                    if (helmet == null || !helmet.hasItemMeta()) continue;

                    ItemMeta meta = helmet.getItemMeta();
                    if (meta == null) continue;

                    for (Map.Entry<Enchantment, UnusualEffect> entry : effects.entrySet()) {
                        if (meta.hasEnchant(entry.getKey())) {
                            entry.getValue().apply(player, timer);
                            break;
                        }
                    }

                    if (new Random().nextDouble() < ((double) 1 / 24000)) {
                        double delta = 0.01 + new Random().nextDouble() * 0.29;
                        String name = player.getName();
                        addChanceDaily(name, delta);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);
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
    }

    private void register(String key, UnusualEffect effect) {
        Enchantment enchant = Enchantment.getByKey(NamespacedKey.fromString("unusuality:" + key));
        if (enchant != null) {
            effects.put(enchant, effect);
        }
    }

    private void registerCommand(Command command) {
        try {
            var commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            commandMap.register(getDescription().getName(), command);
        } catch (Exception e) {
            getLogger().warning("Failed to register command: " + command.getName());
            e.printStackTrace();
        }
    }

    public MainUnusualGUI getMainUnusualGUI() {
        return mainUnusualGUI;
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

    private File chanceFile;
    private FileConfiguration chanceConfig;

    public double getChance(String playerName) {
        return chanceConfig.getDouble("players." + playerName, 0.0);
    }

    public void setChance(String playerName, double value) {
        chanceConfig.set("players." + playerName, value);
        saveChances();
    }

    public void addChance(String playerName, double delta) {
        setChance(playerName, getChance(playerName) + delta);
    }

    private void addChanceDaily(String playerName, double delta) {
        String ip = playerIPs.get(playerName);
        if (ip == null) return;

        if (ipUsedToday.contains(ip)) return; // уже кто-то с этого IP получил прирост

        double gainedToday = todayGain.getOrDefault(playerName, 0.0);
        if (gainedToday >= DAILY_CHANCE_LIMIT) return;

        double allowed = Math.min(delta, DAILY_CHANCE_LIMIT - gainedToday);
        if (allowed <= 0) return;

        setChance(playerName, getChance(playerName) + allowed);
        todayGain.put(playerName, gainedToday + allowed);
        ipUsedToday.add(ip); // теперь этот IP считается использованным
    }

    public void removeChance(String playerName, double delta) {
        setChance(playerName, getChance(playerName) - delta);
    }

    private void loadChances() {
        chanceFile = new File(getDataFolder(), "unusualchance.yml");
        if (!chanceFile.exists()) {
            saveResource("unusualchance.yml", false);
        }
        chanceConfig = YamlConfiguration.loadConfiguration(chanceFile);
    }

    public Map<String, Double> getAllChances() {
        Map<String, Double> result = new HashMap<>();
        ConfigurationSection section = chanceConfig.getConfigurationSection("players");

        if (section != null) {
            for (String key : section.getKeys(false)) {
                result.put(key, chanceConfig.getDouble("players." + key));
            }
        }

        return result;
    }

    private void saveChances() {
        try {
            chanceConfig.save(chanceFile);
        } catch (IOException e) {
            getLogger().warning("Failed to save unusualchance.yml");
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        playerIPs.put(event.getPlayer().getName(), ip);
    }

    @Override
    public void onDisable() {
        UnusualEffect memory = effects.get(Enchantment.getByKey(NamespacedKey.fromString("unusuality:memory_leak")));
        if (memory instanceof memoryLeak leak) {
            leak.clearDisplays();
        }
    }
}
