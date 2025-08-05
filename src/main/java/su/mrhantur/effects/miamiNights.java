package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.List;
import java.util.Random;

public class miamiNights implements UnusualEffect {
    private final Random random = new Random();
    private final Particle.DustOptions[] colors = {
            new Particle.DustOptions(Color.fromRGB(255,105,180), 0.8f),
            new Particle.DustOptions(Color.fromRGB(0,255,255),   0.8f),
            new Particle.DustOptions(Color.fromRGB(186,85,211),  0.8f),
            new Particle.DustOptions(Color.fromRGB(100,149,237), 0.8f)
    };

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        Location o = player.getLocation().add(0, 2.2, 0);
        World w = o.getWorld();

        int drops   = 4;      // всего 6 мини-капель
        double height = 1.0;  // высота потока 1 блок
        double radius = 0.4;  // узкий радиус
        double speed  = 0.1;  // быстрая цикличность

        // Мини-дождь
        for (int i = 0; i < drops; i++) {
            double cycle = ((timer * speed) + i * 0.5) % height;
            double yOff  = height - cycle;
            double angle = i * (2 * Math.PI / drops) + timer * 0.04;
            double spread = 0.7 + 0.3 * random.nextDouble();
            double xOff   = Math.cos(angle) * radius * spread;
            double zOff   = Math.sin(angle) * radius * spread;
            Particle.DustOptions color = colors[i % colors.length];

            Location dropPos = o.clone().add(xOff, yOff, zOff);
            for (Player viewer : viewers) viewer.spawnParticle(Particle.DUST, dropPos, 0, 0, -0.02, 0, color);

            // лёгкий белый отблеск у поверхности
            if (yOff < 0.15) {
                for (Player viewer : viewers) viewer.spawnParticle(
                        Particle.DUST,
                        new Location(o.getWorld(), dropPos.getX(), o.getY(), dropPos.getZ()),
                        2, 0.05, 0, 0.05,
                        new Particle.DustOptions(Color.WHITE, 1.0f)
                );
            }
        }

        // почти без искр — только тонкий разряд раз в секунду
        if (timer % 20 == 0 && random.nextDouble() < 0.1) {
            int i = random.nextInt(drops);
            double ang = i * (2 * Math.PI / drops) + timer * 0.04;
            Location p = o.clone().add(Math.cos(ang) * radius, 0.6, Math.sin(ang) * radius);
            for (Player viewer : viewers) viewer.spawnParticle(Particle.ELECTRIC_SPARK, p, 0, 0, 0, 0);
        }
    }
}