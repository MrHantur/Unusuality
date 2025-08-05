package su.mrhantur.effects;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import su.mrhantur.UnusualEffect;

import java.util.List;

public class orbitingFire implements UnusualEffect {

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 4 != 0) return;

        Location base = player.getLocation().add(0, 2.1, 0);

        double radius = 0.3 + 0.05 * Math.sin(timer / 15.0); // "дыхание"
        double angle = timer / 10.0;
        double x = Math.cos(angle) * radius;
        double z = Math.sin(angle) * radius;
        double y = 0.05 * Math.sin(timer / 4.0); // вертикальное покачивание

        Location particleLoc = base.clone().add(x, y, z);
        for (Player viewer : viewers) viewer.spawnParticle(Particle.SOUL_FIRE_FLAME, particleLoc, 0, 0, 0, 0, 0);
    }
}
