package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import su.mrhantur.UnusualEffect;

import java.util.Random;

public class confetti implements UnusualEffect {
    private final Random random = new Random();

    @Override
    public void apply(Player player, int timer) {
        if (timer % 8 != 0) return;

        var origin = player.getLocation().add(0, 2.4, 0);
        int particles = 15 + random.nextInt(6);

        for (int i = 0; i < particles; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = 0.2 + random.nextDouble() * 0.1;
            Vector offset = new Vector(Math.cos(angle) * speed, 0.05 + random.nextDouble() * 0.15, Math.sin(angle) * speed);
            Color color = Color.fromRGB(100 + random.nextInt(156), 100 + random.nextInt(156), 100 + random.nextInt(156));

            player.getWorld().spawnParticle(
                    Particle.DUST, origin, 1,
                    offset.getX(), offset.getY(), offset.getZ(), 0,
                    new Particle.DustOptions(color, 1.0f)
            );
        }
    }
}
