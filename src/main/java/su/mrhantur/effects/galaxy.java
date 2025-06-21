package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import su.mrhantur.UnusualEffect;

import java.util.Random;

public class galaxy implements UnusualEffect {
    private final Particle.DustOptions nebula1 = new Particle.DustOptions(Color.fromRGB(90, 0, 120), 0.9f);
    private final Particle.DustOptions nebula2 = new Particle.DustOptions(Color.fromRGB(30, 0, 90), 0.6f);
    private final Particle.DustOptions core = new Particle.DustOptions(Color.fromRGB(0, 0, 0), 1.1f);
    private final Random random = new Random();

    @Override
    public void apply(Player player, int timer) {
        Location base = player.getLocation().add(0, 2.4, 0);
        World world = base.getWorld();

        double outerRadius = 0.4 + 0.05 * Math.sin(timer / 10.0);
        double innerRadius = 0.2 + 0.02 * Math.cos(timer / 15.0);

        for (int i = 0; i < 10; i++) {
            double angle = 2 * Math.PI * i / 10 + timer / 30.0;
            double x = Math.cos(angle) * outerRadius * (0.8 + Math.random() * 0.2);
            double z = Math.sin(angle) * outerRadius * (0.8 + Math.random() * 0.2);
            double y = (Math.random() - 0.5) * 0.1;
            Particle.DustOptions dust = (i % 2 == 0) ? nebula1 : nebula2;

            world.spawnParticle(Particle.DUST, base.clone().add(x, y, z), 0, 0, 0, 0, dust);
        }

        for (int i = 0; i < 6; i++) {
            double angle = 2 * Math.PI * i / 6 - timer / 25.0;
            double x = Math.cos(angle) * innerRadius * (0.9 + Math.random() * 0.1);
            double z = Math.sin(angle) * innerRadius * (0.9 + Math.random() * 0.1);
            double y = (Math.random() - 0.5) * 0.07;

            world.spawnParticle(Particle.DUST, base.clone().add(x, y, z), 0, 0, 0, 0, core);
        }

        if (timer % 6 == 0) {
            for (int i = 0; i < 1 + random.nextInt(2); i++) {
                world.spawnParticle(
                        Particle.END_ROD,
                        base.clone().add((Math.random() - 0.5) * 0.5, (Math.random() - 0.3) * 0.3, (Math.random() - 0.5) * 0.5),
                        0, 0, 0.01, 0
                );
            }
        }

        if (timer % 20 == 0) {
            world.spawnParticle(Particle.PORTAL, base.clone().add(0, 0.1, 0), 4, 0.1, 0.1, 0.1, 0.05);
        }
    }
}
