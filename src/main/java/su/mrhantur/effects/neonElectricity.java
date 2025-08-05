package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.List;
import java.util.Random;

public class neonElectricity implements UnusualEffect {
    private final Random random = new Random();

    private final Particle.DustOptions[] colors = {
            new Particle.DustOptions(Color.fromRGB(255,105,180), 0.9f), // Hot Pink
            new Particle.DustOptions(Color.fromRGB(0,255,255),   0.9f), // Cyan
            new Particle.DustOptions(Color.fromRGB(186,85,211),  0.9f), // Medium Orchid
            new Particle.DustOptions(Color.fromRGB(100,149,237), 0.9f)  // Cornflower Blue
    };

    @Override
    public void apply(Entity entity, int timer, List<Player> viewers) {
        if (viewers.isEmpty()) return;

        Location origin = entity.getLocation().add(0, 2.3, 0);
        World world = origin.getWorld();

        int pillars = 4;
        int levels = 5; // Уменьшено с 8 до 5
        double radius = 0.7;

        // 1) Неоновые столбы, пульсирующие вверх-вниз
        for (int i = 0; i < pillars; i++) {
            double angle = timer * 0.03 + i * (Math.PI * 2 / pillars);
            double xOff = Math.cos(angle) * radius;
            double zOff = Math.sin(angle) * radius;
            Particle.DustOptions color = colors[i];

            for (int y = 0; y < levels; y++) {
                // вертикальная позиция + волна
                double yOff = y * 0.2 + Math.sin(timer * 0.15 + y * 0.4 + i) * 0.1;
                Location point = origin.clone().add(xOff, yOff, zOff);

                for (Player viewer : viewers) {
                    if (viewer.getWorld().equals(world)) {
                        viewer.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, color);
                    }
                }
            }
        }

        // 2) Искры между соседними столбами (меньший угол)
        if (random.nextDouble() < 0.25) {
            int a = random.nextInt(pillars);
            // Берем строго соседний столб (угол 90°)
            int b = (a + 1) % pillars;

            double angA = timer * 0.03 + a * (Math.PI * 2 / pillars);
            double angB = timer * 0.03 + b * (Math.PI * 2 / pillars);

            // Более близкие точки на столбах
            double heightBase = 0.5 + random.nextDouble() * 0.4; // Меньший разброс по высоте

            Location pA = origin.clone().add(
                    Math.cos(angA) * radius,
                    heightBase,
                    Math.sin(angA) * radius
            );

            Location pB = origin.clone().add(
                    Math.cos(angB) * radius,
                    heightBase + (random.nextDouble() - 0.5) * 0.2, // Небольшая разница высот
                    Math.sin(angB) * radius
            );

            // линия искр
            for (int i = 0; i < 5; i++) {
                double t = i / 4.0;
                Location mid = pA.clone().add(
                        (pB.getX()-pA.getX()) * t,
                        (pB.getY()-pA.getY()) * t,
                        (pB.getZ()-pA.getZ()) * t
                );

                for (Player viewer : viewers) {
                    if (viewer.getWorld().equals(world)) {
                        viewer.spawnParticle(Particle.ELECTRIC_SPARK, mid, 0,
                                (random.nextDouble()-0.5)*0.02,
                                (random.nextDouble()-0.5)*0.02,
                                (random.nextDouble()-0.5)*0.02,
                                0.05);
                    }
                }
            }
        }
    }
}