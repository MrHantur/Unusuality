package su.mrhantur;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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
                plugin.getLogger().warning("Не удалось создать unusualData.yml");
                e.printStackTrace();
            }
        }

        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось сохранить unusualData.yml");
            e.printStackTrace();
        }
    }

    // ── Геттеры ──────────────────────────────────────────────────────────────

    public int getKeys(String player) {
        return dataConfig.getInt("players." + player + ".keys", 0);
    }

    public double getProgress(String player) {
        return round2(dataConfig.getDouble("players." + player + ".progress", 0.0));
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

    public boolean getOfferAnotherCase(String player) {
        return dataConfig.getBoolean("players." + player + ".offerAnotherCase", false);
    }

    public boolean getAnnounceJackpot(String player) {
        return dataConfig.getBoolean("players." + player + ".announceJackpot", false);
    }

    // ── Сеттеры ──────────────────────────────────────────────────────────────

    public void setKeys(String player, int keys) {
        dataConfig.set("players." + player + ".keys", keys);
        save();
    }

    public void setProgress(String player, double progress) {
        dataConfig.set("players." + player + ".progress", round2(progress));
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

    public void setOfferAnotherCase(String player, boolean value) {
        dataConfig.set("players." + player + ".offerAnotherCase", value);
        save();
    }

    public void setAnnounceJackpot(String player, boolean value) {
        dataConfig.set("players." + player + ".announceJackpot", value);
        save();
    }

    // ── Составные операции ───────────────────────────────────────────────────

    public void addKeys(String player, int delta) {
        setKeys(player, getKeys(player) + delta);
    }

    public void removeKeys(String player, int delta) {
        setKeys(player, Math.max(0, getKeys(player) - delta));
    }

    public void addProgress(String player, double delta) {
        double progress = getProgress(player);
        int keys = getKeys(player);

        progress += delta;
        if (progress >= 1.0) {
            int steps = (int)progress;
            keys += steps;
            progress -= steps;
        }

        setKeys(player, keys);
        setProgress(player, progress);
    }

    public void removeProgress(String player, double delta) {
        double progress = getProgress(player);
        int keys = getKeys(player);

        progress -= delta;
        while (progress < 0.0 && keys > 0) {
            keys--;
            progress += 1.0;
        }

        setKeys(player, Math.max(0, keys));
        setProgress(player, Math.max(0.0, progress));
    }

    // ── Массовое чтение ─────────────────────────────────────────────────────

    public Map<String, Integer> getAllKeys() {
        if (!dataConfig.isConfigurationSection("players")) return Collections.emptyMap();

        Map<String, Integer> result = new HashMap<>();
        for (String player : dataConfig.getConfigurationSection("players").getKeys(false)) {
            result.put(player, getKeys(player));
        }
        return result;
    }

    public Map<String, Double> getAllProgress() {
        if (!dataConfig.isConfigurationSection("players")) return Collections.emptyMap();

        Map<String, Double> result = new HashMap<>();
        for (String player : dataConfig.getConfigurationSection("players").getKeys(false)) {
            result.put(player, getProgress(player));
        }
        return result;
    }

    // ── Вспомогательные методы ───────────────────────────────────────────────

    // Округляет значение до 2 десятичных знаков
    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}