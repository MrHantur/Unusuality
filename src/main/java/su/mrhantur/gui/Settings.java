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

    public Settings(Unusuality plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§9⚙ Настройки эффектов ⚙");
        String playerName = player.getName().toLowerCase();

        // Заполняем стеклом
        ItemStack glass = createGlassPane();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Настройка 1: Показ ваших эффектов
        boolean showEffectPlayer = dataManager.getShowEffectPlayer(playerName);
        ItemStack showMineItem = createSettingItem(
                showEffectPlayer ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e⭐ Показ ваших эффектов",
                showEffectPlayer,
                List.of(
                        "§7Включает/выключает отображение",
                        "§7эффектов ваших необычных предметов",
                        "§7для вас самих",
                        "",
                        "§8Клик для переключения"
                )
        );
        inv.setItem(11, showMineItem);

        // Настройка 2: Показ чужих эффектов
        boolean showAllEffects = dataManager.getShowAllEffects(playerName);
        ItemStack showOthersItem = createSettingItem(
                showAllEffects ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e👥 Показ чужих эффектов",
                showAllEffects,
                List.of(
                        "§7Включает/выключает отображение",
                        "§7эффектов необычных предметов",
                        "§7других игроков",
                        "",
                        "§8Клик для переключения"
                )
        );
        inv.setItem(13, showOthersItem);

        // Настройка 3: Видимость ваших эффектов для других
        boolean canSeeMyEffect = dataManager.getCanSeeMyEffect(playerName);
        ItemStack visibilityItem = createSettingItem(
                canSeeMyEffect ? Material.LIME_DYE : Material.GRAY_DYE,
                "§e🔍 Видимость для себя",
                canSeeMyEffect,
                List.of(
                        "§7Разрешает/запрещает вам, но не",
                        "§7другим игрокам видеть ваши",
                        "§7эффекты необычных предметов",
                        "",
                        "§8Клик для переключения"
                )
        );
        inv.setItem(15, visibilityItem);

        // Информационная панель
        ItemStack infoItem = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§b📖 Информация");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Текущие настройки:");
        infoLore.add("§7• Ваши эффекты: " + (showEffectPlayer ? "§aВКЛ" : "§cВЫКЛ"));
        infoLore.add("§7• Чужие эффекты: " + (showAllEffects ? "§aВКЛ" : "§cВЫКЛ"));
        infoLore.add("§7• Видимость для других: " + (canSeeMyEffect ? "§aВКЛ" : "§cВЫКЛ"));
        infoLore.add("");
        infoLore.add("§8Эти настройки влияют только на");
        infoLore.add("§8отображение визуальных эффектов");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inv.setItem(4, infoItem);

        // Кнопка "Назад"
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§f← Назад");
        backMeta.setLore(List.of("§7Вернуться в главное меню"));
        backButton.setItemMeta(backMeta);
        inv.setItem(26, backButton);

        // Кнопка "Сбросить настройки"
        ItemStack resetButton = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = resetButton.getItemMeta();
        resetMeta.setDisplayName("§c🔄 Сбросить настройки");
        resetMeta.setLore(List.of(
                "§7Сбросить все настройки",
                "§7к значениям по умолчанию",
                "",
                "§8Клик для сброса"
        ));
        resetButton.setItemMeta(resetMeta);
        inv.setItem(22, resetButton);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.equals("§9⚙ Настройки эффектов ⚙")) return;

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String playerName = player.getName().toLowerCase();

        // Обработка кнопки "Назад"
        if (clickedItem.getType() == Material.ARROW) {
            plugin.getMainUnusualGUI().open(player);
            return;
        }

        // Обработка информационной панели
        if (clickedItem.getType() == Material.KNOWLEDGE_BOOK) {
            // Просто обновляем GUI для актуальной информации
            open(player);
            return;
        }

        // Обработка кнопки сброса
        if (clickedItem.getType() == Material.BARRIER) {
            // Сбрасываем все настройки к значениям по умолчанию
            dataManager.setShowEffectPlayer(playerName, true);
            dataManager.setShowAllEffects(playerName, true);
            dataManager.setCanSeeMyEffect(playerName, true);

            player.sendMessage("§a✅ Настройки сброшены к значениям по умолчанию!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);

            // Обновляем GUI
            open(player);
            return;
        }

        // Обработка стекла
        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // Обработка настроек
        if (clickedItem.getType() == Material.LIME_DYE || clickedItem.getType() == Material.GRAY_DYE) {
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta == null) return;

            String displayName = meta.getDisplayName();

            if (displayName.contains("⭐ Показ ваших эффектов")) {
                boolean current = dataManager.getShowEffectPlayer(playerName);
                dataManager.setShowEffectPlayer(playerName, !current);

                player.sendMessage("§eПоказ ваших эффектов " +
                        (current ? "§cвыключен" : "§aвключен"));

            } else if (displayName.contains("👥 Показ чужих эффектов")) {
                boolean current = dataManager.getShowAllEffects(playerName);
                dataManager.setShowAllEffects(playerName, !current);

                player.sendMessage("§eПоказ чужих эффектов " +
                        (current ? "§cвыключен" : "§aвключен"));

            } else if (displayName.contains("🔍 Видимость для других")) {
                boolean current = dataManager.getCanSeeMyEffect(playerName);
                dataManager.setCanSeeMyEffect(playerName, !current);

                player.sendMessage("§eВидимость ваших эффектов для себя " +
                        (current ? "§cзапрещена" : "§aразрешена"));
            }

            // Воспроизводим звук
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);

            // Обновляем GUI
            open(player);
        }
    }

    private ItemStack createSettingItem(Material material, String name, boolean enabled, List<String> baseLore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);

        List<String> lore = new ArrayList<>(baseLore);
        lore.add("");
        lore.add("§7Статус: " + (enabled ? "§aВКЛЮЧЕНО" : "§cВЫКЛЮЧЕНО"));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        return glass;
    }
}