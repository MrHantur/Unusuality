package su.mrhantur.gui;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import su.mrhantur.Unusuality;
import su.mrhantur.series.EnchantmentSeries;
import su.mrhantur.series.SeriesManager;

import java.util.*;

public class TestEnchantment implements Listener {
    private final Unusuality plugin;
    private final Map<UUID, BukkitTask> activeTasks = new HashMap<>();
    private final Map<String, Enchantment> enchantmentByKey = new HashMap<>();
    private final Map<Enchantment, String> enchantmentToSeries = new HashMap<>();

    public TestEnchantment(Unusuality plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        SeriesManager seriesManager = plugin.getSeriesManager();
        List<EnchantmentSeries> allSeries = new ArrayList<>(seriesManager.getAllSeries());

        // Сортируем серии по ID
        allSeries.sort(Comparator.comparing(EnchantmentSeries::getId));

        // Рассчитываем размер инвентаря
        int size = 27;
        Inventory inv = Bukkit.createInventory(null, size, "§bТест зачарований");

        // Заполняем фон
        ItemStack glass = createGlassPane();
        for (int i = 0; i < size; i++) {
            inv.setItem(i, glass);
        }

        // Добавляем элементы
        int slot = 0;
        for (EnchantmentSeries series : allSeries) {
            // Добавляем зачарования серии
            for (Enchantment enchant : series.getEnchantments()) {
                if (enchant != null) {
                    inv.setItem(slot++, createEnchantedBook(enchant, series.getDisplayName()));
                }
            }
        }

        // Кнопка "Назад"
        ItemStack backButton = createBackButton();
        inv.setItem(size - 1, backButton);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("§bТест зачарований")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;

        if (clicked.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) clicked.getItemMeta();
            if (meta.getStoredEnchants().isEmpty()) return;

            Enchantment enchantment = meta.getStoredEnchants().keySet().iterator().next();
            giveTestHelmet(player, enchantment);
            player.closeInventory();
        }
        else if (clicked.getType() == Material.ARROW) {
            plugin.getMainUnusualGUI().open(player);
        }
    }

    private void giveTestHelmet(Player player, Enchantment enchantment) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        ItemMeta meta = helmet.getItemMeta();

        meta.setDisplayName("§6Тестовый шлем");
        meta.setLore(List.of("§8Тестовый шлем"));
        meta.addEnchant(enchantment, 1, true);
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);

        helmet.setItemMeta(meta);

        player.getInventory().setHelmet(helmet);
        player.sendMessage("§aВыдан тестовый шлем");

        startHelmetTimer(player);
    }

    private void startHelmetTimer(Player player) {
        cancelTask(player);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                ItemStack helmet = player.getInventory().getHelmet();
                if (helmet != null && helmet.hasItemMeta() &&
                        helmet.getItemMeta().getLore() != null &&
                        helmet.getItemMeta().getLore().contains("§8Тестовый шлем")) {

                    player.getInventory().setHelmet(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                    player.spawnParticle(Particle.SMOKE, player.getEyeLocation(), 15, 0.3, 0.3, 0.3, 0.1);
                    player.sendMessage(ChatColor.YELLOW + "Время тестового шлема истекло!");
                }

                activeTasks.remove(player.getUniqueId());
            }
        }.runTaskLater(plugin, 20 * 15); // 15 секунд

        activeTasks.put(player.getUniqueId(), task);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelTask(event.getPlayer());
    }

    private void cancelTask(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && helmet.hasItemMeta() &&
                helmet.getItemMeta().lore() != null &&
                helmet.getItemMeta().lore().contains("§8Тестовый шлем")) {
            player.getInventory().setHelmet(null);
        }

        UUID uuid = player.getUniqueId();
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
            activeTasks.remove(uuid);
        }
    }

    private ItemStack createBackButton() {
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§f← Назад");
        backMeta.setLore(List.of("§7Вернуться в главное меню"));
        backButton.setItemMeta(backMeta);
        return backButton;
    }

    private ItemStack createEnchantedBook(Enchantment enchantment, String seriesName) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(enchantment, 1, true);
        meta.setDisplayName(seriesName);
        meta.setLore(List.of("§eКлик: получить шлем"));
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }
}
