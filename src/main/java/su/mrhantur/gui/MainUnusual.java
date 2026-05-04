package su.mrhantur.gui;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import su.mrhantur.Unusuality;
import su.mrhantur.UnusualityDataManager;

import java.util.*;

public class MainUnusual implements Listener {

    // Размер главного меню
    private static final int GUI_SIZE = 27;

    // Слоты функциональных предметов
    private static final int SLOT_INFO_HEAD        = 13;
    private static final int SLOT_KEY_MANAGEMENT   = 10;
    private static final int SLOT_EXCHANGE         = 11;
    private static final int SLOT_OPEN_CASE        = 12;
    private static final int SLOT_EXTRACT_ENCHANT  = 14;
    private static final int SLOT_TEST_ENCHANT     = 15;
    private static final int SLOT_SETTINGS         = 16;

    // Слоты декоративных рамок (некоторые)
    private static final int[] DECO_SLOTS = {0, 1, 7, 8, 9, 17, 18, 26};
    private static final Material[] DECO_MATERIALS = {
            Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE,
            Material.PINK_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE,
            Material.CYAN_STAINED_GLASS_PANE,
            Material.LIME_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE
    };
    private static final String[] DECO_COLORS = {"§6", "§e", "§d", "§5", "§9", "§b", "§a", "§2"};

    /** @deprecated Используйте {@link CaseOpener#isPlayerRolling(Player)}. Оставлено для обратной совместимости. */
    @Deprecated
    public final Map<UUID, Boolean> rollingPlayers = new HashMap<>();

    private final Unusuality plugin;
    private final UnusualityDataManager dataManager;

    public MainUnusual(Unusuality plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, "§d★ Unusuality Меню ★");
        String playerName = player.getName().toLowerCase();

        // Фон
        ItemStack glass = createGlassPane();
        for (int i = 0; i < GUI_SIZE; i++) {
            inv.setItem(i, glass);
        }

        // Голова игрока
        inv.setItem(SLOT_INFO_HEAD, createPlayerHead(player));

        int keys = dataManager.getKeys(playerName);
        double progress = dataManager.getProgress(playerName);
        int physicalKeys = countPhysicalKeys(player);

        // Кнопка управления ключами
        inv.setItem(SLOT_KEY_MANAGEMENT, createKeyManagementItem(keys));

        // Обмен предметов
        inv.setItem(SLOT_EXCHANGE, createExchangeItem());

        // Открыть кейс
        inv.setItem(SLOT_OPEN_CASE, createOpenCaseItem());

        // Извлечение зачарования
        inv.setItem(SLOT_EXTRACT_ENCHANT, createExtractItem());

        // Тест зачарований
        inv.setItem(SLOT_TEST_ENCHANT, createTestItem());

        // Настройки
        inv.setItem(SLOT_SETTINGS, createSettingsItem());

        // Декоративные рамки
        for (int i = 0; i < DECO_SLOTS.length; i++) {
            inv.setItem(DECO_SLOTS[i], createDecoration(DECO_MATERIALS[i], DECO_COLORS[i]));
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!e.getView().getTitle().equals("§d★ Unusuality Меню ★")) return;

        e.setCancelled(true);

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) return;

        switch (item.getType()) {
            case PLAYER_HEAD     -> open(player); // обновить меню
            case TRIAL_KEY       -> handleKeyClick(player, e.getClick());
            case ENDER_CHEST     -> plugin.getCaseSelectionGUI().open(player);
            case DIAMOND_HELMET  -> plugin.getHelmetExtractorGUI().open(player);
            case COMMAND_BLOCK   -> plugin.getSettingsGUI().open(player);
            case ENDER_EYE       -> plugin.getTestEnchantment().open(player);
            case GOLD_INGOT      -> placeholder(player);
        }
    }

    private void placeholder(Player player) {
        player.sendMessage("§cПока ресурсов слишком мало, чтобы обменивать их на ключи");
    }

    private void handleKeyClick(Player player, ClickType click) {
        if (click.isLeftClick()) {
            plugin.getKeyConverter().withdrawKey(player, 1);
        } else if (click.isRightClick()) {
            plugin.getKeyConverter().depositAllKeys(player);
        }
        open(player); // обновляем меню
    }

    // ── Создание предметов интерфейса ──────────────────────────────────────

    private ItemStack createPlayerHead(Player player) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        meta.setOwningPlayer(player);
        meta.setDisplayName("§e" + player.getName());

        String name = player.getName().toLowerCase();
        int keys = dataManager.getKeys(name);
        double progress = dataManager.getProgress(name);
        int physicalKeys = countPhysicalKeys(player);
        double total = keys + progress + physicalKeys;

        meta.setLore(List.of(
                "§7Цифровые ключи: §b" + keys,
                "§7Прогресс: §b" + String.format(Locale.US, "%.0f", progress * 100) + "%",
                "§7Физические ключи: §b" + physicalKeys,
                "§7Всего: §b" + String.format("%.2f", total) + " ключ(ей)"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createKeyManagementItem(int keys) {
        ItemStack item = new ItemStack(Material.TRIAL_KEY);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Управление ключами");
        meta.setLore(List.of(
                "§7Цифровые ключи: §b" + keys,
                "",
                "§eЛКМ: §7вывести один ключ",
                "§eПКМ: §7пополнить все ключи из инвентаря"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createExchangeItem() {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6Обмен предметов");
        meta.setLore(List.of(
                "§7Обменяйте ресурсы на ключи",
                "",
                "§eКлик: §7открыть меню обмена"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createOpenCaseItem() {
        ItemStack item = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dОткрыть кейс");
        meta.setLore(List.of(
                "§7Стоимость: §c1 ключ",
                "",
                "§eКлик: §7выбрать кейс"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createExtractItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bИзвлечь зачарование");
        meta.setLore(List.of(
                "§7Извлеките необычное зачарование",
                "§7из шлема и получите его в виде книги",
                "",
                "§eКлик: §7открыть экстрактор"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTestItem() {
        ItemStack item = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bТест зачарований");
        meta.setLore(List.of(
                "§7Примерьте зачарование на",
                "§7временный шлем (15 секунд)",
                "",
                "§eКлик: §7выбрать зачарование"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSettingsItem() {
        ItemStack item = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§cНастройки");
        meta.setLore(List.of(
                "§7Настройка отображения эффектов",
                "",
                "§eКлик: §7открыть настройки"
        ));
        item.setItemMeta(meta);
        return item;
    }

    // ── Вспомогательные методы ────────────────────────────────────────────

    private int countPhysicalKeys(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory()) {
            if (plugin.getKeyConverter().isPhysicalKey(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }

    private ItemStack createDecoration(Material material, String color) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + "✦");
        item.setItemMeta(meta);
        return item;
    }
}