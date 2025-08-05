package su.mrhantur.effects;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.mrhantur.effects.bloodPact;

public class bloodPactListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Проверяем, был ли убийца игроком
        if (!(event.getEntity().getKiller() instanceof Player)) return;

        Player killer = (Player) event.getEntity().getKiller();

        // Проверяем, есть ли на шлеме энчант bloodpact
        ItemStack helmet = killer.getInventory().getHelmet();
        if (helmet == null || !helmet.hasItemMeta()) return;

        ItemMeta meta = helmet.getItemMeta();
        if (meta == null) return;

        // Проверяем наличие энчанта bloodpact
        Enchantment bloodpactEnchant = Enchantment.getByKey(NamespacedKey.fromString("unusuality:blood_pact"));
        if (bloodpactEnchant != null && meta.hasEnchant(bloodpactEnchant)) {
            // Активируем эффект кровавого пакта
            bloodPact.activateBloodpact(killer.getName());
        }
    }
}