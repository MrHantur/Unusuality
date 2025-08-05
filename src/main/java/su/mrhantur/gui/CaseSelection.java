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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CaseSelection implements Listener {
    private final Unusuality plugin;
    private final CaseOpener caseOpener;

    public CaseSelection(Unusuality plugin, CaseOpener caseOpener) {
        this.plugin = plugin;
        this.caseOpener = caseOpener;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        List<EnchantmentSeries> availableSeries = new ArrayList<>(plugin.getSeriesManager().getAvailableSeries());

        availableSeries.sort(Comparator.comparingInt(series -> {
            String name = series.getDisplayName().replaceAll("§.", "");
            try {
                int index = name.indexOf("#");
                if (index != -1) {
                    return Integer.parseInt(name.substring(index + 1).replaceAll("[^0-9]", ""));
                }
            } catch (Exception ignored) {}
            return 0;
        }));


        // Определяем размер инвентаря (минимум 27 слотов)
        int size = Math.max(27, ((availableSeries.size() + 8) / 9) * 9);

        Inventory inv = Bukkit.createInventory(null, size, "§6⚡ Выбор кейса ⚡");

        // Заполняем стеклом
        ItemStack glass = createGlassPane();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Размещаем серии
        int[] slots = {10, 12, 14, 16, 19, 21, 23, 25}; // Красивые позиции
        for (int i = 0; i < availableSeries.size() && i < slots.length; i++) {
            EnchantmentSeries series = availableSeries.get(i);
            inv.setItem(slots[i], series.createDisplayItem());
        }

        // Кнопка назад
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§f← Назад");
        backMeta.setLore(List.of("§7Вернуться в главное меню"));
        backButton.setItemMeta(backMeta);
        inv.setItem(size - 1, backButton);

        // Информация о игроке
        String playerName = player.getName().toLowerCase();
        int keys = plugin.getDataManager().getKeys(playerName);
        double progress = plugin.getDataManager().getProgress(playerName);

        ItemStack infoItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) infoItem.getItemMeta(); // Получаем SkullMeta

        // Устанавливаем владельца головы (игрока)
        skullMeta.setOwningPlayer(player);

        // Добавляем информацию о физических ключах
        int physicalKeys = countPhysicalKeys(player);
        // Устанавливаем имя и описание
        skullMeta.setDisplayName("§e" + player.getName());
        double total = keys + progress + physicalKeys;
        skullMeta.setLore(List.of(
                "§7Ключи: §b" + keys,
                "§7Прогресс: §b" + progress * 100 + "%",
                "§7Физические ключи: §b" + physicalKeys,
                "§7Всего: §b" + String.format("%.2f", total) + " ключ(ей)"
        ));

        infoItem.setItemMeta(skullMeta); // Применяем изменения
        inv.setItem(4, infoItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.equals("§6⚡ Выбор кейса ⚡")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Обработка кнопки назад
        if (clickedItem.getType() == Material.ARROW) {
            plugin.getMainUnusualGUI().open(player);
            return;
        }

        // Обработка информации о игроке
        if (clickedItem.getType() == Material.PLAYER_HEAD) {
            String playerName = player.getName().toLowerCase();
            int keys = plugin.getDataManager().getKeys(playerName);
            double progress = plugin.getDataManager().getProgress(playerName);

            // Добавляем информацию о физических ключах
            int physicalKeys = countPhysicalKeys(player);

            player.sendMessage("§7Цифровые ключи: §b" + keys);
            player.sendMessage("§7Прогресс: §b" + progress * 100 + "%");
            player.sendMessage("§7Физические ключи: §b" + physicalKeys);
            return;
        }

        // Обработка стекла
        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // Поиск серии по предмету
        EnchantmentSeries selectedSeries = null;
        for (EnchantmentSeries series : plugin.getSeriesManager().getAllSeries()) {
            if (series.getDisplayMaterial() == clickedItem.getType()) {
                ItemMeta clickedMeta = clickedItem.getItemMeta();
                if (clickedMeta != null && clickedMeta.getDisplayName().equals(series.getDisplayName())) {
                    selectedSeries = series;
                    break;
                }
            }
        }

        if (selectedSeries == null) return;

        // Обработка ЛКМ/ПКМ
        if (event.getClick() == ClickType.LEFT) {
            player.closeInventory();
            caseOpener.openCase(player, selectedSeries);
        } else if (event.getClick() == ClickType.RIGHT) {
            // Проверяем текущий вид предмета
            if (isInfoItem(clickedItem)) {
                // Если это информационный вид - возвращаем обычный
                event.getInventory().setItem(event.getSlot(), selectedSeries.createDisplayItem());
            } else {
                // Если это обычный вид - показываем информацию
                event.getInventory().setItem(event.getSlot(), selectedSeries.createInfoItem());
            }
        }
    }

    // Проверка, является ли предмет информационным
    private boolean isInfoItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return false;

        List<String> lore = meta.getLore();
        // Проверяем наличие маркера информационного предмета
        return !lore.isEmpty() && lore.get(2).contains("Список зачарований:");
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        return glass;
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
}