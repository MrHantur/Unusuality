package su.mrhantur.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.mrhantur.Unusuality;
import su.mrhantur.UnusualityDataManager;

import java.util.ArrayList;
import java.util.List;

public class Settings implements Listener {

    private final Unusuality plugin;
    private final UnusualityDataManager dataManager;

    // Размер GUI
    private static final int GUI_SIZE = 36;

    // Слоты элементов
    private static final int SLOT_INFO          = 4;
    private static final int SLOT_SHOW_MINE     = 12;   // Показ ваших эффектов
    private static final int SLOT_SHOW_OTHERS   = 14;   // Показ чужих эффектов
    private static final int SLOT_SHOW_TO_OTHERS= 20;   // Видимость для других
    private static final int SLOT_OFFER_ANOTHER = 22;   // Предложить открыть ещё
    private static final int SLOT_ANNOUNCE      = 24;   // Писать в чат о джекпоте
    private static final int SLOT_RESET         = 31;
    private static final int SLOT_BACK          = 35;

    public Settings(Unusuality plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, "§9⚙ Настройки эффектов ⚙");
        String playerName = player.getName().toLowerCase();

        // Фон
        ItemStack glass = createGlassPane();
        for (int i = 0; i < GUI_SIZE; i++) {
            inv.setItem(i, glass);
        }

        // Текущие значения
        boolean showMine       = dataManager.getShowEffectPlayer(playerName);
        boolean showOthers     = dataManager.getShowAllEffects(playerName);
        boolean visibleToOthers= dataManager.getCanSeeMyEffect(playerName);
        boolean offerAnother   = dataManager.getOfferAnotherCase(playerName);
        boolean announce       = dataManager.getAnnounceJackpot(playerName);

        // Настройка 1: Показ ваших эффектов
        inv.setItem(SLOT_SHOW_MINE, createSettingItem(
                showMine ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e⭐ Показ ваших эффектов",
                showMine,
                List.of("§7Включает/выключает отображение",
                        "§7эффектов ваших необычных предметов",
                        "§7для вас самих")
        ));

        // Настройка 2: Показ чужих эффектов
        inv.setItem(SLOT_SHOW_OTHERS, createSettingItem(
                showOthers ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e👥 Показ чужих эффектов",
                showOthers,
                List.of("§7Включает/выключает отображение",
                        "§7эффектов необычных предметов",
                        "§7других игроков")
        ));

        // Настройка 3: Видимость ваших эффектов для других (исправлено!)
        inv.setItem(SLOT_SHOW_TO_OTHERS, createSettingItem(
                visibleToOthers ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e👁 Показ ваших эффектов другим",
                visibleToOthers,
                List.of("§7Разрешает/запрещает другим игрокам",
                        "§7видеть эффекты ваших необычных",
                        "§7предметов")
        ));

        // Настройка 4: Предложить открыть ещё кейс
        inv.setItem(SLOT_OFFER_ANOTHER, createSettingItem(
                offerAnother ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e🔄 Открыть ещё кейс",
                offerAnother,
                List.of("§7После открытия кейса предлагает",
                        "§7открыть ещё один, если у вас",
                        "§7остались ключи")
        ));

        // Настройка 5: Писать в чат о джекпоте
        inv.setItem(SLOT_ANNOUNCE, createSettingItem(
                announce ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e📢 Оповещение о джекпоте",
                announce,
                List.of("§7При получении необычного зачарования",
                        "§7отправляет сообщение в общий чат")
        ));

        // Информационная панель
        inv.setItem(SLOT_INFO, createInfoItem(showMine, showOthers, visibleToOthers, offerAnother, announce));

        // Кнопка сброса
        inv.setItem(SLOT_RESET, createResetButton());

        // Кнопка «Назад»
        inv.setItem(SLOT_BACK, createBackButton());

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("§9⚙ Настройки эффектов ⚙")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String playerName = player.getName().toLowerCase();

        // Назад
        if (clicked.getType() == Material.ARROW) {
            plugin.getMainUnusualGUI().open(player);
            return;
        }

        // Сброс
        if (clicked.getType() == Material.BARRIER) {
            dataManager.setShowEffectPlayer(playerName, true);
            dataManager.setShowAllEffects(playerName, true);
            dataManager.setCanSeeMyEffect(playerName, true);
            dataManager.setOfferAnotherCase(playerName, false);
            dataManager.setAnnounceJackpot(playerName, false);

            player.sendMessage("§a✅ Настройки сброшены к значениям по умолчанию!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
            open(player);
            return;
        }

        // Информационная панель (обновление)
        if (clicked.getType() == Material.KNOWLEDGE_BOOK) {
            open(player);
            return;
        }

        // Стекло
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // Переключатели
        if (clicked.getType() == Material.LIME_DYE || clicked.getType() == Material.GRAY_DYE) {
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null) return;
            String name = meta.getDisplayName();

            boolean toggled = false;
            if (name.contains("Показ ваших эффектов") && !name.contains("другим")) { // ⭐
                boolean cur = dataManager.getShowEffectPlayer(playerName);
                dataManager.setShowEffectPlayer(playerName, !cur);
                player.sendMessage("§eПоказ ваших эффектов " + (cur ? "§cвыключен" : "§aвключен"));
                toggled = true;
            } else if (name.contains("Показ чужих эффектов")) { // 👥
                boolean cur = dataManager.getShowAllEffects(playerName);
                dataManager.setShowAllEffects(playerName, !cur);
                player.sendMessage("§eПоказ чужих эффектов " + (cur ? "§cвыключен" : "§aвключен"));
                toggled = true;
            } else if (name.contains("Показ ваших эффектов другим")) { // 👁
                boolean cur = dataManager.getCanSeeMyEffect(playerName);
                dataManager.setCanSeeMyEffect(playerName, !cur);
                player.sendMessage("§eПоказ ваших эффектов другим " + (cur ? "§cзапрещён" : "§aразрешён"));
                toggled = true;
            } else if (name.contains("Открыть ещё кейс")) { // 🔄
                boolean cur = dataManager.getOfferAnotherCase(playerName);
                dataManager.setOfferAnotherCase(playerName, !cur);
                player.sendMessage("§eПредложение открыть ещё кейс " + (cur ? "§cвыключено" : "§aвключено"));
                toggled = true;
            } else if (name.contains("Оповещение о джекпоте")) { // 📢
                boolean cur = dataManager.getAnnounceJackpot(playerName);
                dataManager.setAnnounceJackpot(playerName, !cur);
                player.sendMessage("§eОповещение о джекпоте " + (cur ? "§cвыключено" : "§aвключено"));
                toggled = true;
            }

            if (toggled) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                open(player);
            }
        }
    }

    private ItemStack createSettingItem(Material material, String name, boolean enabled, List<String> desc) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>(desc);
        lore.add("");
        lore.add("§7Статус: " + (enabled ? "§aВКЛЮЧЕНО" : "§cВЫКЛЮЧЕНО"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInfoItem(boolean showMine, boolean showOthers, boolean visibleToOthers,
                                     boolean offerAnother, boolean announce) {
        ItemStack info = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§b📖 Информация");
        List<String> lore = new ArrayList<>();
        lore.add("§7Текущие настройки:");
        lore.add("§7• Ваши эффекты: " + (showMine ? "§aВКЛ" : "§cВЫКЛ"));
        lore.add("§7• Чужие эффекты: " + (showOthers ? "§aВКЛ" : "§cВЫКЛ"));
        lore.add("§7• Видимость для других: " + (visibleToOthers ? "§aВКЛ" : "§cВЫКЛ"));
        lore.add("§7• Предложить ещё кейс: " + (offerAnother ? "§aВКЛ" : "§cВЫКЛ"));
        lore.add("§7• Оповещение о джекпоте: " + (announce ? "§aВКЛ" : "§cВЫКЛ"));
        lore.add("");
        lore.add("§8Эти настройки влияют только на");
        lore.add("§8отображение визуальных эффектов");
        meta.setLore(lore);
        info.setItemMeta(meta);
        return info;
    }

    private ItemStack createResetButton() {
        ItemStack button = new ItemStack(Material.BARRIER);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName("§c🔄 Сбросить настройки");
        meta.setLore(List.of("§7Сбросить все настройки", "§7к значениям по умолчанию"));
        button.setItemMeta(meta);
        return button;
    }

    private ItemStack createBackButton() {
        ItemStack button = new ItemStack(Material.ARROW);
        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName("§f← Назад");
        meta.setLore(List.of("§7Вернуться в главное меню"));
        button.setItemMeta(meta);
        return button;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }
}