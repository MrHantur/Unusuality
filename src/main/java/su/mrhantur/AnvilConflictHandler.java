package su.mrhantur;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class AnvilConflictHandler implements Listener {

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack left = inventory.getItem(0);
        ItemStack right = inventory.getItem(1);

        if (left == null || right == null) {
            return;
        }

        boolean leftHasUnusual = hasUnusual(left);
        boolean rightHasUnusual = hasUnusual(right);

        if (leftHasUnusual && rightHasUnusual) {
            event.setResult(null); // Удаляем результат
        }
    }

    private boolean hasUnusual(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        Map<Enchantment, Integer> enchants;

        if (meta instanceof EnchantmentStorageMeta bookMeta) {
            enchants = bookMeta.getStoredEnchants();
        } else {
            enchants = meta.getEnchants();
        }

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment ench = entry.getKey();
            NamespacedKey key = ench.getKey();
            if (key.getNamespace().equalsIgnoreCase("unusuality")) {
                return true;
            }
        }

        return false;
    }

}
