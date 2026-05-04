package su.mrhantur.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;

import java.util.*;

public class waterScarf implements UnusualEffect {

    private static final int HISTORY_SIZE = 16;       // длина хвоста шарфа
    private static final int IDLE_THRESHOLD = 16;     // тиков без движения для полного исчезновения
    private static final double SCARF_Y = 1.3;        // высота на уровне шеи

    private final Map<UUID, ScarfState> playerStates = new HashMap<>();

    private static class ScarfState {
        final Deque<Location> history = new ArrayDeque<>(HISTORY_SIZE);
        int stillTicks = 0;      // счётчик неподвижности
        float wavePhase = 0f;    // фаза волны для анимации
    }

    @Override
    public void apply(Entity entity, int timer, List<Player> viewers) {
        if (!(entity instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        Location current = player.getLocation();
        ScarfState state = playerStates.computeIfAbsent(uuid, k -> new ScarfState());

        // Обновляем историю позиций каждый второй тик
        if (timer % 2 == 1) {
            state.history.addFirst(current.clone().add(0, SCARF_Y, 0));
            if (state.history.size() > HISTORY_SIZE) state.history.removeLast();
        }

        // Проверка движения: расстояние между первой и последней точкой
        boolean isMoving = state.history.size() > 2 &&
                state.history.getFirst().distance(state.history.getLast()) > 0.6;

        if (isMoving) state.stillTicks = 0;
        else state.stillTicks++;

        state.wavePhase += 0.1f;

        // Если игрок стоит дольше порога — шарф полностью невидим
        if (state.stillTicks > IDLE_THRESHOLD) {
            return;
        }

        drawFlowingScarf(state, timer, viewers);
    }

    /**
     * Рисует ленту воды при движении.
     * Меньше сегментов и частиц для производительности.
     */
    private void drawFlowingScarf(ScarfState state, int timer, List<Player> viewers) {
        List<Location> points = new ArrayList<>(state.history);
        if (points.size() < 2) return;

        for (int i = 0; i < points.size() - 1; i++) {
            Location p1 = points.get(i);
            Location p2 = points.get(i + 1);
            float progress = (float) i / points.size();

            // Градиент: голубой → тёмно-синий
            Color color = interpolateColor(
                    Color.fromRGB(0, 200, 255),
                    Color.fromRGB(0, 30, 120),
                    progress
            );
            Particle.DustOptions opts = new Particle.DustOptions(color, 0.7f - progress * 0.2f);

            int segments = 4;   // уменьшено с 5
            for (int s = 0; s < segments; s++) {
                double ratio = (double) s / segments;
                double x = lerp(p1.getX(), p2.getX(), ratio);
                double y = lerp(p1.getY(), p2.getY(), ratio);
                double z = lerp(p1.getZ(), p2.getZ(), ratio);

                // Лёгкая волна из стороны в сторону, затухающая к хвосту
                double wave = Math.sin(timer * 0.09 + i * 0.5 + state.wavePhase) * 0.18 * (1 - progress);
                x += Math.cos(timer * 0.04 + i) * wave * 0.3;
                z += Math.sin(timer * 0.04 + i) * wave * 0.3;
                y += Math.cos(timer * 0.15 + i * 0.3) * 0.05;

                Location spawn = new Location(p1.getWorld(), x, y, z);
                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.DUST, spawn, 1, 0, 0, 0, 0, opts);
                    // Редкие пузырьки и капли воды
                    if (Math.random() < 0.02) {
                        viewer.spawnParticle(Particle.BUBBLE, spawn, 0, 0.1, 0.1, 0.1, 0.02);
                    }
                    if (Math.random() < 0.05) {
                        viewer.spawnParticle(Particle.FALLING_WATER, spawn, 0, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }
        }
    }

    // Вспомогательные методы линейной интерполяции

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private Color interpolateColor(Color c1, Color c2, float t) {
        int r = (int) (c1.getRed()   + (c2.getRed()   - c1.getRed())   * t);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int) (c1.getBlue()  + (c2.getBlue()  - c1.getBlue())  * t);
        return Color.fromRGB(r, g, b);
    }

    public void cleanup(UUID uuid) {
        playerStates.remove(uuid);
    }
}