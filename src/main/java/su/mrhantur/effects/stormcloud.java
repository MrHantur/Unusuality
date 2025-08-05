package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.List;
import java.util.Random;

public class stormcloud implements UnusualEffect {
    private final Random random = new Random();
    private final Particle.DustOptions gray = new Particle.DustOptions(Color.fromRGB(90, 90, 90), 1.1f);

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        Location base = player.getLocation().add(0, 2.2, 0); // над головой

        // Меньше частиц — 7
        for (int i = 0; i < 12; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 1.4;
            double offsetY = random.nextDouble() * 0.4;
            double offsetZ = (random.nextDouble() - 0.5) * 1.4;

            Location particleLoc = base.clone().add(offsetX, offsetY, offsetZ);
            for (Player viewer : viewers) viewer.getWorld().spawnParticle(Particle.DUST, particleLoc, 0, 0, 0, 0, gray);
        }

        // Искры в облаке (редко)
        if (timer % 80 == 0) {
            Location center = base.clone().add(0, 0.2, 0);
            for (Player viewer : viewers) {
                viewer.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, center, 3, 0.3, 0.05, 0.3, 0.01);
                viewer.getWorld().playSound(center, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 0.3f, 1.6f);
            }
        }
    }
}
