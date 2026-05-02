package su.mrhantur.effects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import su.mrhantur.UnusualEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ownGrave implements UnusualEffect {

    // ──────────────────────────────────────────────
    // НАСТРОЙКИ ЭФФЕКТА
    // ──────────────────────────────────────────────

    private static final int TICKS_BEFORE_FIRST = 60;
    private static final int TICKS_PER_LAYER = 50;

    /** Блоки для каждого слоя (снизу вверх) */
    private static final Material[] LAYER_BLOCKS = {
            Material.SOUL_SOIL,
            Material.COARSE_DIRT,
            Material.GRAVEL,
            Material.CRIMSON_ROOTS
    };

    /** Высота каждого слоя относительно ног игрока */
    private static final float[] LAYER_HEIGHTS = {0f, 0.7f, 1.15f, 1.7f};

    /** Коэффициент уменьшения масштаба для каждого следующего слоя */
    private static final float LAYER_SCALE_DECREASE = 0.15f;

    // ──────────────────────────────────────────────
    // ВНУТРЕННИЕ КЛАССЫ
    // ──────────────────────────────────────────────

    private static class BurialState {
        Location anchor;
        final List<BlockDisplay> layers = new ArrayList<>();
        TextDisplay graveMarker = null;
        int standTicks = 0;
        boolean fullyBuried = false;
        int auraPhase = 0;
    }

    // ──────────────────────────────────────────────
    // ХРАНЕНИЕ СОСТОЯНИЙ
    // ──────────────────────────────────────────────

    private final Map<UUID, BurialState> activeBurials = new HashMap<>();
    private final Map<UUID, Location> lastPositions = new HashMap<>();

    // ──────────────────────────────────────────────
    // ТОЧКА ВХОДА
    // ──────────────────────────────────────────────

    @Override
    public void apply(Entity entity, int timer, List<Player> viewers) {
        if (!(entity instanceof Player player)) return;

        Location current = player.getLocation();

        if (isPlayerMoving(player, current)) {
            eruptBurial(player, viewers);
            return;
        }

        continueBurial(player, current, timer, viewers);
    }

    // ──────────────────────────────────────────────
    // ПРОВЕРКА ДВИЖЕНИЯ
    // ──────────────────────────────────────────────

    private boolean isPlayerMoving(Player player, Location current) {
        Location prev = lastPositions.put(player.getUniqueId(), current.clone());

        if (prev == null || !prev.getWorld().equals(current.getWorld())) {
            return false;
        }

        double dx = current.getX() - prev.getX();
        double dz = current.getZ() - prev.getZ();
        return (dx * dx + dz * dz) > 0.0025;
    }

    // ──────────────────────────────────────────────
    // ПРОЦЕСС ЗАКАПЫВАНИЯ
    // ──────────────────────────────────────────────

    private void continueBurial(Player player, Location loc, int timer, List<Player> viewers) {
        BurialState state = activeBurials.computeIfAbsent(
                player.getUniqueId(), k -> new BurialState());

        if (state.anchor == null) {
            state.anchor = snapToBlockGrid(loc);
        }

        state.standTicks++;
        state.auraPhase = (state.auraPhase + 1) % 360;

        // Обновляем позиции слоёв
        for (int i = 0; i < state.layers.size(); i++) {
            BlockDisplay layer = state.layers.get(i);
            if (!layer.isDead()) {
                layer.teleport(getLayerOrigin(state.anchor, i));
            }
        }

        // Обновляем надгробие
        if (state.graveMarker != null && !state.graveMarker.isDead()) {
            state.graveMarker.teleport(state.anchor.clone().add(0.5, 2.9, 0.5));
        }

        // Добавляем новый слой
        int nextLayer = state.layers.size();
        if (nextLayer < LAYER_BLOCKS.length) {
            int requiredTicks = TICKS_BEFORE_FIRST + nextLayer * TICKS_PER_LAYER;
            if (state.standTicks == requiredTicks) {
                spawnLayer(state, nextLayer, viewers);
            }
        }

        // Завершение закапывания
        if (!state.fullyBuried && state.layers.size() == LAYER_BLOCKS.length) {
            state.fullyBuried = true;
            state.graveMarker = spawnGraveMarker(player, state.anchor, viewers, timer);
            triggerBurialCompleteEffects(state.anchor, viewers);
        }

        // Атмосферные эффекты (только дым костра)
        if (state.fullyBuried) {
            spawnBuriedAura(state.anchor, state.auraPhase, viewers);
        } else if (!state.layers.isEmpty()) {
            spawnPartialBurialParticles(state.anchor, timer, viewers);
        }

        // Редкий шёпот (только дым)
        if (!state.layers.isEmpty() && timer % 20 == 0) {
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,
                        state.anchor.clone().add(0.5, 0.3, 0.5),
                        1, 0.1, 0.05, 0.1, 0.02);
            }
        }
    }

    // ──────────────────────────────────────────────
    // СОЗДАНИЕ СЛОЯ ЗЕМЛИ (с уменьшающимся масштабом)
    // ──────────────────────────────────────────────

    private void spawnLayer(BurialState state, int layerIndex, List<Player> viewers) {
        Location origin = getLayerOrigin(state.anchor, layerIndex);
        Material blockType = LAYER_BLOCKS[layerIndex];

        BlockDisplay display = (BlockDisplay) origin.getWorld()
                .spawnEntity(origin, EntityType.BLOCK_DISPLAY);

        display.setBlock(blockType.createBlockData());
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setPersistent(false);
        display.setBillboard(BlockDisplay.Billboard.FIXED);

        float scale = getLayerScale(layerIndex);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(20);
        display.setTransformation(createScaleTransform(scale, 0.05f, scale));
        display.setTransformation(createScaleTransform(scale, 1f, scale));

        state.layers.add(display);

        for (Player viewer : viewers) {
            viewer.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,
                    origin.clone().add(0.5, 0.2, 0.5),
                    1, 0.15, 0.05, 0.15, 0.01);   // count = 1, разлёт уменьшен
        }
    }

    // ──────────────────────────────────────────────
    // НАДГРОБИЕ С ТЕКСТОМ
    // ──────────────────────────────────────────────

    private TextDisplay spawnGraveMarker(Player player, Location anchor, List<Player> viewers, int timer) {
        Location pos = anchor.clone().add(0.5, 2.9, 0.5);

        TextDisplay display = (TextDisplay) anchor.getWorld()
                .spawnEntity(pos, EntityType.TEXT_DISPLAY);

        display.setBillboard(TextDisplay.Billboard.VERTICAL);
        display.setBackgroundColor(Color.fromARGB(200, 10, 10, 20));
        display.setShadowed(true);
        display.setInvulnerable(true);
        display.setGravity(false);
        display.setPersistent(false);
        display.setDefaultBackground(true);
        display.setSeeThrough(true);
        display.setTextOpacity((byte) 220);

        display.text(
                Component.text("☠ ЗДЕСЬ ПОКОИТСЯ ☠", NamedTextColor.DARK_GRAY, TextDecoration.BOLD)
                        .appendNewline()
                        .append(Component.text(player.getName(), NamedTextColor.GRAY, TextDecoration.BOLD))
                        .appendNewline()
                        .append(Component.text("Слишком долго стоял",
                                NamedTextColor.GRAY, TextDecoration.ITALIC))
        );

        // Только дым костра при появлении
        if (timer % 4 == 0) {
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pos, 1, 0.15, 0.05, 0.15, 0.01);
            }
        }

        return display;
    }

    // ──────────────────────────────────────────────
    // ЭФФЕКТЫ ПОЛНОГО ЗАКАПЫВАНИЯ (только дым)
    // ──────────────────────────────────────────────

    private void triggerBurialCompleteEffects(Location anchor, List<Player> viewers) {
        Location center = anchor.clone().add(0.5, 2.0, 0.5);

        for (Player viewer : viewers) {
            viewer.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, center, 2, 0.2, 0.1, 0.2, 0.02);
        }
    }

    // ──────────────────────────────────────────────
    // АУРА ПОГРЕБЁННОГО ИГРОКА (только дым)
    // ──────────────────────────────────────────────

    private void spawnBuriedAura(Location anchor, int phase, List<Player> viewers) {
        Location center = anchor.clone().add(0.5, 1.5, 0.5);
        double radius = 0.5 + Math.sin(phase / 20.0) * 0.1;
        for (int i = 0; i < 3; i++) {                          // 3 вместо 6
            double angle = 2 * Math.PI * i / 3 + phase / 30.0; // деление на 3
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.sin(phase / 15.0 + i) * 0.1;
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,
                        center.clone().add(x, y, z),
                        1, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    // ──────────────────────────────────────────────
    // ЧАСТИЦЫ ПРИ ЧАСТИЧНОМ ЗАКАПЫВАНИИ (только дым)
    // ──────────────────────────────────────────────

    private void spawnPartialBurialParticles(Location anchor, int timer, List<Player> viewers) {
        if (timer % 20 != 0) return;            // раз в секунду (20 тиков)
        Location base = anchor.clone().add(0.5, 0.8, 0.5);
        for (Player viewer : viewers) {
            viewer.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,
                    base.clone().add(
                            (Math.random() - 0.5) * 0.4,
                            Math.random() * 0.3,
                            (Math.random() - 0.5) * 0.4),
                    1, 0, 0.01, 0, 0);          // count = 1, лёгкий подъём
        }
    }

    // ──────────────────────────────────────────────
    // ИЗВЕРЖЕНИЕ (ПРЕРЫВАНИЕ РИТУАЛА) — только дым
    // ──────────────────────────────────────────────

    private void eruptBurial(Player player, List<Player> viewers) {
        BurialState state = activeBurials.remove(player.getUniqueId());
        if (state == null) return;

        Location center = state.anchor.clone().add(0.5, 1.0, 0.5);

        // Если могила была полностью построена – устраиваем взрыв
        if (state.fullyBuried) {
            // Частицы на каждом слое
            for (int i = 0; i < state.layers.size(); i++) {
                BlockDisplay layer = state.layers.get(i);
                if (layer.isDead()) continue;

                layer.setInterpolationDelay(0);
                layer.setInterpolationDuration(8);
                layer.setTransformation(createScaleTransform(0.2f, 3f, 0.2f));

                for (Player viewer : viewers) {
                    viewer.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,
                            center.clone().add(0, LAYER_HEIGHTS[i] + 0.3, 0),
                            5, 0.2, 0.1, 0.2, 0.01);
                }
                layer.remove();
            }

            // Финальный залп
            for (Player viewer : viewers) {
                viewer.spawnParticle(Particle.EXPLOSION,
                        center, 2, 0.3, 0.3, 0.3, 0.02);
            }

            if (state.graveMarker != null && !state.graveMarker.isDead()) {
                state.graveMarker.remove();
            }
        } else {
            // Неполная могила – тихое удаление без частиц
            for (BlockDisplay layer : state.layers) {
                if (!layer.isDead()) {
                    layer.remove();
                }
            }
            // Надгробия ещё нет, поэтому проверка не нужна
        }
    }

    // ──────────────────────────────────────────────
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ──────────────────────────────────────────────

    /** Центрирует позицию по блоку для ровной укладки */
    private static Location snapToBlockGrid(Location loc) {
        return new Location(
                loc.getWorld(),
                loc.getX() - 0.6,
                loc.getY(),
                loc.getZ() - 0.6
        );
    }

    /** Возвращает точку спавна для слоя с компенсацией масштаба – пирамида */
    private static Location getLayerOrigin(Location anchor, int index) {
        Location loc = anchor.clone();

        // Центр самого нижнего слоя (масштаб первого слоя)
        float baseScale = getLayerScale(0);
        double centerX = anchor.getX() + baseScale / 2.0;
        double centerZ = anchor.getZ() + baseScale / 2.0;

        // Масштаб текущего слоя
        float scale = getLayerScale(index);

        // Смещаем левый нижний угол так, чтобы геометрический центр блока совпал с центром нижнего слоя
        loc.setX(centerX - scale / 2.0);
        loc.setZ(centerZ - scale / 2.0);

        // Добавляем вертикальное смещение (Y остаётся из anchor + высота слоя)
        loc.add(0, LAYER_HEIGHTS[index], 0);

        return loc;
    }

    /** Возвращает масштаб для слоя (каждый следующий меньше) */
    private static float getLayerScale(int layerIndex) {
        return Math.max(0.4f, 1.3f - (layerIndex * LAYER_SCALE_DECREASE));
    }

    /** Создаёт трансформацию масштабирования */
    private static Transformation createScaleTransform(float x, float y, float z) {
        AxisAngle4f identity = new AxisAngle4f();
        return new Transformation(new Vector3f(), identity, new Vector3f(x, y, z), identity);
    }

    // ──────────────────────────────────────────────
    // ОЧИСТКА
    // ──────────────────────────────────────────────

    public void cleanup(Player player) {
        BurialState state = activeBurials.remove(player.getUniqueId());
        if (state != null) {
            state.layers.forEach(layer -> {
                if (!layer.isDead()) layer.remove();
            });
            if (state.graveMarker != null && !state.graveMarker.isDead()) {
                state.graveMarker.remove();
            }
        }
        lastPositions.remove(player.getUniqueId());
    }

    public void cleanupAll() {
        // Делаем копию, чтобы избежать ConcurrentModification
        for (UUID uuid : new ArrayList<>(activeBurials.keySet())) {
            BurialState state = activeBurials.remove(uuid);
            if (state != null) {
                state.layers.forEach(layer -> {
                    if (!layer.isDead()) layer.remove();
                });
                if (state.graveMarker != null && !state.graveMarker.isDead()) {
                    state.graveMarker.remove();
                }
            }
        }
        lastPositions.clear();
    }
}