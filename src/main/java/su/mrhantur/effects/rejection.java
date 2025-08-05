package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.List;
import java.util.Random;

public class rejection implements UnusualEffect {
    private final Random random = new Random();
    private final Particle.DustOptions[] colors = {
            new Particle.DustOptions(Color.fromRGB(100, 200, 255), 0.8f),
            new Particle.DustOptions(Color.fromRGB(50, 100, 255), 0.8f),
            new Particle.DustOptions(Color.fromRGB(200, 150, 255), 0.8f)
    };
    private final Particle.DustOptions coreColor = new Particle.DustOptions(Color.WHITE, 0.8f);

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 2 != 0) return;

        Location base = player.getLocation().add(0, 2.5, 0);
        int period = 120; // Увеличенный период для плавности
        double progress = (timer % period) / (double) period;

        // Плавная функция амплитуды (мягкий старт и финиш)
        double amplitude = easeInOutQuad(progress);

        // Генерация трех траекторий
        for (int i = 0; i < 3; i++) {
            double phase = i * 2 * Math.PI / 3;

            // Непрерывный угол с плавным переходом между циклами
            double angle = 2 * Math.PI * progress + phase;

            // Параметры траектории с плавным изменением
            double radius = 0.5 * amplitude;
            double height = 0.3 * amplitude;
            int waves = 3; // Уменьшенное количество волн для плавности

            // Координаты с плавным движением
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);
            double y = height * Math.sin(waves * angle) + 0.1;

            // Плавное появление частиц в начале цикла
            double visibility = Math.min(1.0, progress * 5);
            if (visibility > 0.1) {
                Location pos = base.clone().add(x, y, z);
                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.DUST, pos, 0, 0, 0, 0, colors[i]);

                    // Плавное затухание в конце цикла
                    if (progress > 0.85) {
                        double fade = (1 - progress) * 5;
                        viewer.spawnParticle(Particle.DUST, pos, 0, 0, 0, 0,
                                new Particle.DustOptions(colors[i].getColor(), (float)(colors[i].getSize() * fade)));
                    }
                }
            }
        }

        // Генерация ядра с плавными переходами
        double coreIntensity = amplitude * (0.5 + 0.5 * Math.sin(progress * 4 * Math.PI));
        Location corePos = base.clone().add(0, 0.1, 0);
        int particles = (int) (3 * coreIntensity) + 1;

        for (Player viewer : viewers) {
            // Основное ядро
            viewer.spawnParticle(Particle.DUST, corePos, particles, 0.05, 0.05, 0.05, coreColor);

            // Плавное появление искр
            if (coreIntensity > 0.7 && random.nextDouble() < 0.3) {
                viewer.spawnParticle(Particle.ELECTRIC_SPARK, corePos, 1, 0.1, 0.1, 0.1, 0.01);
            }

            // Эффект "шлейфа" при движении
            if (progress > 0.1 && progress < 0.9) {
                double tailProgress = progress - 0.05;
                double tailAmplitude = easeInOutQuad(tailProgress);
                for (int i = 0; i < 3; i++) {
                    double phase = i * 2 * Math.PI / 3;
                    double angle = 2 * Math.PI * tailProgress + phase;
                    double radius = 0.5 * tailAmplitude;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    double y = 0.3 * tailAmplitude * Math.sin(3 * angle) + 0.1;

                    viewer.spawnParticle(Particle.DUST, base.clone().add(x, y, z), 0, 0, 0, 0,
                            new Particle.DustOptions(colors[i].getColor(), 0.4f));
                }
            }
        }
    }

    // Функция для плавного ускорения и замедления
    private double easeInOutQuad(double x) {
        return x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2;
    }
}