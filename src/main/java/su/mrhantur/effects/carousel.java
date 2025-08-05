package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.List;

public class carousel implements UnusualEffect {
    private final Color[] rainbowColors = {
            Color.fromRGB(255, 0, 0),    // Red
            Color.fromRGB(255, 165, 0),  // Orange
            Color.fromRGB(255, 255, 0),  // Yellow
            Color.fromRGB(0, 255, 0),    // Green
            Color.fromRGB(0, 0, 255),    // Blue
            Color.fromRGB(75, 0, 130),   // Indigo
            Color.fromRGB(238, 130, 238) // Violet
    };

    private final Particle.DustOptions crystal = new Particle.DustOptions(Color.fromRGB(220, 220, 255), 1.0f);

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 3 != 0) return;

        Location center = player.getLocation().add(0, 2.2, 0);

        // Crystal core
        for (Player viewer : viewers)
            viewer.spawnParticle(Particle.DUST, center, 0, 0, 0, 0, crystal);

        // Rainbow rays
        int rays = 7;
        for (int i = 0; i < rays; i++) {
            double angle = 2 * Math.PI * i / rays + timer * 0.08;
            Color rayColor = rainbowColors[i];
            Particle.DustOptions rayDust = new Particle.DustOptions(rayColor, 0.6f);

            for (double dist = 0.2; dist <= 0.8; dist += 0.15) {
                double x = Math.cos(angle) * dist;
                double z = Math.sin(angle) * dist;
                double y = Math.sin(timer * 0.1 + i) * 0.1;

                for (Player viewer : viewers)
                    viewer.spawnParticle(Particle.DUST, center.clone().add(x, y, z), 0, 0, 0, 0, rayDust);
            }
        }

        // Prismatic sparkles
        if (timer % 8 == 0) {
            for (Player viewer : viewers)
                viewer.spawnParticle(Particle.ENCHANT, center, 5, 0.4, 0.4, 0.4, 0.1);
        }
    }
}