package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import su.mrhantur.UnusualEffect;

public class mountainHalo implements UnusualEffect {

    private final Particle.DustOptions glowDust = new Particle.DustOptions(Color.fromRGB(180, 200, 255), 1.2f);

    @Override
    public void apply(Player player, int timer) {
        if (timer % 2 != 0) return;

        Location base = player.getLocation().add(0, 2.5, 0);
        World world = base.getWorld();

        double radius = 0.55;
        int points = 12;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points + timer / 25.0;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = 0.05 * Math.sin(timer / 10.0 + i); // покачивание

            Location pos = base.clone().add(x, y, z);

            // Лёгкий сияющий след
            world.spawnParticle(Particle.DUST, pos, 0, 0, 0, 0, glowDust);

            // Иногда — магическая искра
            if (i % 6 == 0 && timer % 8 == 0) {
                world.spawnParticle(Particle.CRIT, pos, 0, 0, 0, 0, 0.0);
            }

            // Иногда снежинки
            if (i % 3 == 0 && timer % 4 == 0) {
                world.spawnParticle(Particle.INSTANT_EFFECT, pos, 0, 0, 0, 0, 0.0);
            }
        }

        if (timer % 10 == 0) {
            world.spawnParticle(Particle.WAX_ON, base.clone().add(0, 0.05, 0), 1, 0, 0, 0, 0.0);
        }
    }
}
