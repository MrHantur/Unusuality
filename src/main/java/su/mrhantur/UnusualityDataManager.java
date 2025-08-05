package su.mrhantur;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import su.mrhantur.Unusuality;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UnusualityDataManager {

    private final Unusuality plugin;
    private final File dataFile;
    private final FileConfiguration dataConfig;

    public UnusualityDataManager(Unusuality plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "unusualData.yml");

        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create unusualData.yml");
                e.printStackTrace();
            }
        }

        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save unusualData.yml");
            e.printStackTrace();
        }
    }

    public int getKeys(String player) {
        return dataConfig.getInt("players." + player + ".keys", 0);
    }

    public double getProgress(String player) {
        return Math.round(dataConfig.getDouble("players." + player + ".progress", 0.0) * 100.0) / 100.0;
    }

    public boolean getShowEffectPlayer(String player) {
        return dataConfig.getBoolean("players." + player + ".showEffectPlayer", true);
    }

    public boolean getShowAllEffects(String player) {
        return dataConfig.getBoolean("players." + player + ".showAllEffects", true);
    }

    public boolean getCanSeeMyEffect(String player) {
        return dataConfig.getBoolean("players." + player + ".canSeeMyEffect", true);
    }

    public void setKeys(String player, int keys) {
        dataConfig.set("players." + player + ".keys", keys);
        save();
    }

    public void setProgress(String player, double progress) {
        dataConfig.set("players." + player + ".progress", Math.round(progress * 100.0) / 100.0);
        save();
    }

    public void setShowEffectPlayer(String player, boolean value) {
        dataConfig.set("players." + player + ".showEffectPlayer", value);
        save();
    }

    public void setShowAllEffects(String player, boolean value) {
        dataConfig.set("players." + player + ".showAllEffects", value);
        save();
    }

    public void setCanSeeMyEffect(String player, boolean value) {
        dataConfig.set("players." + player + ".canSeeMyEffect", value);
        save();
    }

    public void addKeys(String player, int delta) {
        int keys = getKeys(player);
        setKeys(player, keys + delta);
    }

    public void removeKeys(String player, int delta) {
        int keys = getKeys(player);
        int newKeys = Math.max(0, keys - delta);
        setKeys(player, newKeys);
    }

    public Map<String, Integer> getAllKeys() {
        if (!dataConfig.isConfigurationSection("players")) return Collections.emptyMap();

        Map<String, Integer> keysMap = new HashMap<>();
        for (String player : dataConfig.getConfigurationSection("players").getKeys(false)) {
            keysMap.put(player, getKeys(player));
        }
        return keysMap;
    }

    public Map<String, Double> getAllProgress() {
        if (!dataConfig.isConfigurationSection("players")) return Collections.emptyMap();

        Map<String, Double> progressMap = new HashMap<>();
        for (String player : dataConfig.getConfigurationSection("players").getKeys(false)) {
            progressMap.put(player, getProgress(player));
        }
        return progressMap;
    }

    public void addProgress(String player, double delta) {
        double currentProgress = getProgress(player);
        int keys = getKeys(player);

        double newProgress = currentProgress + delta;

        while (newProgress >= 1.0) {
            newProgress -= 1.0;
            keys++;
        }

        setKeys(player, keys);
        setProgress(player, newProgress);
    }

    public void removeProgress(String player, double delta) {
        double currentProgress = getProgress(player);
        int keys = getKeys(player);

        double newProgress = currentProgress - delta;

        while (newProgress < 0.0 && keys > 0) {
            keys--;
            newProgress += 1.0;
        }

        newProgress = Math.max(0.0, newProgress);
        setKeys(player, Math.max(0, keys));
        setProgress(player, newProgress);
    }
}
