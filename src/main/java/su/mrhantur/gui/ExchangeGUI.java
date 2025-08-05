package su.mrhantur.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.util.ArrayList;
import java.util.List;

public class ExchangeGUI implements Listener {

    private final Unusuality plugin;
    private final List<ExchangeOffer> offers = new ArrayList<>();
    private final Inventory inv;

    public ExchangeGUI(Unusuality plugin) {
        this.plugin = plugin;
        this.inv = Bukkit.createInventory(null, 27, "§6♻ Обмен предметов");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadOffers();
        setup();
    }

    private void loadOffers() {
        // Легко расширяемый список предложений
        offers.add(new ExchangeOffer(Material.DIAMOND, 32, 1));
        offers.add(new ExchangeOffer(Material.NETHERITE_INGOT, 1, 1));
        offers.add(new ExchangeOffer(Material.EMERALD, 64, 1));
        offers.add(new ExchangeOffer(Material.NETHER_STAR, 1, 3));
        offers.add(new ExchangeOffer(Material.ELYTRA, 1, 3));
        // Добавляйте новые предложения здесь
    }

    private void setup() {
        // Заполняем стеклом
        ItemStack glass = createGlassPane();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        // Размещаем предложения обмена
        int[] slots = {10, 12, 14, 16, 19, 21, 23, 25};
        for (int i = 0; i < Math.min(offers.size(), slots.length); i++) {
            inv.setItem(slots[i], offers.get(i).createDisplayItem());
        }

        // Кнопка назад
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§f← Назад");
        backMeta.setLore(List.of("§7Вернуться в главное меню"));
        backButton.setItemMeta(backMeta);
        inv.setItem(26, backButton);

        // Информация
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§bИнформация");
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Обменяйте ресурсы на ключи");
        infoLore.add("§7для открытия необычных кейсов");
        infoLore.add("");
        infoLore.add("§8Доступные предложения:");
        infoMeta.setLore(infoLore);
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);
    }

    public void open(Player player) {
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals("§6♻ Обмен предметов")) return;

        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Кнопка "Назад"
        if (clicked.getType() == Material.ARROW) {
            plugin.getMainUnusualGUI().open(player);
            return;
        }

        // Поиск соответствующего предложения
        for (ExchangeOffer offer : offers) {
            if (clicked.getType() == offer.getMaterial() &&
                    clicked.getAmount() == offer.getAmount()) {

                processExchange(player, offer);
                return;
            }
        }
    }

    private void processExchange(Player player, ExchangeOffer offer) {
        // Проверяем наличие предметов
        if (!player.getInventory().containsAtLeast(new ItemStack(offer.getMaterial()), offer.getAmount())) {
            String materialName = offer.getMaterialName(offer.getMaterial()).toLowerCase();
            player.sendMessage(ChatColor.RED + "У вас недостаточно " + materialName + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Удаляем предметы
        ItemStack toRemove = new ItemStack(offer.getMaterial(), offer.getAmount());
        player.getInventory().removeItem(toRemove);

        // Добавляем ключи
        String playerName = player.getName().toLowerCase();
        plugin.getDataManager().addKeys(playerName, offer.getKeys());

        // Эффекты и сообщение
        String materialName = offer.getMaterialName(offer.getMaterial());
        player.sendMessage(ChatColor.GREEN + "Вы обменяли " + offer.getAmount() + " " +
                materialName + " на " + offer.getKeys() + " ключ" + getKeySuffix(offer.getKeys()));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);
    }

    private String getKeySuffix(int keys) {
        if (keys % 10 == 1 && keys % 100 != 11) return "";
        if (keys % 10 >= 2 && keys % 10 <= 4 &&
                (keys % 100 < 10 || keys % 100 >= 20)) return "а";
        return "ей";
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }
}