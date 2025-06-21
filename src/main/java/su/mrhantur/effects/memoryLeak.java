package su.mrhantur.effects;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;
import su.mrhantur.UnusualEffect;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class memoryLeak implements UnusualEffect {
    private final Random random = new Random();
    private final String[] memoryChunks = {
            "0xDEAD", "null", "0xMEM", "NaN", "@over", "ERR", "404", "*void"
    };
    private final Set<TextDisplay> displays = ConcurrentHashMap.newKeySet();

    @Override
    public void apply(Player player, int timer) {
        if (timer % 30 != 0 || random.nextDouble() > 0.6) return;

        Location base = player.getLocation();
        Location loc = base.clone().add(
                (random.nextDouble() - 0.5) * 0.3, // x drift
                2.2 + random.nextDouble() * 0.3,   // y above head
                (random.nextDouble() - 0.5) * 0.3  // z drift
        );

        String text = memoryChunks[random.nextInt(memoryChunks.length)];

        TextDisplay display = loc.getWorld().spawn(loc, TextDisplay.class);
        display.setText(randomStyle(text));
        display.setBillboard(TextDisplay.Billboard.CENTER);
        display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        display.setSeeThrough(true);
        display.setShadowed(false);
        display.setPersistent(false);
        display.setViewRange(32);
        display.setRotation(0, 0);

        // Вектор медленного падения + дрейф по сторонам
        Vector velocity = new Vector(
                (random.nextDouble() - 0.5) * 0.01,
                -0.01 - random.nextDouble() * 0.015,
                (random.nextDouble() - 0.5) * 0.01
        );
        display.setVelocity(velocity);

        displays.add(display);

        if (random.nextDouble() < 0.5) {
            Particle type = random.nextBoolean() ? Particle.CRIT : Particle.ENCHANT;
            loc.getWorld().spawnParticle(type, loc, 1, 0.01, 0.01, 0.01, 0);
        }

        if (random.nextDouble() < 0.35) {
            Particle type = random.nextBoolean() ? Particle.PORTAL : Particle.WAX_ON;
            loc.getWorld().spawnParticle(type, loc, 1, 0.01, 0.01, 0.01, 0);
        }

        if (random.nextDouble() < 0.1) {
            Particle type = random.nextBoolean() ? Particle.ANGRY_VILLAGER : Particle.HAPPY_VILLAGER;
            loc.getWorld().spawnParticle(type, loc, 1, 0.01, 0.01, 0.01, 0);
        }

        // Удаление через 1.5 секунды (30 тиков)
        Bukkit.getScheduler().runTaskLater(
                Bukkit.getPluginManager().getPlugin("Unusuality"),
                () -> {
                    display.remove();
                    displays.remove(display);
                },
                30L
        );
    }

    private String randomStyle(String text) {
        String[] colorCodes = {"§a", "§b", "§7", "§f", "§d"};
        String[] styles = {"", "§o", "§k"};

        return colorCodes[random.nextInt(colorCodes.length)] +
                styles[random.nextInt(styles.length)] +
                text;
    }

    public void clearDisplays() {
        for (TextDisplay display : displays) {
            if (display.isValid()) {
                display.remove();
            }
        }
        displays.clear();
    }
}
