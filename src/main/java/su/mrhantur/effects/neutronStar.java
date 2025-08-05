package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import su.mrhantur.UnusualEffect;

import java.util.List;
import java.util.Random;

public class neutronStar implements UnusualEffect {
    private final Particle.DustOptions core = new Particle.DustOptions(Color.fromRGB(30, 30, 60), 1.0f);
    private final Particle.DustOptions beam = new Particle.DustOptions(Color.fromRGB(150, 150, 255), 0.5f);
    private final Random random = new Random();

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 2 == 0) return;

        Location center = player.getLocation().add(0, 2.4, 0);
        World world = center.getWorld();

        for (int i = 0; i < 8; i++) {
            double angle = 2 * Math.PI * i / 8 + timer / 10.0;
            double radius = 0.15 + 0.02 * Math.sin(timer / 8.0);
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = (random.nextDouble() - 0.5) * 0.05;

            for (Player viewer : viewers) viewer.spawnParticle(Particle.DUST, center.clone().add(x, y, z), 0, 0, 0, 0, core);
        }

        double jetAngle = timer / 5.0;
        double dx = Math.cos(jetAngle) * 0.02;
        double dz = Math.sin(jetAngle) * 0.02;

        for (int i = -3; i <= 3; i++) {
            Location jetUp = center.clone().add(dx * i, i * 0.1, dz * i);
            Location jetDown = center.clone().add(-dx * i, -i * 0.1, -dz * i);

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, jetUp, 0, 0, 0, 0, beam);
                viewer.spawnParticle(Particle.DUST, jetDown, 0, 0, 0, 0, beam);
            }
        }

        if (random.nextDouble() < 0.4) {
            for (Player viewer : viewers) viewer.spawnParticle(Particle.ELECTRIC_SPARK, center, 1, 0.1, 0.1, 0.1, 0.01);
        }

        if (timer % 40 == 1) {
            for (Player viewer : viewers) viewer.spawnParticle(Particle.PORTAL, center, 6, 0.3, 0.2, 0.3, 0.05);
        }

        if (timer % 5 == 0) {
            for (Player viewer : viewers) viewer.spawnParticle(Particle.END_ROD, center.clone().add(0, 0.05, 0), 0, 0, 0.01, 0);
        }
    }
}
