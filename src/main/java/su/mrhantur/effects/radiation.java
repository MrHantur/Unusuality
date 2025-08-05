package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.List;
import java.util.Random;

public class radiation implements UnusualEffect {
    private final Random random = new Random();

    // Цвета для радиации - желто-зеленые оттенки
    private final Particle.DustOptions[] radiationColors = {
            new Particle.DustOptions(Color.fromRGB(255, 255, 0), 0.6f),   // Ярко-желтый
            new Particle.DustOptions(Color.fromRGB(200, 255, 0), 0.6f),   // Желто-зеленый
            new Particle.DustOptions(Color.fromRGB(150, 255, 50), 0.6f),  // Зеленовато-желтый
            new Particle.DustOptions(Color.fromRGB(100, 200, 0), 0.6f)    // Зеленый
    };

    private final Particle.DustOptions coreColor = new Particle.DustOptions(Color.fromRGB(255, 255, 100), 1.0f);

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 2 != 0) return;

        Location center = player.getLocation().add(0, 2.3, 0);

        // Создаем символ радиации из трех секторов
        drawRadiationSymbol(center, timer, viewers);

        // Случайные искры опасности
        if (random.nextDouble() < 0.07) {
            drawHazardSparks(center, viewers);
        }
    }

    private void drawRadiationSymbol(Location center, int timer, List<Player> viewers) {
        double rotationSpeed = 0.05;
        double baseRotation = timer * rotationSpeed;

        // Рисуем три сектора знака радиации
        for (int sector = 0; sector < 3; sector++) {
            double sectorAngle = sector * (2 * Math.PI / 3) + baseRotation;

            // Внутренний радиус (центральная окружность)
            double innerRadius = 0.2;
            // Внешний радиус
            double outerRadius = 0.7;

            // Рисуем сектор (веер)
            for (double angle = -Math.PI/6; angle <= Math.PI/6; angle += Math.PI/18) {
                double currentAngle = sectorAngle + angle;

                // Рисуем линии от центра к краю
                for (double radius = innerRadius; radius <= outerRadius; radius += 0.08) {
                    double x = Math.cos(currentAngle) * radius;
                    double z = Math.sin(currentAngle) * radius;
                    double y = Math.sin(timer * 0.1) * 0.05; // Легкое покачивание

                    Location point = center.clone().add(x, y, z);

                    // Выбираем цвет в зависимости от расстояния от центра
                    Particle.DustOptions color = radiationColors[(int)(radius * 4) % radiationColors.length];

                    for (Player viewer : viewers) {
                        viewer.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, color);
                    }
                }
            }
        }

        // Рисуем центральную окружность
        int circlePoints = 7;
        for (int i = 0; i < circlePoints; i++) {
            double angle = i * 2 * Math.PI / circlePoints;
            double x = Math.cos(angle) * 0.15;
            double z = Math.sin(angle) * 0.15;

            Location point = center.clone().add(x, 0, z);

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, coreColor);
            }
        }
    }

    private void drawHazardSparks(Location center, List<Player> viewers) {
        // Создаем случайные искры вокруг символа
        for (int i = 0; i < 2; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.4 + random.nextDouble() * 0.4;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = (random.nextDouble() - 0.5) * 0.2;

            Location sparkPoint = center.clone().add(x, y, z);

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.WAX_ON, sparkPoint, 1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }
}