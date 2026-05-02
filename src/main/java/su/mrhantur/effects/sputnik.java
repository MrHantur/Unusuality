package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;
import java.util.List;

public class sputnik implements UnusualEffect {

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 2 == 1) return;

        // --- Цвет на основе никнейма ---
        String name = player.getName();
        int hash = name.hashCode();

        // Извлекаем приятные, насыщенные цвета из хеша
        int r = (Math.abs((hash >> 16) & 0xFF) % 156) + 100; // 100-255
        int g = (Math.abs((hash >> 8) & 0xFF) % 156) + 100;
        int b = (Math.abs(hash & 0xFF) % 156) + 100;
        Color planetColor = Color.fromRGB(r, g, b);

        // Ядро и свечение с разными размерами
        Particle.DustOptions planetCore = new Particle.DustOptions(planetColor, 0.9f);
        Particle.DustOptions planetGlow = new Particle.DustOptions(planetColor, 0.4f);

        // --- Орбита ---
        double angle = 2 * Math.PI * (timer % 6000) / 6000.0;
        double orbitRadius = 1.1;
        double baseY = 2.2;

        double x = Math.cos(angle) * orbitRadius;
        double z = Math.sin(angle) * orbitRadius;

        Location planetPos = player.getLocation().add(x, baseY, z);

        // --- Ядро "планеты" ---
        for (Player viewer : viewers) {
            viewer.spawnParticle(Particle.DUST, planetPos, 0, 0, 0, 0, planetCore);
        }

        // --- Лёгкое свечение вокруг ---
        if (timer % 6 == 0) {
            for (int i = 0; i < 2; i++) {
                double spreadAngle = angle + Math.PI * i;
                double glowR = 0.25;
                double gx = Math.cos(spreadAngle) * glowR;
                double gz = Math.sin(spreadAngle) * glowR;
                double gy = Math.sin(timer / 20.0 + i) * 0.05;

                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.DUST, planetPos.clone().add(gx, gy, gz), 0, 0, 0, 0, planetGlow);
                }
            }
        }

        if (timer % 6 == 0) {
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.END_ROD, planetPos.clone(), 0, 0.02, 0.02, 0.02, 0);
            }
        }
    }
}