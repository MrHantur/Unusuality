package su.mrhantur.effects;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import su.mrhantur.UnusualEffect;
import java.util.List;

public class sakuraTrails implements UnusualEffect {

    @Override
    public void apply(Entity player, int timer, List<Player> viewers) {
        if (timer % 2 == 0) return;

        Location loc = player.getLocation();

        double t = timer * 0.1; // Плавный множитель времени

        // Лепестки медленно кружатся над игроком, пульсируя вверх-вниз
        for (int i = 0; i < 2; i++) {
            double angle = t + (i * Math.PI);
            double x = Math.cos(angle) * 0.6;
            double z = Math.sin(angle) * 0.6;
            double y = 2.2 + Math.sin(t * 0.5) * 0.1; // Легкое парение

            spawn(viewers, loc.clone().add(x, y, z), Particle.CHERRY_LEAVES);
        }

        // Создает наклонную орбиту лепестков вокруг торса
        if (timer % 4 == 1) {
            double angle = t * 1.5;
            double radius = 0.8;

            // Математика наклона кольца
            double x = radius * Math.cos(angle);
            double y = 1.0 + (radius * Math.sin(angle) * 0.5); // Наклон по вертикали
            double z = radius * Math.sin(angle);

            spawn(viewers, loc.clone().add(x, y, z), Particle.CHERRY_LEAVES);
        }
    }

    // Вспомогательный метод для чистоты кода
    private void spawn(List<Player> viewers, Location loc, Particle particle) {
        for (Player viewer : viewers) {
            viewer.spawnParticle(particle, loc, 0, 0, 0, 0, 0);
        }
    }
}