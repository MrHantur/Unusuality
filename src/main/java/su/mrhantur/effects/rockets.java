package su.mrhantur.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;
import java.util.List;

public class rockets implements UnusualEffect {

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 2 == 0) return;

        Location base = player.getLocation();

        // Запускаем 3 ракеты по кругу
        int rocketCount = 3;
        for (int r = 0; r < rocketCount; r++) {
            int rocketTimer = (timer + r * 20) % 100;
            if (rocketTimer > 35) continue;

            // 🔥 Прямолинейный подъём (без спирали)
            double angle = 2 * Math.PI * r / rocketCount;
            double startX = Math.cos(angle) * 0.4;
            double startZ = Math.sin(angle) * 0.4;

            double height = rocketTimer * 0.12;
            double x = startX;
            double z = startZ;
            double y = height - 0.1;

            Location rocketPos = base.clone().add(x, y, z);

            // --- Основная частица ракеты ---
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, rocketPos, 0, 0, 0.08, 0, 0.02);
            }

            // --- Акцентная частица другого типа для контраста ---
            if (timer % 3 == 0) {
                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.ELECTRIC_SPARK, rocketPos.clone().add(0, 0.05, 0), 0, 0, 0.05, 0, 0);
                }
            }

            // --- Шлейф дыма ---
            if (rocketTimer % 3 == 0 && rocketTimer < 30) {
                Location trailPos = rocketPos.clone().subtract(0, 0.15, 0);
                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.SMALL_FLAME, trailPos, 0, 0, 0.05, 0, 0.01);
                }
            }

            // --- Финал: облако искр + дым ---
            if (rocketTimer == 35) {
                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.ELECTRIC_SPARK, rocketPos, 4, 0.15, 0.15, 0.15, 0.02);
                    viewer.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, rocketPos, 2, 0.1, 0.1, 0.1, 0.01);
                }
            }
        }
    }
}