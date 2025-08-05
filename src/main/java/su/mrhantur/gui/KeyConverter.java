package su.mrhantur.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.mrhantur.Unusuality;

import java.util.Arrays;
import java.util.List;

public class KeyConverter {
    private final Unusuality plugin;

    public KeyConverter(Unusuality plugin) {
        this.plugin = plugin;
    }

    public void withdrawKey(Player player, int amount) {
        String playerName = player.getName().toLowerCase();
        int keys = plugin.getDataManager().getKeys(playerName);

        if (keys < amount) {
            player.sendMessage("§cУ вас недостаточно ключей!");
            return;
        }

        plugin.getDataManager().removeKeys(playerName, amount);

        ItemStack physicalKey = createPhysicalKey(amount);
        if (player.getInventory().addItem(physicalKey).size() > 0) {
            player.getWorld().dropItem(player.getLocation(), physicalKey);
        }

        player.sendMessage("§aВы вывели " + amount + " ключ(ей)!");
    }

    private ItemStack createPhysicalKey(int amount) {
        ItemStack key = new ItemStack(Material.TRIAL_KEY, amount);
        ItemMeta meta = key.getItemMeta();
        meta.setDisplayName("§6Ключ от кейса");
        meta.setLore(Arrays.asList(
                "§7Используйте этот ключ для открытия кейсов",
                "§7Чтобы перевести ключи в цифровой формат,",
                "§7нажмите ПКМ по этому предмету в главном меню"
        ));
        key.setItemMeta(meta);
        return key;
    }

    public void depositAllKeys(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory()) {
            if (isPhysicalKey(item)) {
                count += item.getAmount();
                item.setAmount(0);
            }
        }

        if (count > 0) {
            String playerName = player.getName().toLowerCase();
            plugin.getDataManager().addKeys(playerName, count);
            player.sendMessage("§aВы пополнили " + count + " ключ(ей)!");
        } else {
            player.sendMessage("§cВ вашем инвентаре нет ключей!");
        }
    }

    public boolean isPhysicalKey(ItemStack item) {
        if (item == null || item.getType() != Material.TRIAL_KEY) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null &&
                meta.hasDisplayName() &&
                meta.getDisplayName().equals("§6Ключ от кейса") &&
                meta.hasLore() &&
                meta.getLore().contains("§7Используйте этот ключ для открытия кейсов");
    }
}