package su.mrhantur.gui;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import su.mrhantur.Unusuality;
import su.mrhantur.UnusualityDataManager;

import java.util.*;

public class MainUnusual implements Listener {

    public final Map<UUID, Boolean> rollingPlayers = new HashMap<>();
    private final Unusuality plugin;
    private final UnusualityDataManager dataManager;

    public MainUnusual(Unusuality plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§d★ Unusuality Меню ★");
        String playerName = player.getName().toLowerCase();

        // Заполняем стеклом
        ItemStack glass = createGlassPane();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Информационная голова игрока
        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) infoItem.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName("§e" + player.getName());

        int keys = dataManager.getKeys(playerName);
        double progress = dataManager.getProgress(playerName);
        int physicalKeys = countPhysicalKeys(player);
        double total = keys + progress + physicalKeys;

        skullMeta.setLore(Arrays.asList(
                "§7Цифровые ключи: §b" + keys,
                "§7Прогресс: §b" + String.format(Locale.US, "%.2f", progress * 100) + "%",
                "§7Физические ключи: §b" + physicalKeys,
                "§7Всего: §b" + String.format("%.2f", total) + " ключ(ей)"
        ));
        infoItem.setItemMeta(skullMeta);
        inv.setItem(13, infoItem);

        // Кнопка управления ключами
        ItemStack keyItem = new ItemStack(Material.TRIAL_KEY);
        ItemMeta keyMeta = keyItem.getItemMeta();
        keyMeta.setDisplayName("§6Управление ключами");
        keyMeta.setLore(Arrays.asList(
                "§7Цифровые ключи: §b" + keys,
                "",
                "§eЛКМ: §7вывести один ключ",
                "§eПКМ: §7пополнить все ключи из инвентаря"
        ));
        keyItem.setItemMeta(keyMeta);
        inv.setItem(10, keyItem);

        // Кнопка обмена предметов
        ItemStack exchangeItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta exchangeMeta = exchangeItem.getItemMeta();
        exchangeMeta.setDisplayName("§6Обмен предметов");
        exchangeMeta.setLore(Arrays.asList(
                "§7Обменяйте ресурсы на ключи",
                "",
                "§eКлик: §7открыть меню обмена"
        ));
        exchangeItem.setItemMeta(exchangeMeta);
        inv.setItem(11, exchangeItem);

        // Кнопка открытия кейсов
        ItemStack rollItem = new ItemStack(Material.ENDER_CHEST);
        ItemMeta rollMeta = rollItem.getItemMeta();
        rollMeta.setDisplayName("§dОткрыть кейс");
        rollMeta.setLore(Arrays.asList(
                "§7Стоимость: §c1 ключ",
                "",
                "§eКлик: §7выбрать кейс"
        ));
        rollItem.setItemMeta(rollMeta);
        inv.setItem(12, rollItem);

        // Кнопка извлечения зачарований
        ItemStack extractorItem = new ItemStack(Material.DIAMOND_HELMET);
        ItemMeta extractorMeta = extractorItem.getItemMeta();
        extractorMeta.setDisplayName("§bИзвлечь зачарование");
        extractorMeta.setLore(Arrays.asList(
                "§7Извлеките необычное зачарование",
                "§7из шлема и получите его в виде книги",
                "",
                "§eКлик: §7открыть экстрактор"
        ));
        extractorItem.setItemMeta(extractorMeta);
        inv.setItem(14, extractorItem);

        // Кнопка пробы зачарования
        ItemStack testItem = new ItemStack(Material.ENDER_EYE);
        ItemMeta testMeta = testItem.getItemMeta();
        testMeta.setDisplayName("§bТест зачарований");
        testMeta.setLore(Arrays.asList(
                "§7Примерьте зачарование на",
                "§7временный шлем (15 секунд)",
                "",
                "§eКлик: §7выбрать зачарование"
        ));
        testItem.setItemMeta(testMeta);
        inv.setItem(15, testItem); // Слот между кнопками

        // Кнопка настроек
        ItemStack settingsItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName("§cНастройки");
        settingsMeta.setLore(Arrays.asList(
                "§7Настройка отображения эффектов",
                "",
                "§eКлик: §7открыть настройки"
        ));
        settingsItem.setItemMeta(settingsMeta);
        inv.setItem(16, settingsItem);

        // Декоративные элементы
        inv.setItem(0, createDecoration(Material.ORANGE_STAINED_GLASS_PANE, "§6"));
        inv.setItem(1, createDecoration(Material.YELLOW_STAINED_GLASS_PANE, "§e"));
        inv.setItem(7, createDecoration(Material.PINK_STAINED_GLASS_PANE, "§d"));
        inv.setItem(8, createDecoration(Material.MAGENTA_STAINED_GLASS_PANE, "§5"));
        inv.setItem(9, createDecoration(Material.BLUE_STAINED_GLASS_PANE, "§9"));
        inv.setItem(17, createDecoration(Material.CYAN_STAINED_GLASS_PANE, "§b"));
        inv.setItem(18, createDecoration(Material.LIME_STAINED_GLASS_PANE, "§a"));
        inv.setItem(26, createDecoration(Material.GREEN_STAINED_GLASS_PANE, "§2"));

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (e.getView().getTitle().equals("§d★ Unusuality Меню ★")) {
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            switch (item.getType()) {
                case PLAYER_HEAD:
                    open(player); // Обновляем информацию
                    break;

                case TRIAL_KEY:
                    if (e.getClick().isLeftClick()) {
                        plugin.getKeyConverter().withdrawKey(player, 1);
                        open(player);
                    } else if (e.getClick().isRightClick()) {
                        plugin.getKeyConverter().depositAllKeys(player);
                        open(player);
                    }
                    break;

                case ENDER_CHEST:
                    plugin.getCaseSelectionGUI().open(player);
                    break;

                case DIAMOND_HELMET:
                    plugin.getHelmetExtractorGUI().open(player);
                    break;

                case COMMAND_BLOCK:
                    plugin.getSettingsGUI().open(player);
                    break;

                case ENDER_EYE:
                    plugin.getTestEnchantment().open(player);
                    break;

                case GOLD_INGOT:
                    plugin.getExchangeGUI().open(player);
                    break;
            }
        }
    }

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
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
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