package su.mrhantur;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public final class Unusuality extends JavaPlugin {

    Integer timer = 0;

    @Override
    public void onEnable() {
        getLogger().info("★UNUSUALITY★");
        getLogger().info("♥♦♣♠ IS ♠♣♦♥");
        getLogger().info("[■##] WORKING");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ItemStack helmet = player.getInventory().getHelmet();
                    if (helmet != null && helmet.hasItemMeta()) {
                        ItemMeta meta = helmet.getItemMeta();
                        unusualChecker(player, meta);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1);
    }

    @Override
    public void onDisable() {
        getLogger().info("Bye bye!");
    }

    private void unusualChecker(Player player, ItemMeta meta) {
        timer++;
        timer %= 200;

        Enchantment firefliesEnchant = Enchantment.getByKey(NamespacedKey.fromString("unusuality:fireflies"));
        Enchantment confettiEnchant = Enchantment.getByKey(NamespacedKey.fromString("unusuality:confetti"));

        if (meta.hasEnchant(firefliesEnchant)) {
            uFireflies(player);
        } else if (meta.hasEnchant(confettiEnchant)) {
            uConfetti(player);
        }
    }

    private final Particle.DustOptions fireDust = new Particle.DustOptions(Color.fromRGB(255, 255, 0), 0.5f);

    private void uFireflies(Player player) {
        if (timer % 2 != 0) return;

        double R = 0.5;
        double r = 0.2;
        double k = 6;
        int segments = 2;

        Location base = player.getLocation().add(0, 2.3, 0);

        for (int i = 0; i < segments; i++) {
            double t = ((timer % 200) + i) / 40.0 * Math.PI;

            double x1 = (R + r * Math.cos(k * t)) * Math.cos(t);
            double z1 = (R + r * Math.cos(k * t)) * Math.sin(t);
            double y1 = r * Math.sin(k * t);

            double x2 = (R + r * Math.cos(k * t + Math.PI)) * Math.cos(t);
            double z2 = (R + r * Math.cos(k * t + Math.PI)) * Math.sin(t);
            double y2 = r * Math.sin(k * t + Math.PI);

            Location p1 = base.clone().add(x1, y1, z1);
            Location p2 = base.clone().add(x2, y2, z2);

            player.getWorld().spawnParticle(Particle.DUST, p1, 0, 0, 0, 0, fireDust);
            player.getWorld().spawnParticle(Particle.DUST, p2, 0, 0, 0, 0, fireDust);
        }
    }

    private final Random random = new Random();

    private void uConfetti(Player player) {
        if (timer % 8 != 0) return; // "взрывы" раз в 8 тиков (~0.4 сек)

        Location origin = player.getLocation().add(0, 2.4, 0); // над головой
        int particles = 15 + random.nextInt(6); // от 15 до 20 частиц в "взрыве"

        for (int i = 0; i < particles; i++) {
            // Случайное направление в 3D (в стороны + немного вверх)
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 0.2 + random.nextDouble() * 0.1;

            double dx = Math.cos(angle) * speed;
            double dz = Math.sin(angle) * speed;
            double dy = 0.05 + random.nextDouble() * 0.15;

            // Случайный яркий цвет
            Color color = Color.fromRGB(
                    100 + random.nextInt(156),
                    100 + random.nextInt(156),
                    100 + random.nextInt(156)
            );
            Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);

            // Создаём частицу с направлением
            player.getWorld().spawnParticle(
                    Particle.DUST,
                    origin,
                    1,
                    dx, dy, dz,
                    0,
                    dust
            );
        }
    }
}
