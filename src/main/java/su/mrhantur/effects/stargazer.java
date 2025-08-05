package su.mrhantur.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.List;

public class stargazer implements UnusualEffect {

    // Конфигурация эффекта
    private static final int STAR_COUNT = 2;
    private static final double ORBIT_RADIUS = 0.2;
    private static final double ORBIT_HEIGHT = 2.2;
    private static final double ROTATION_SPEED = 0.02;
    private static final double PULSE_SPEED = 0.05;
    private static final double PULSE_RANGE = 0.1;

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 60 != 0) return;

        Location center = player.getLocation().add(0, ORBIT_HEIGHT, 0);

        // Обновление и отрисовка вращающихся звёзд
        renderOrbitingStars(center, timer, viewers);
    }

    private void renderOrbitingStars(Location center, int timer, List<Player> viewers) {
        double rotation = timer * ROTATION_SPEED;

        for (int i = 0; i < STAR_COUNT; i++) {
            double theta = 2 * Math.PI * i / STAR_COUNT + rotation;
            double phi = Math.PI / 3 + Math.sin((timer + i * 7) * PULSE_SPEED) * PULSE_RANGE;

            double x = Math.sin(phi) * Math.cos(theta) * ORBIT_RADIUS;
            double y = Math.cos(phi) * ORBIT_RADIUS;
            double z = Math.sin(phi) * Math.sin(theta) * ORBIT_RADIUS;

            Location starPos = center.clone().add(x, y, z);

            for (Player viewer : viewers) {
                if (!viewer.getWorld().equals(center.getWorld())) continue;
                viewer.spawnParticle(Particle.FIREFLY, starPos, 1, 0, 0, 0, 0);
            }
        }
    }
}