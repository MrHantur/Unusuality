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
        Enchantment greenEnergyEnchant = Enchantment.getByKey(NamespacedKey.fromString("unusuality:green_energy"));
        Enchantment galaxyEnchant = Enchantment.getByKey(NamespacedKey.fromString("unusuality:galaxy"));

        if (meta.hasEnchant(firefliesEnchant)) {
            uFireflies(player);
        } else if (meta.hasEnchant(confettiEnchant)) {
            uConfetti(player);
        } else if (meta.hasEnchant(greenEnergyEnchant)) {
            uGreenEnergy(player);
        } else if(meta.hasEnchant(galaxyEnchant)) {
            uGalaxy(player);
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

    private final Particle.DustOptions green = new Particle.DustOptions(Color.fromRGB(80, 255, 80), 0.6f);
    private final Particle.DustOptions aqua = new Particle.DustOptions(Color.fromRGB(80, 255, 200), 0.6f);

    private final double[][] pentagramPoints = new double[5][2];
    {
        // Расчёт 5 точек пятиугольника
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(72 * i - 90);
            pentagramPoints[i][0] = Math.cos(angle);
            pentagramPoints[i][1] = Math.sin(angle);
        }
    }

    private void uGreenEnergy(Player player) {
        if (timer % 2 != 0) return;

        Location base = player.getLocation().add(0, 2.25, 0);
        World world = player.getWorld();

        double radius = 0.35 + 0.05 * Math.sin(timer / 10.0); // пульсация
        double rotation = timer / 40.0; // вращение

        for (int i = 0; i < 5; i++) {
            int j = (i + 2) % 5; // соединение через 2 точки — пентаграмма
            for (double t = 0; t <= 1.0; t += 0.25) {
                double x1 = pentagramPoints[i][0], z1 = pentagramPoints[i][1];
                double x2 = pentagramPoints[j][0], z2 = pentagramPoints[j][1];

                double x = (1 - t) * x1 + t * x2;
                double z = (1 - t) * z1 + t * z2;

                // Поворот
                double xRot = x * Math.cos(rotation) - z * Math.sin(rotation);
                double zRot = x * Math.sin(rotation) + z * Math.cos(rotation);

                Location loc = base.clone().add(xRot * radius, 0, zRot * radius);
                Particle.DustOptions dust = (i % 2 == 0) ? green : aqua;

                world.spawnParticle(Particle.DUST, loc, 0, 0, 0, 0, dust);
            }
        }

        // Время от времени вспышка энергии вверх
        if (timer % 6 == 0) {
            Location spark = base.clone().add(0, 0.4, 0);
            world.spawnParticle(Particle.DUST, spark, 0, 0, 0, 0, (random.nextBoolean() ? green : aqua));
        }
    }

    private final Particle.DustOptions nebula1 = new Particle.DustOptions(Color.fromRGB(90, 0, 120), 0.9f);
    private final Particle.DustOptions nebula2 = new Particle.DustOptions(Color.fromRGB(30, 0, 90), 0.6f);
    private final Particle.DustOptions core = new Particle.DustOptions(Color.fromRGB(0, 0, 0), 1.1f);

    private void uGalaxy(Player player) {
        Location base = player.getLocation().add(0, 2.4, 0);
        World world = player.getWorld();

        double outerRadius = 0.4 + 0.05 * Math.sin(timer / 10.0);
        double innerRadius = 0.2 + 0.02 * Math.cos(timer / 15.0);
        int outerPoints = 10;
        int innerPoints = 6;

        // Внешний слой — фиолетовая туманность
        for (int i = 0; i < outerPoints; i++) {
            double angle = 2 * Math.PI * i / outerPoints + timer / 30.0;
            double x = Math.cos(angle) * outerRadius * (0.8 + Math.random() * 0.2);
            double z = Math.sin(angle) * outerRadius * (0.8 + Math.random() * 0.2);
            double y = (Math.random() - 0.5) * 0.1;
            Location pos = base.clone().add(x, y, z);
            Particle.DustOptions dust = (i % 2 == 0) ? nebula1 : nebula2;
            world.spawnParticle(Particle.DUST, pos, 0, 0, 0, 0, dust);
        }

        // Внутренний слой — ядро/вихрь в другую сторону
        for (int i = 0; i < innerPoints; i++) {
            double angle = 2 * Math.PI * i / innerPoints - timer / 25.0;
            double x = Math.cos(angle) * innerRadius * (0.9 + Math.random() * 0.1);
            double z = Math.sin(angle) * innerRadius * (0.9 + Math.random() * 0.1);
            double y = (Math.random() - 0.5) * 0.07;
            Location pos = base.clone().add(x, y, z);
            world.spawnParticle(Particle.DUST, pos, 0, 0, 0, 0, core);
        }

        // Маленькое количество звёзд
        if (timer % 6 == 0) {
            int stars = 1 + random.nextInt(2); // 1–2 звезды
            for (int i = 0; i < stars; i++) {
                Location pos = base.clone().add(
                        (Math.random() - 0.5) * 0.5,
                        (Math.random() - 0.3) * 0.3,
                        (Math.random() - 0.5) * 0.5
                );
                world.spawnParticle(Particle.END_ROD, pos, 0, 0, 0.01, 0);
            }
        }

        // Портальный всплеск
        if (timer % 20 == 0) {
            world.spawnParticle(Particle.PORTAL, base.clone().add(0, 0.1, 0), 4, 0.1, 0.1, 0.1, 0.05);
        }
    }



}
