package su.mrhantur;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import java.util.List;

public interface UnusualEffect {
    void apply(Entity player, int timer, List<Player> viewers);
}