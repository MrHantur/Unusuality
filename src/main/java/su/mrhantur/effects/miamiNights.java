package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.Random;

public class miamiNights implements UnusualEffect {
    private final Random random = new Random();

    private final Particle.DustOptions pink = new Particle.DustOptions(Color.fromRGB(255, 105, 180), 0.8f);
    private final Particle.DustOptions cyan = new Particle.DustOptions(Color.fromRGB(0, 255, 255), 0.8f);
    private final Particle.DustOptions purple = new Particle.DustOptions(Color.fromRGB(186, 85, 211), 0.8f);
    private final Particle.DustOptions blue = new Particle.DustOptions(Color.fromRGB(100, 149, 237), 0.8f);
    private final Particle.DustOptions white = new Particle.DustOptions(Color.fromRGB(255, 255, 255), 1.0f);
    private final Particle.DustOptions[] colors = new Particle.DustOptions[]{pink, cyan, purple, blue};

    @Override
    public void apply(Player player, int timer) {
        Location origin = player.getLocation().add(0, 1.9, 0);
        World world = origin.getWorld();

        for (int i = 0; i < 4; i++) {
            Particle.DustOptions color = colors[i % colors.length];
            double seed = timer * 0.04 + i * 15;

            double baseYaw = Math.toRadians(90 * i);
            double yawOffset = Math.sin(seed * 0.3 + i) * Math.toRadians(20);
            double yaw = baseYaw + yawOffset;

            // 💡 ОСТРЫЙ угол к горизонту — 45–60°
            double basePitchDeg = 45 + Math.sin(seed * 0.3 + i) * 7.5; // 37.5° – 52.5°
            double pitch = Math.toRadians(basePitchDeg);

            double dx = Math.cos(pitch) * Math.cos(yaw);
            double dy = Math.sin(pitch);
            double dz = Math.cos(pitch) * Math.sin(yaw);

            Location current = origin.clone();

            for (int j = 0; j < 5; j++) {
                double wobbleX = Math.sin(seed + j * 0.4) * 0.02;
                double wobbleZ = Math.cos(seed + j * 0.3) * 0.02;
                double wobbleY = Math.sin(seed + j * 0.2) * 0.01;

                current = current.add(dx * 0.12 + wobbleX, dy * 0.12 + wobbleY, dz * 0.12 + wobbleZ);
                world.spawnParticle(Particle.DUST, current, 0, 0, 0, 0, color);

                if (j == 4 && random.nextDouble() < 0.8) {
                    world.spawnParticle(Particle.DUST, current, 0, 0, 0.01, 0, white);
                }
            }
        }

        if (random.nextDouble() < 0.3) {
            world.spawnParticle(Particle.ELECTRIC_SPARK,
                    origin.clone().add((random.nextDouble() - 0.5) * 0.5, 0.2, (random.nextDouble() - 0.5) * 0.5),
                    0, 0, 0.01, 0);
        }
    }

}
