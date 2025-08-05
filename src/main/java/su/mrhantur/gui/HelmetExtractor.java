package su.mrhantur.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import su.mrhantur.Unusuality;

import java.util.*;

public class HelmetExtractor implements Listener {

    private final Unusuality plugin;
    private final Inventory inv;
    private final int HELMET_SLOT = 10;
    private final int EXTRACT_BUTTON = 16;
    private final int BACK_BUTTON = 26;
    private final int INFO_SLOT = 13;

    // Список всех материалов шлемов
    private static final List<Material> HELMET_TYPES = Arrays.asList(
            Material.LEATHER_HELMET,
            Material.CHAINMAIL_HELMET,
            Material.IRON_HELMET,
            Material.GOLDEN_HELMET,
            Material.DIAMOND_HELMET,
            Material.NETHERITE_HELMET,
            Material.TURTLE_HELMET,
            Material.CARVED_PUMPKIN
    );

    public HelmetExtractor(Unusuality plugin) {
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(null, 27, "§dИзвлечение зачарования");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setup();
    }

    private void setup() {
        // Заполняем стеклом
        ItemStack glass = createGlassPane();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Слот для шлема - барьер
        inv.setItem(HELMET_SLOT, createPlaceholder());

        // Кнопка извлечения
        ItemStack extractButton = new ItemStack(Material.DIAMOND);
        ItemMeta extractMeta = extractButton.getItemMeta();
        extractMeta.setDisplayName("§6Извлечь зачарование");
        List<String> extractLore = new ArrayList<>();
        extractLore.add("§7Стоимость: §e50 уровней опыта");
        extractLore.add("§8Клик для извлечения");
        extractMeta.setLore(extractLore);
        extractButton.setItemMeta(extractMeta);
        inv.setItem(EXTRACT_BUTTON, extractButton);

        // Кнопка "Назад"
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§f← Назад");
        backMeta.setLore(List.of("§7Вернуться в главное меню"));
        backButton.setItemMeta(backMeta);
        inv.setItem(BACK_BUTTON, backButton);

        // Информация
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§bИнформация");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Поместите шлем с необычным зачарованием");
        infoLore.add("§7в слот слева. Извлечение зачарования");
        infoLore.add("§7вернет книгу с этим зачарованием");
        infoLore.add("§7и вернет шлем без зачарования");
        infoLore.add("");
        infoLore.add("§8Поддерживаемые шлемы:");
        infoLore.add("§8- Кожаные, кольчужные, железные");
        infoLore.add("§8- Золотые, алмазные, незеритовые");
        infoLore.add("§8- Черепашьи панцири, тыквы");
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(INFO_SLOT, info);
    }

    public void open(Player player) {
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§dИзвлечение зачарования")) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Разрешаем взаимодействие с инвентарем игрока
        if (event.getRawSlot() >= inv.getSize()) {
            return;
        }

        // Отменяем все клики в GUI
        event.setCancelled(true);

        // Клик в слот шлема
        if (slot == HELMET_SLOT) {
            // Если в слоте шлем - возвращаем его игроку
            if (clickedItem != null && HELMET_TYPES.contains(clickedItem.getType())) {
                if (player.getInventory().addItem(clickedItem).isEmpty()) {
                    inv.setItem(HELMET_SLOT, createPlaceholder());
                } else {
                    player.sendMessage(ChatColor.RED + "Не хватает места в инвентаре!");
                }
                return;
            }

            // Если игрок кликает с курсором (пытается поместить предмет)
            if (cursor != null && cursor.getType() != Material.AIR) {
                // Проверяем, что это шлем
                if (!HELMET_TYPES.contains(cursor.getType())) {
                    player.sendMessage(ChatColor.RED + "Вы можете поместить только шлем!");
                    return;
                }

                // Проверяем наличие необычного зачарования
                boolean hasUnusual = false;
                if (cursor.hasItemMeta()) {
                    ItemMeta meta = cursor.getItemMeta();
                    for (Enchantment ench : meta.getEnchants().keySet()) {
                        if (plugin.isUnusualEnchantment(ench)) {
                            hasUnusual = true;
                            break;
                        }
                    }
                }

                if (!hasUnusual) {
                    player.sendMessage(ChatColor.RED + "Этот шлем не имеет необычного зачарования!");
                    return;
                }

                // Помещаем шлем в слот
                ItemStack helmet = cursor.clone();
                helmet.setAmount(1);
                inv.setItem(HELMET_SLOT, helmet);

                // Убираем один шлем с курсора
                if (cursor.getAmount() > 1) {
                    cursor.setAmount(cursor.getAmount() - 1);
                } else {
                    player.setItemOnCursor(null);
                }
            }
        }
        // Кнопка "Назад"
        else if (slot == BACK_BUTTON) {
            // Возвращаем шлем при выходе
            returnHelmetIfPresent(player);
            // Открываем главное меню
            plugin.getMainUnusualGUI().open(player);
        }
        // Кнопка извлечения
        else if (slot == EXTRACT_BUTTON) {
            ItemStack helmet = inv.getItem(HELMET_SLOT);
            if (helmet == null || !HELMET_TYPES.contains(helmet.getType())) {
                player.sendMessage(ChatColor.RED + "Сначала поместите шлем!");
                return;
            }

            // Проверяем наличие необычного зачарования
            Enchantment unusualEnchant = null;
            ItemMeta helmetMeta = helmet.getItemMeta();
            if (helmetMeta != null) {
                for (Enchantment ench : helmetMeta.getEnchants().keySet()) {
                    if (plugin.isUnusualEnchantment(ench)) {
                        unusualEnchant = ench;
                        break;
                    }
                }
            }

            if (unusualEnchant == null) {
                player.sendMessage(ChatColor.RED + "Этот шлем не имеет необычного зачарования!");
                return;
            }

            // Проверяем уровень игрока
            if (player.getLevel() < 50) {
                player.sendMessage(ChatColor.RED + "У вас недостаточно опыта! Нужно 50 уровней");
                return;
            }

            // Отнимаем 50 уровней
            player.setLevel(player.getLevel() - 50);

            // Создаем книгу с зачарованием
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) book.getItemMeta();
            bookMeta.addStoredEnchant(unusualEnchant, 1, true);
            book.setItemMeta(bookMeta);

            // Удаляем необычное зачарование со шлема
            if (helmetMeta != null) {
                helmetMeta.removeEnchant(unusualEnchant);
                helmet.setItemMeta(helmetMeta);
            }

            // Возвращаем шлем без зачарования и выдаем книгу
            player.getInventory().addItem(helmet, book);
            inv.setItem(HELMET_SLOT, createPlaceholder());

            // Эффекты и сообщение
            player.sendMessage(ChatColor.GREEN + "Зачарование успешно извлечено! Шлем возвращен без зачарования");
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().equals("§dИзвлечение зачарования")) {
            // Отменяем перетаскивание в GUI
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("§dИзвлечение зачарования")) return;
        returnHelmetIfPresent((Player) event.getPlayer());
    }

    private void returnHelmetIfPresent(Player player) {
        // Возвращаем шлем, если он был в слоте
        ItemStack helmet = inv.getItem(HELMET_SLOT);
        if (helmet != null && HELMET_TYPES.contains(helmet.getType())) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(helmet);
            if (!leftover.isEmpty()) {
                // Если не хватило места, бросаем шлем на землю
                player.getWorld().dropItem(player.getLocation(), helmet);
            }
            inv.setItem(HELMET_SLOT, createPlaceholder());
        }
    }

    private ItemStack createPlaceholder() {
        ItemStack placeholder = new ItemStack(Material.BARRIER);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName("§7Поместите шлем сюда");
        placeholder.setItemMeta(meta);
        return placeholder;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        return glass;
    }
}