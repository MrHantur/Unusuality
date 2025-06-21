package su.mrhantur.effects;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import su.mrhantur.UnusualEffect;

import java.util.Random;

public class restlessSouls implements UnusualEffect {
    private final Random random = new Random();

    @Override
    public void apply(Player player, int timer) {
        Location feet = player.getLocation().add(0, 0.1, 0);
        if (timer % 8 == 0) {
            for (int i = 0; i < 3; i++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                double radius = 0.3 + random.nextDouble() * 0.3;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;

                Location pos = feet.clone().add(x, 0, z);
                feet.getWorld().spawnParticle(Particle.SOUL, pos, 0, 0, 0.1, 0, 0.01);
                feet.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, pos, 0, 0, 0.1, 0, 0.01);
            }
        }

        if (timer % 3 == 0) {
            for (int i = 0; i < 2; i++) {
                double x = (random.nextDouble() - 0.5) * 0.5;
                double z = (random.nextDouble() - 0.5) * 0.5;
                feet.getWorld().spawnParticle(Particle.ASH, feet.clone().add(x, 0.05, z), 0, 0, 0.01, 0, 0.001);
            }
        }

        if (timer % 40 == 0) {
            feet.getWorld().playSound(feet, Sound.ENTITY_PHANTOM_FLAP, 0.3f, 0.5f + random.nextFloat() * 0.2f);
            feet.getWorld().spawnParticle(Particle.SMOKE, feet, 2, 0.2, 0, 0.2, 0.01);
        }
    }
}
