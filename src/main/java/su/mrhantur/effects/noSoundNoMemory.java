package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;
import java.util.List;

public class noSoundNoMemory implements UnusualEffect {

    private final Color leafColor = Color.fromRGB(180, 20, 20);

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 2 != 0) return;

        Location base = player.getLocation();

        if (timer % 4 == 0) {
            double canopyHeight = 1.2 + Math.sin(timer / 20.0) * 0.05;
            double canopyRadius = 1.2;
            int points = 8;

            for (int i = 0; i < points; i++) {
                double angle = 2 * Math.PI * i / points + timer / 40.0;
                double r = canopyRadius + (Math.sin(angle * 5 + timer / 10.0) * 0.1);
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;
                double y = canopyHeight + (Math.random() * 0.15);

                Location pos = base.clone().add(x, y, z);
                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.TINTED_LEAVES, pos, 0, 0, 0, 0, 0, leafColor);
                }
            }
        }

        if (timer % 12 == 0) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * 1.0;
            double x = Math.cos(angle) * r;
            double z = Math.sin(angle) * r;
            double y = 1.0 + Math.random() * 0.5;

            Location pos = base.clone().add(x, y, z);
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.TINTED_LEAVES, pos, 0, 0, -0.02, 0, 0.02, leafColor);
            }
        }

        if (timer % 20 == 0) {
            for (int i = 0; i < 4; i++) {
                double angle = 2 * Math.PI * i / 4 + timer / 30.0;
                double r = 0.8;
                double x = Math.cos(angle) * r;
                double z = Math.sin(angle) * r;

                Location pos = base.clone().add(x, 0.05, z);
                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.RAID_OMEN, pos, 0, 0, 0, 0, 0);
                }
            }
        }
    }
}