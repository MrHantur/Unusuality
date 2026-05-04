package su.mrhantur.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import su.mrhantur.Unusuality;
import su.mrhantur.series.EnchantmentSeries;

import java.util.*;

public class CaseSelection implements Listener {
    private final Unusuality plugin;
    private final CaseOpener caseOpener;

    // Порядок серий по их идентификаторам (чем меньше число, тем раньше)
    private static final Map<String, Integer> SERIES_ORDER = Map.of(
            "first",  1,
            "second", 2,
            "third",  3,
            "fourth", 4
    );

    public CaseSelection(Unusuality plugin, CaseOpener caseOpener) {
        this.plugin = plugin;
        this.caseOpener = caseOpener;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        List<EnchantmentSeries> availableSeries = new ArrayList<>(plugin.getSeriesManager().getAvailableSeries());

        // Сортировка по заранее заданному порядку
        availableSeries.sort(Comparator.comparingInt(series ->
                SERIES_ORDER.getOrDefault(series.getId(), Integer.MAX_VALUE)));

        int size = Math.max(27, ((availableSeries.size() + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(null, size, "§6⚡ Выбор кейса ⚡");

        // Заполнение фона стеклом
        ItemStack glass = createGlassPane();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Размещение кнопок серий в красивых слотах
        int[] slots = {10, 12, 14, 16, 19, 21, 23, 25};
        for (int i = 0; i < availableSeries.size() && i < slots.length; i++) {
            inv.setItem(slots[i], availableSeries.get(i).createDisplayItem());
        }

        // Кнопка «Назад»
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§f← Назад");
        backMeta.setLore(List.of("§7Вернуться в главное меню"));
        backButton.setItemMeta(backMeta);
        inv.setItem(size - 1, backButton);

        // Информационная голова игрока
        String playerName = player.getName().toLowerCase();
        int keys = plugin.getDataManager().getKeys(playerName);
        double progress = plugin.getDataManager().getProgress(playerName);
        int physicalKeys = countPhysicalKeys(player);

        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) infoItem.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName("§e" + player.getName());
        double total = keys + progress + physicalKeys;
        skullMeta.setLore(List.of(
                "§7Ключи: §b" + keys,
                "§7Прогресс: §b" + String.format("%.0f", progress * 100) + "%",
                "§7Физические ключи: §b" + physicalKeys,
                "§7Всего: §b" + String.format("%.2f", total) + " ключ(ей)"
        ));
        infoItem.setItemMeta(skullMeta);
        inv.setItem(4, infoItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("§6⚡ Выбор кейса ⚡")) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Кнопка «Назад»
        if (clicked.getType() == Material.ARROW) {
            plugin.getMainUnusualGUI().open(player);
            return;
        }

        // Голова игрока — вывести подробности в чат
        if (clicked.getType() == Material.PLAYER_HEAD) {
            String playerName = player.getName().toLowerCase();
            int keys = plugin.getDataManager().getKeys(playerName);
            double progress = plugin.getDataManager().getProgress(playerName);
            int physicalKeys = countPhysicalKeys(player);
            player.sendMessage("§7Цифровые ключи: §b" + keys);
            player.sendMessage("§7Прогресс: §b" + String.format("%.0f", progress * 100) + "%");
            player.sendMessage("§7Физические ключи: §b" + physicalKeys);
            return;
        }

        // Стекло — ничего не делаем
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Поиск серии по предмету (материал + имя)
        EnchantmentSeries selectedSeries = null;
        for (EnchantmentSeries series : plugin.getSeriesManager().getAllSeries()) {
            if (series.getDisplayMaterial() == clicked.getType()) {
                ItemMeta clickedMeta = clicked.getItemMeta();
                if (clickedMeta != null && clickedMeta.getDisplayName().equals(series.getDisplayName())) {
                    selectedSeries = series;
                    break;
                }
            }
        }

        if (selectedSeries == null) return;

        // ЛКМ — открыть кейс, ПКМ — переключить вид предмета (описание/обычный)
        if (event.getClick() == ClickType.LEFT) {
            player.closeInventory();
            caseOpener.openCase(player, selectedSeries);
        } else if (event.getClick() == ClickType.RIGHT) {
            if (isInfoItem(clicked)) {
                event.getInventory().setItem(event.getSlot(), selectedSeries.createDisplayItem());
            } else {
                event.getInventory().setItem(event.getSlot(), selectedSeries.createInfoItem());
            }
        }
    }

    /** Проверяет, является ли предмет информационной версией (с описанием зачарований) */
    private boolean isInfoItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;
        List<String> lore = meta.getLore();
        // В info‑предмете третья строка лора содержит список зачарований
        return lore.size() > 2 && lore.get(2).contains("Список зачарований:");
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        return glass;
    }

    // Считает физические ключи (из KeyConverter) в инвентаре игрока
    private int countPhysicalKeys(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory()) {
            if (plugin.getKeyConverter().isPhysicalKey(item)) {
                count += item.getAmount();
            }
        }
        return count;
    }
}