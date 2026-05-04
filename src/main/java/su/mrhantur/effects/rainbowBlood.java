package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.*;

public class rainbowBlood implements UnusualEffect {

    // Настройки частиц
    private static final float DUST_SIZE = 1.4f;          // размер частицы
    private static final int MIN_PARTICLES = 8;          // минимум частиц при любом уроне
    private static final int MAX_PARTICLES = 30;          // максимум, чтобы не перегружать
    private static final double SPREAD_XZ = 0.9;          // радиус разлёта по горизонтали
    private static final double SPREAD_Y_MIN = 0.2;       // нижняя граница по Y
    private static final double SPREAD_Y_MAX = 1.3;       // верхняя граница по Y

    // Заранее подготовленные цвета радуги и опции частиц
    private static final Color[] RAINBOW_COLORS = {
            Color.fromRGB(255, 0, 0),    // Красный
            Color.fromRGB(255, 128, 0),  // Оранжевый
            Color.fromRGB(255, 255, 0),  // Жёлтый
            Color.fromRGB(0, 255, 0),    // Зелёный
            Color.fromRGB(0, 255, 255),  // Голубой
            Color.fromRGB(0, 128, 255),  // Синий
            Color.fromRGB(128, 0, 255)   // Фиолетовый
    };
    private static final Particle.DustOptions[] DUST_OPTIONS = new Particle.DustOptions[RAINBOW_COLORS.length];

    static {
        for (int i = 0; i < RAINBOW_COLORS.length; i++) {
            DUST_OPTIONS[i] = new Particle.DustOptions(RAINBOW_COLORS[i], DUST_SIZE);
        }
    }

    // Храним предыдущее здоровье игроков, чтобы отслеживать урон
    private final Map<UUID, Double> previousHealth = new HashMap<>();

    @Override
    public void apply(Entity entity, int timer, List<Player> viewers) {
        if (!(entity instanceof Player player)) return;

        double currentHealth = player.getHealth();
        UUID uuid = player.getUniqueId();
        double oldHealth = previousHealth.getOrDefault(uuid, currentHealth);

        // Если здоровье уменьшилось — игрок получил урон
        if (currentHealth < oldHealth) {
            double damageTaken = oldHealth - currentHealth;
            // Количество частиц зависит от величины урона
            int count = (int) Math.min(MIN_PARTICLES + damageTaken * 3, MAX_PARTICLES);
            spawnRainbowBlood(player.getLocation().add(0, 1.0, 0), count, viewers);
        }

        previousHealth.put(uuid, currentHealth);
    }

    // Создаёт взрыв радужных частиц вокруг указанной точки.
    private void spawnRainbowBlood(Location centre, int count, List<Player> viewers) {
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            // Случайное смещение внутри полусферы
            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = random.nextDouble() * SPREAD_XZ;
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;
            double dy = SPREAD_Y_MIN + random.nextDouble() * (SPREAD_Y_MAX - SPREAD_Y_MIN);

            Location spawnLoc = centre.clone().add(dx, dy, dz);

            // Случайный цвет из подготовленного набора
            Particle.DustOptions dust = DUST_OPTIONS[random.nextInt(DUST_OPTIONS.length)];

            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.DUST, spawnLoc, 1, 0, 0, 0, 0, dust);
            }
        }
    }

    // Очистка данных игрока при выходе
    public void cleanup(UUID uuid) {
        previousHealth.remove(uuid);
    }
}