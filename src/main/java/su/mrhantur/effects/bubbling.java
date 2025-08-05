package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import su.mrhantur.UnusualEffect;

import java.util.List;
import java.util.Random;

public class bubbling implements UnusualEffect {
    private final Particle.DustOptions baseAura = new Particle.DustOptions(Color.fromRGB(100, 180, 255), 0.6f);
    private final Random random = new Random();

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        Location loc = player.getLocation().add(0, 0.05, 0);
        int bubbleCount = 6;
        double radius = 0.3;

        // Кольцо пузырьков
        for (int i = 0; i < bubbleCount; i++) {
            double angle = 2 * Math.PI * i / bubbleCount + (timer % 20) / 20.0 * Math.PI;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            Location particleLoc = loc.clone().add(x, 0, z);
            for (Player viewer : viewers) viewer.getWorld().spawnParticle(Particle.BUBBLE, particleLoc, 0, 0, 0.05, 0, 0.01);
        }

        // Центральное сияние
        for (Player viewer : viewers) viewer.getWorld().spawnParticle(Particle.DUST, loc, 0, 0, 0, 0, baseAura);

        // Всплывающие пузырьки вверх (редко)
        if (timer % 3 == 0) {
            Location upward = loc.clone().add(random.nextDouble() * 0.4 - 0.2, 0.1, random.nextDouble() * 0.4 - 0.2);
            for (Player viewer : viewers) viewer.getWorld().spawnParticle(Particle.BUBBLE_POP, upward, 0, 0, 0.04 + random.nextDouble() * 0.02, 0, 0.02);
        }
    }
}
