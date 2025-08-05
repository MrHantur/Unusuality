package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.List;

public class flamingLantern implements UnusualEffect {
    private final Particle.DustOptions flameCore = new Particle.DustOptions(Color.fromRGB(255, 80, 20), 1.1f);

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 2 == 0) return;

        World world = player.getWorld();

        double offsetY = 2.4 + Math.sin(timer / 12.0) * 0.05;
        double offsetX = Math.cos(timer / 30.0) * 0.07;
        double offsetZ = Math.sin(timer / 40.0) * 0.07;
        Location center = player.getLocation().add(offsetX, offsetY, offsetZ);

        // Центральное пламя
        for (Player viewer : viewers) viewer.spawnParticle(Particle.DUST, center, 0, 0, 0, 0, flameCore);

        // Меньше огня вокруг
        for (int i = 0; i < 4; i++) {
            double angle = 2 * Math.PI * i / 4 + timer * 0.08;
            double radius = 0.22 + Math.sin(timer / 20.0) * 0.02;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.sin(angle * 3 + timer / 15.0) * 0.05;
            for (Player viewer : viewers) viewer.spawnParticle(Particle.FLAME, center.clone().add(x, y, z), 0, 0, 0.005, 0);
        }

        // Редкие вихри дыма
        if (timer % 5 == 0) {
            double angle = Math.random() * 2 * Math.PI;
            double x = Math.cos(angle) * 0.12;
            double z = Math.sin(angle) * 0.12;
            for (Player viewer : viewers) viewer.spawnParticle(Particle.SMOKE, center.clone().add(x, 0.05, z), 0, 0, 0.01, 0);
        }

        // Искра
        if (timer % 24 == 0) {
            for (Player viewer : viewers) viewer.spawnParticle(Particle.CRIT, center.clone(), 1, 0.1, 0.1, 0.1, 0.01);
        }
    }
}
