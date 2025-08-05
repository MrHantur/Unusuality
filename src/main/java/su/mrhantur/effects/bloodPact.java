package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.*;

public class bloodPact implements UnusualEffect {
    // Я вообще хз, как реализовать этот эффект.
    // Очень сложно реализовать мою идею в условиях
    // майна. Хотел что-то вроде эффекта от ульты
    // гули из доты, но получилось так себе


    private final Random random = new Random();

    // Карта для отслеживания времени активации эффекта для каждого игрока
    private static final Map<String, Long> activationTimes = new HashMap<>();
    private static final long ACTIVATION_DURATION = 5000; // 5 секунд яркого свечения

    // Цвета для обычного состояния (тусклые, темные)
    private final Particle.DustOptions[] normalColors = {
            new Particle.DustOptions(Color.fromRGB(80, 20, 20), 0.6f),   // Темно-красный
            new Particle.DustOptions(Color.fromRGB(60, 15, 15), 0.5f),   // Очень темно-красный
            new Particle.DustOptions(Color.fromRGB(40, 10, 10), 0.4f)    // Почти черно-красный
    };

    // Цвета для активированного состояния (яркие, кровавые)
    private final Particle.DustOptions[] activatedColors = {
            new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.6f),    // Ярко-красный
            new Particle.DustOptions(Color.fromRGB(200, 0, 0), 0.5f),    // Темно-красный
            new Particle.DustOptions(Color.fromRGB(255, 50, 50), 0.5f),  // Светло-красный
            new Particle.DustOptions(Color.fromRGB(150, 0, 0), 0.4f)     // Кровавый
    };

    private final Particle.DustOptions coreNormal = new Particle.DustOptions(Color.fromRGB(100, 30, 30), 0.6f);
    private final Particle.DustOptions coreActivated = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.8f);

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 2 != 0) return;

        Location center = player.getLocation().add(0, 2.2, 0);
        String playerName = player.getName();

        // Проверяем, активирован ли эффект
        boolean isActivated = isEffectActivated(playerName);

        if (isActivated) {
            drawActivatedEffect(center, timer, viewers, playerName);
        } else {
            drawNormalEffect(center, timer, viewers);
        }
    }

    // Публичный метод для активации эффекта при убийстве
    public static void activateBloodpact(String playerName) {
        activationTimes.put(playerName, System.currentTimeMillis());
    }

    private boolean isEffectActivated(String playerName) {
        Long activationTime = activationTimes.get(playerName);
        if (activationTime == null) return false;

        long currentTime = System.currentTimeMillis();
        if (currentTime - activationTime > ACTIVATION_DURATION) {
            activationTimes.remove(playerName);
            return false;
        }

        return true;
    }

    private void drawNormalEffect(Location center, int timer, List<Player> viewers) {
        // Тусклый, едва заметный эффект
        if (timer % 6 != 0) return; // Реже спавним частицы

        // Медленное сердцебиение (тусклое)
        double heartbeat = 0.3 + 0.2 * Math.sin(timer * 0.08); // Медленное сердцебиение

        // Рисуем тусклые капилляры
        drawCapillaries(center, timer, viewers, heartbeat, normalColors, false);

        // Тусклое ядро-сердце
        if (timer % 12 == 0) {
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, center, 1, 0.02, 0.02, 0.02, coreNormal);
            }
        }
    }

    private void drawActivatedEffect(Location center, int timer, List<Player> viewers, String playerName) {
        // Хаотичный эффект при активации

        // Рисуем хаотичные капилляры
        drawChaoticCapillaries(center, timer, viewers, playerName);

        // Яркое ядро-сердце
        drawActivatedCore(center, timer, viewers);

        // Кровавые искры
        if (random.nextDouble() < 0.4) {
            drawBloodSparks(center, viewers);
        }

        // Дополнительный хаотичный эффект - случайные брызги
        if (random.nextDouble() < 0.2) {
            drawRandomBloodSplatters(center, viewers);
        }
    }

    private static class CapillaryState {
        double baseAngle;
        double lengthVariation;
        int capillaryIndex;
        int lifeTime = 3; // Капилляр будет отрисовываться 3 кадра
    }

    // Хранилище состояний капилляров для каждого игрока
    private final Map<String, List<CapillaryState>> capillaryStates = new HashMap<>();

    private void drawChaoticCapillaries(Location center, int timer, List<Player> viewers, String playerName) {
        // Создаем новые капилляры только раз в 3 кадра
        if (timer % 6 == 0) {
            List<CapillaryState> states = new ArrayList<>();
            int capillaryCount = 3 + random.nextInt(5);

            for (int i = 0; i < capillaryCount; i++) {
                CapillaryState state = new CapillaryState();
                state.baseAngle = random.nextDouble() * Math.PI * 2;
                state.lengthVariation = 0.2 + random.nextDouble() * 0.8;
                state.capillaryIndex = i;
                states.add(state);
            }
            capillaryStates.put(playerName, states);
        }

        // Отрисовываем существующие капилляры
        List<CapillaryState> states = capillaryStates.get(playerName);
        if (states != null) {
            for (Iterator<CapillaryState> iterator = states.iterator(); iterator.hasNext();) {
                CapillaryState state = iterator.next();
                double chaosIntensity = Math.sin(timer * 0.15 + state.capillaryIndex) * 0.5 + 0.5;

                drawChaoticCapillary(center, state.baseAngle, timer, viewers,
                        chaosIntensity, state.lengthVariation, state.capillaryIndex);

                // Уменьшаем время жизни и удаляем "мертвые" капилляры
                if (--state.lifeTime <= 0) {
                    iterator.remove();
                }
            }

            // Обновляем хранилище
            if (states.isEmpty()) {
                capillaryStates.remove(playerName);
            } else {
                capillaryStates.put(playerName, states);
            }
        }
    }

    private void drawChaoticCapillary(Location center, double baseAngle, int timer, List<Player> viewers,
                                      double chaosIntensity, double lengthVariation, int capillaryIndex) {
        // Хаотичная длина - то сужается, то расширяется
        double pulsation = Math.sin(timer * 0.2 + capillaryIndex * 0.7) * 0.4 + 0.6; // от 0.2 до 1.0
        double maxLength = 0.3 + lengthVariation * 0.7 * pulsation;

        // Хаотичное количество сегментов
        int segments = 6 + random.nextInt(10);

        for (int segment = 1; segment <= segments; segment++) {
            double segmentRatio = (double) segment / segments;
            double distance = maxLength * segmentRatio;

            // Сильные хаотичные колебания
            double angleVariation = Math.sin(timer * 0.25 + segment + baseAngle + capillaryIndex) * 0.3;
            angleVariation += Math.sin(timer * 0.4 + segment * 2) * 0.2;
            double currentAngle = baseAngle + angleVariation;

            // Хаотичные координаты
            double x = Math.cos(currentAngle) * distance;
            double z = Math.sin(currentAngle) * distance;

            // Хаотичное вертикальное движение
            double y = Math.sin(timer * 0.3 + segment + capillaryIndex) * 0.15;
            y += Math.sin(timer * 0.6 + segment) * 0.1;

            // Случайное смещение для большего хаоса
            x += (random.nextDouble() - 0.5) * 0.05;
            z += (random.nextDouble() - 0.5) * 0.05;
            y += (random.nextDouble() - 0.5) * 0.03;

            Location point = center.clone().add(x, y, z);

            // Случайный цвет из палитры
            Particle.DustOptions color = activatedColors[random.nextInt(activatedColors.length)];

            // Хаотичное количество частиц
            int particleCount = 1 + random.nextInt(3);

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, point, particleCount, 0.02, 0.02, 0.02, 0, color);

                // Хаотичный эффект течения крови
                if (random.nextDouble() < 0.03) {
                    viewer.spawnParticle(Particle.CRIT, point, 1, 0.03, 0.03, 0.03, 0.08);
                }

                // Иногда добавляем дополнительные эффекты
                if (random.nextDouble() < 0.05) {
                    viewer.spawnParticle(Particle.BLOCK, point, 1, 0.02, 0.02, 0.02, 0.3,
                            org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
                }
            }

            // Хаотичные ответвления на случайных сегментах
            if (random.nextDouble() < 0.15) {
                drawRandomBranch(point, currentAngle, viewers, distance * 0.3);
            }
        }
    }

    private void drawRandomBranch(Location basePoint, double baseAngle, List<Player> viewers, double branchLength) {
        // Случайное направление ответвления
        double branchAngle = baseAngle + (random.nextDouble() - 0.5) * Math.PI;
        double actualLength = branchLength * (0.3 + random.nextDouble() * 0.7);

        // Несколько точек ответвления
        int branchPoints = 2 + random.nextInt(4);
        for (int i = 1; i <= branchPoints; i++) {
            double ratio = (double) i / branchPoints;
            double distance = actualLength * ratio;

            double x = Math.cos(branchAngle) * distance;
            double z = Math.sin(branchAngle) * distance;
            double y = (random.nextDouble() - 0.5) * 0.1;

            Location branchPoint = basePoint.clone().add(x, y, z);
            Particle.DustOptions color = activatedColors[random.nextInt(activatedColors.length)];

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, branchPoint, 1, 0.01, 0.01, 0.01, 0, color);
            }
        }
    }

    private void drawRandomBloodSplatters(Location center, List<Player> viewers) {
        // Случайные брызги крови в разных направлениях
        int splatters = 3 + random.nextInt(6);

        for (int i = 0; i < splatters; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.4 + random.nextDouble() * 0.6;
            double height = (random.nextDouble() - 0.5) * 0.4;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location splatterPoint = center.clone().add(x, height, z);
            Particle.DustOptions color = activatedColors[random.nextInt(activatedColors.length)];

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, splatterPoint, 2, 0.05, 0.05, 0.05, 0, color);

                if (random.nextDouble() < 0.5) {
                    viewer.spawnParticle(Particle.CRIT, splatterPoint, 1, 0.03, 0.03, 0.03, 0.1);
                }
            }
        }
    }

    private void drawCapillaries(Location center, int timer, List<Player> viewers,
                                 double heartbeat, Particle.DustOptions[] colors, boolean isActivated) {
        // Количество основных капилляров
        int mainCapillaries = 6;

        for (int i = 0; i < mainCapillaries; i++) {
            double baseAngle = i * 2 * Math.PI / mainCapillaries;

            // Основной капилляр
            drawSingleCapillary(center, baseAngle, timer, viewers, heartbeat, colors, isActivated, true);

            // Ответвления (только для активированного состояния)
            if (isActivated) {
                double branchAngle1 = baseAngle + Math.PI / 12;
                double branchAngle2 = baseAngle - Math.PI / 12;

                drawSingleCapillary(center, branchAngle1, timer, viewers, heartbeat * 0.7, colors, isActivated, false);
                drawSingleCapillary(center, branchAngle2, timer, viewers, heartbeat * 0.7, colors, isActivated, false);
            }
        }
    }

    private void drawSingleCapillary(Location center, double angle, int timer, List<Player> viewers,
                                     double heartbeat, Particle.DustOptions[] colors, boolean isActivated, boolean isMain) {
        // Максимальная длина капилляра в зависимости от сердцебиения
        double maxLength = isMain ? 0.6 : 0.4;
        double currentLength = maxLength * heartbeat;

        // Количество сегментов капилляра
        int segments = isMain ? 8 : 5;

        for (int segment = 1; segment <= segments; segment++) {
            double segmentRatio = (double) segment / segments;
            double distance = currentLength * segmentRatio;

            // Добавляем небольшую неровность капилляра
            double angleVariation = Math.sin(timer * 0.1 + segment + angle) * 0.1;
            double currentAngle = angle + angleVariation;

            // Основная позиция
            double x = Math.cos(currentAngle) * distance;
            double z = Math.sin(currentAngle) * distance;

            // Добавляем вертикальное покачивание
            double y = Math.sin(timer * 0.1 + segment) * 0.05;

            // Разветвление на конце (только для основных капилляров)
            if (isMain && segment == segments && isActivated) {
                // Рисуем небольшое разветвление
                for (int branch = 0; branch < 3; branch++) {
                    double branchAngle = currentAngle + (branch - 1) * Math.PI / 8;
                    double branchLength = 0.1;

                    double branchX = x + Math.cos(branchAngle) * branchLength;
                    double branchZ = z + Math.sin(branchAngle) * branchLength;

                    Location branchPoint = center.clone().add(branchX, y, branchZ);
                    Particle.DustOptions branchColor = colors[branch % colors.length];

                    for (Player viewer : viewers) {
                        viewer.spawnParticle(Particle.DUST, branchPoint, 1, 0, 0, 0, 0, branchColor);
                    }
                }
            }

            Location point = center.clone().add(x, y, z);

            // Цвет становится темнее к концу капилляра
            Particle.DustOptions color = colors[segment % colors.length];

            // Для активированного состояния добавляем больше частиц
            int particleCount = isActivated ? 1 : 1;

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, point, particleCount, 0, 0, 0, 0, color);

                // Добавляем эффект течения крови (только для активированного)
                if (isActivated && random.nextDouble() < 0.1) {
                    viewer.spawnParticle(Particle.CRIT, point, 1, 0.02, 0.02, 0.02, 0.05);
                }
            }
        }
    }

    private void drawActivatedCore(Location center, int timer, List<Player> viewers) {
        double pulse = 0.7 + 0.3 * Math.sin(timer * 0.3);
        int particles = (int)(5 * pulse) + 2;

        for (Player viewer : viewers) {
            viewer.spawnParticle(Particle.DUST, center, particles, 0.05, 0.05, 0.05, coreActivated);

            // Добавляем эффект крови
            if (pulse > 0.9) {
                viewer.spawnParticle(Particle.BLOCK, center, 3, 0.1, 0.1, 0.1, 0.5,
                        org.bukkit.Material.REDSTONE_BLOCK.createBlockData());
            }
        }
    }

    private void drawBloodSparks(Location center, List<Player> viewers) {
        // Кровавые искры летят во все стороны
        for (int i = 0; i < 4; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = 0.3 + random.nextDouble() * 0.3;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = random.nextDouble() * 0.2;

            Location sparkPoint = center.clone().add(x, y, z);

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, sparkPoint, 1, 0, 0, 0, 0,
                        activatedColors[random.nextInt(activatedColors.length)]);
                viewer.spawnParticle(Particle.CRIT, sparkPoint, 1, 0.05, 0.05, 0.05, 0.1);
            }
        }
    }
}