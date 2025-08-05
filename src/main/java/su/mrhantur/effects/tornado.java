package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.List;
import java.util.Random;

public class tornado implements UnusualEffect {

    private final Random random = new Random();

    // Типы частиц для разнообразия
    private final Particle[] particleTypes = {
            Particle.ASH,
            Particle.WHITE_ASH,
            Particle.MYCELIUM
    };

    @Override
    public void apply(Entity entity, int timer, List<Player> viewers) {
        if (timer % 2 == 0) return;

        Location base = entity.getLocation().add(0, 0.1, 0); // Основание торнадо
        double height = 0.8;    // Высота торнадо
        double baseRadius = 0.15; // Радиус в основании
        double topRadius = 0.6;  // Радиус на вершине

        // Параметры анимации
        double rotation = timer * 0.2;
        double verticalSpeed = 0.05;
        double pulse = 0.05 * Math.sin(timer * 0.15);

        // Основное тело торнадо (уменьшенное количество частиц)
        for (double y = 0; y < height; y += 0.25) {
            // Плавное расширение кверху
            double radius = baseRadius + (topRadius - baseRadius) * (y / height);
            radius += pulse; // Добавляем пульсацию

            // Спиральное движение
            double angle = rotation + y * verticalSpeed;

            // Генерация 4 частиц на уровень вместо 6
            for (int i = 0; i < 4; i++) {
                double segmentAngle = angle + i * (Math.PI / 2);
                double x = Math.cos(segmentAngle) * radius;
                double z = Math.sin(segmentAngle) * radius;

                Location loc = base.clone().add(x, y, z);

                // Случайный выбор типа частицы
                Particle particleType = particleTypes[random.nextInt(particleTypes.length)];

                for (Player viewer : viewers) {
                    // Для частиц пыли используем цветные варианты
                    if (particleType == Particle.ASH || particleType == Particle.WHITE_ASH) {
                        viewer.spawnParticle(particleType, loc, 1, 0, 0, 0, 0.02);
                    }
                    // Для обычных частиц используем стандартный вызов
                    else {
                        viewer.spawnParticle(particleType, loc, 1, 0, 0, 0, 0.02);
                    }
                }
            }
        }

        // Вращающиеся "языки" у основания (реже и меньше частиц)
        if (timer % 5 == 0) {
            double groundAngle = rotation * 1.5;
            for (int i = 0; i < 2; i++) { // Только 2 частицы
                double angle = groundAngle + i * Math.PI;
                double x = Math.cos(angle) * baseRadius * 1.8;
                double z = Math.sin(angle) * baseRadius * 1.8;

                Location loc = base.clone().add(x, 0.05, z);
                Particle particleType = particleTypes[random.nextInt(particleTypes.length)];

                for (Player viewer : viewers) {
                    viewer.spawnParticle(particleType, loc, 1, 0.02, 0.02, 0.02, 0.03);
                }
            }
        }

        // Верхняя часть с частицами "разлета" (уменьшенное количество)
        if (timer % 6 == 0) {
            Location top = base.clone().add(0, height, 0);
            for (int i = 0; i < 3; i++) { // Только 3 частицы
                double angle = random.nextDouble() * Math.PI * 2;
                double distance = topRadius * (0.6 + random.nextDouble() * 0.4);
                double x = Math.cos(angle) * distance;
                double z = Math.sin(angle) * distance;

                Location loc = top.clone().add(x, 0.2, z);
                Particle particleType = particleTypes[random.nextInt(particleTypes.length)];

                for (Player viewer : viewers) {
                    viewer.spawnParticle(particleType, loc, 1, 0.05, 0.05, 0.05, 0.03);
                }
            }
        }
    }
}