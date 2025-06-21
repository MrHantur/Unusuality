package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import su.mrhantur.UnusualEffect;

import java.util.Random;

public class greenEnergy implements UnusualEffect {
    private final Particle.DustOptions green = new Particle.DustOptions(Color.fromRGB(80, 255, 80), 0.6f);
    private final Particle.DustOptions aqua = new Particle.DustOptions(Color.fromRGB(80, 255, 200), 0.6f);
    private final double[][] pentagramPoints = new double[5][2];
    private final Random random = new Random();

    public greenEnergy() {
        for (int i = 0; i < 5; i++) {
            double angle = Math.toRadians(72 * i - 90);
            pentagramPoints[i][0] = Math.cos(angle);
            pentagramPoints[i][1] = Math.sin(angle);
        }
    }

    @Override
    public void apply(Player player, int timer) {
        if (timer % 2 != 0) return;

        Location base = player.getLocation().add(0, 2.25, 0);
        World world = base.getWorld();
        double radius = 0.35 + 0.05 * Math.sin(timer / 10.0);
        double rotation = timer / 40.0;

        for (int i = 0; i < 5; i++) {
            int j = (i + 2) % 5;
            for (double t = 0; t <= 1.0; t += 0.25) {
                double x = (1 - t) * pentagramPoints[i][0] + t * pentagramPoints[j][0];
                double z = (1 - t) * pentagramPoints[i][1] + t * pentagramPoints[j][1];

                double xRot = x * Math.cos(rotation) - z * Math.sin(rotation);
                double zRot = x * Math.sin(rotation) + z * Math.cos(rotation);

                Location loc = base.clone().add(xRot * radius, 0, zRot * radius);
                Particle.DustOptions dust = (i % 2 == 0) ? green : aqua;

                world.spawnParticle(Particle.DUST, loc, 0, 0, 0, 0, dust);
            }
        }

        if (timer % 6 == 0) {
            world.spawnParticle(Particle.DUST, base.clone().add(0, 0.4, 0), 0, 0, 0, 0, (random.nextBoolean() ? green : aqua));
        }
    }
}
