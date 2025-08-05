package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import su.mrhantur.UnusualEffect;

import java.util.List;

public class devilHorns implements UnusualEffect {

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        // Базовое положение (лоб игрока)
        Location base = player.getLocation().add(0, 1.9, 0);
        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Vector right = new Vector(-direction.getZ(), 0, direction.getX()).normalize();
        Vector forward = direction.clone();

        int segments = 4; // Количество сегментов на рог
        double radius = 0.3 + 0.02 * Math.sin(timer / 10.0); // Пульсирующий радиус

        // Левый рог
        renderHorn(viewers, base, right, forward, segments, radius, true);
        // Правый рог
        renderHorn(viewers, base, right, forward, segments, radius, false);
    }

    private void renderHorn(List<Player> viewers, Location base, Vector right, Vector forward,
                            int segments, double radius, boolean isLeft) {

        double maxDivergence = 0.2; // максимальное смещение на кончике

        for (int i = 0; i <= segments; i++) {
            // спавним частицы только на чётных сегментах
            if (i % 2 != 0) continue;

            double t = (double) i / segments;
            double angle = t * Math.PI / 2; // 90° дуга

            // Форма рога (кривая Безье)
            double x = radius * Math.cos(angle);
            double y = radius * Math.sin(angle);
            double z = -radius * (1 - Math.cos(angle)) * 0.5;

            // Дополнительное расхождение
            double divergenceOffset = maxDivergence * t;

            Vector lateral = right.clone().multiply(isLeft ? -1 : 1);
            Vector pointOffset = lateral.clone().multiply(x + divergenceOffset)
                    .add(new Vector(0, y, 0))
                    .add(forward.clone().multiply(z));

            Location point = base.clone().add(pointOffset);

            // Градиент от темно-серого к красному
            int red   = 30 + (int)(t * 170);
            int green = (int)(30 * (1 - t));
            int blue  = (int)(30 * (1 - t));
            Color color = Color.fromRGB(red, green, blue);
            Particle.DustOptions dust = new Particle.DustOptions(color, 1.0f);

            // Отображаем одну частицу пыли на зрителя
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, dust);
            }

            // Эффекты пламени на кончиках рогов с меньшей вероятностью
            if (i == segments && Math.random() < 0.05) {
                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0.01);
                    viewer.spawnParticle(Particle.SMOKE, point, 1, 0.05, 0.05, 0.05, 0.01);
                }
            }
        }
    }
}