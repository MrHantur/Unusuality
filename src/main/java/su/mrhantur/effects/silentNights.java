package su.mrhantur.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import su.mrhantur.UnusualEffect;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class silentNights implements UnusualEffect {

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        long time = world.getTime();
        boolean isNight = time >= 13000 && time <= 23000;

        if (isNight) {
            spawnNightFireflies(loc, timer, viewers);
        } else {
            spawnDayPollen(loc, timer, viewers);
        }
    }

    private void spawnNightFireflies(Location loc, int timer, List<Player> viewers) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // СВЕТЛЯЧКИ (Fireflies)
        if (timer % 12 == 0) {
            // Генерируем позицию в кольце: от 1.5 до 3.5 метров от игрока
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = 1.5 + random.nextDouble() * 2.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 0.5 + random.nextDouble() * 2.5; // Разброс по высоте

            Location particleLoc = loc.clone().add(x, y, z);

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.FIREFLY, particleLoc, 1, 0, 0, 0, 0.02);

                if (random.nextDouble() > 0.8) {
                    viewer.spawnParticle(Particle.WAX_OFF, particleLoc, 1, 0.1, 0.1, 0.1, 0);
                }
            }
        }

        // Дымка у ног
        if (timer % 15 == 0) {
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.WHITE_ASH, loc.clone().add(0, 0.1, 0), 3, 0.6, 0.1, 0.6, 0.01);
            }
        }
    }

    private void spawnDayPollen(Location loc, int timer, List<Player> viewers) {
        if (timer % 30 != 0) return;

        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Это гарантирует, что частица никогда не появится прямо перед лицом
        double angle = random.nextDouble() * 2 * Math.PI;
        double radius = 1.5 + random.nextDouble() * 1.5;
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        double y = 0.5 + random.nextDouble() * 3.0; // Высота от колен до высоко над головой

        Location spawnLoc = loc.clone().add(x, y, z);

        for (Player viewer : viewers) {
            // SPORE_BLOSSOM_AIR — самая спокойная частица.
            // Она крошечная, медленно парит и не имеет ярких вспышек.
            viewer.spawnParticle(Particle.SPORE_BLOSSOM_AIR, spawnLoc, 1, 0, 0, 0, 0.01);

            // Добавляем редчайшую серую пылинку для объема (раз в 4 секунды)
            if (timer % 60 == 0) {
                viewer.spawnParticle(Particle.WHITE_ASH, spawnLoc.add(0.2, -0.1, 0.1), 1, 0, 0, 0, 0.005);
            }
        }
    }
}