package su.mrhantur.effects;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import su.mrhantur.UnusualEffect;

public class fireflies implements UnusualEffect {

    private final Particle.DustOptions fireDust = new Particle.DustOptions(Color.fromRGB(255, 255, 0), 0.5f);

    @Override
    public void apply(Player player, int timer) {
        if (timer % 2 != 0) return;

        double R = 0.5;
        double r = 0.2;
        double k = 6;
        int segments = 2;

        Location base = player.getLocation().add(0, 2.3, 0);

        for (int i = 0; i < segments; i++) {
            double t = ((timer % 200) + i) / 40.0 * Math.PI;

            double x1 = (R + r * Math.cos(k * t)) * Math.cos(t);
            double z1 = (R + r * Math.cos(k * t)) * Math.sin(t);
            double y1 = r * Math.sin(k * t);

            double x2 = (R + r * Math.cos(k * t + Math.PI)) * Math.cos(t);
            double z2 = (R + r * Math.cos(k * t + Math.PI)) * Math.sin(t);
            double y2 = r * Math.sin(k * t + Math.PI);

            Location p1 = base.clone().add(x1, y1, z1);
            Location p2 = base.clone().add(x2, y2, z2);

            World world = player.getWorld();
            world.spawnParticle(Particle.DUST, p1, 0, 0, 0, 0, fireDust);
            world.spawnParticle(Particle.DUST, p2, 0, 0, 0, 0, fireDust);
        }
    }
}
