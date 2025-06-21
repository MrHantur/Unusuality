package su.mrhantur;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import su.mrhantur.effects.*;

import java.util.*;

public final class Unusuality extends JavaPlugin {

    private final Map<Enchantment, UnusualEffect> effects = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("★UNUSUALITY★");
        getLogger().info("♥♦♣♠ IS ♠♣♦♥");
        getLogger().info("[■##] WORKING");

        registerEffects();

        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                timer = (timer + 1) % 200;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack helmet = player.getInventory().getHelmet();
                    if (helmet == null || !helmet.hasItemMeta()) continue;

                    ItemMeta meta = helmet.getItemMeta();
                    if (meta == null) continue;

                    for (Map.Entry<Enchantment, UnusualEffect> entry : effects.entrySet()) {
                        if (meta.hasEnchant(entry.getKey())) {
                            entry.getValue().apply(player, timer);
                            break;
                        }
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);
    }

    private void registerEffects() {
        register("fireflies", new fireflies());
        register("confetti", new confetti());
        register("green_energy", new greenEnergy());
        register("galaxy", new galaxy());
        register("restless_souls", new restlessSouls());
        register("astral_step", new astralStep());
    }

    private void register(String key, UnusualEffect effect) {
        Enchantment enchant = Enchantment.getByKey(NamespacedKey.fromString("unusuality:" + key));
        if (enchant != null) {
            effects.put(enchant, effect);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Bye bye!");
    }
}