package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import su.mrhantur.UnusualEffect;

public class astralStep implements UnusualEffect {
    private final Particle.DustOptions echoColor = new Particle.DustOptions(Color.fromRGB(160, 100, 255), 0.8f);

    @Override
    public void apply(Player player, int timer) {
        if (timer % 2 != 0) return;

        Location loc = player.getLocation().add(0, 0.1, 0);
        int points = 10;
        double radius = 0.35;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(x, 0, z), 0, 0, 0, 0, echoColor);
        }

        loc.getWorld().spawnParticle(Particle.END_ROD, loc, 0, 0, 0.01, 0);

        if (timer % 6 == 0) {
            loc.getWorld().spawnParticle(Particle.WITCH, loc.clone().add(0, -0.5, 0), 2, 0.02, 0.01, 0.02, 0.01);
        }
    }
}
