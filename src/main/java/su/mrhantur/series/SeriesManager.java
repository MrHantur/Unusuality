package su.mrhantur.series;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import su.mrhantur.Unusuality;

import java.util.*;

public class SeriesManager {
    private final Unusuality plugin;
    private final Map<String, EnchantmentSeries> series = new HashMap<>();
    private final Map<String, List<String>> enchantmentNames = new HashMap<>(); // Новое поле для хранения названий

    public SeriesManager(Unusuality plugin) {
        this.plugin = plugin;
        initializeSeries();
    }

    private void initializeSeries() {
        // Серия #1
        enchantmentNames.put("first", Arrays.asList(
                "Светлячки",
                "Зелёная энергия",
                "Конфетти",
                "Галактика",
                "Беспокойные души",
                "Астральный шаг"
        ));

        // Серия #2
        enchantmentNames.put("second", Arrays.asList(
                "Штормовое облако",
                "Утечка памяти",
                "Нейтронная звезда",
                "Пылающая лампа",
                "Пузырьки",
                "Орбитальный огонёк",
                "Горный нимб",
                "Майами"
        ));

        // Серия #3
        enchantmentNames.put("third", Arrays.asList(
                "Смерч",
                "Карусель",
                "Звездочёт",
                "Отторжение",
                "Радиация",
                "Неоновое электричество",
                "Дьявольские рога",
                "Кровавый договор"
        ));


        // Серия #1
        List<Enchantment> firstSeries = new ArrayList<>(Arrays.asList(
                getEnchantmentByKey("fireflies"),
                getEnchantmentByKey("green_energy"),
                getEnchantmentByKey("confetti"),
                getEnchantmentByKey("galaxy"),
                getEnchantmentByKey("restless_souls"),
                getEnchantmentByKey("astral_step")
        ));
        firstSeries.removeIf(Objects::isNull);

        // Серия #2
        List<Enchantment> secondSeries = new ArrayList<>(Arrays.asList(
                getEnchantmentByKey("stormcloud"),
                getEnchantmentByKey("memory_leak"),
                getEnchantmentByKey("neutron_star"),
                getEnchantmentByKey("flaming_lantern"),
                getEnchantmentByKey("bubbling"),
                getEnchantmentByKey("orbiting_fire"),
                getEnchantmentByKey("mountain_halo"),
                getEnchantmentByKey("miami_nights")
        ));
        secondSeries.removeIf(Objects::isNull);

        // Серия #3
        List<Enchantment> thirdSeries = new ArrayList<>(Arrays.asList(
                getEnchantmentByKey("tornado"),
                getEnchantmentByKey("stargazer"),
                getEnchantmentByKey("rejection"),
                getEnchantmentByKey("radiation"),
                getEnchantmentByKey("neon_electricity"),
                getEnchantmentByKey("devil_horns"),
                getEnchantmentByKey("carousel"),
                getEnchantmentByKey("blood_pact")
        ));
        thirdSeries.removeIf(Objects::isNull);


        // Серия #1
        registerSeries(new EnchantmentSeries(
                plugin, // Передаем плагин
                "first",
                "§2Серия #1",
                "Самые первые зачарования (snapshot 3)",
                firstSeries,
                Material.NAUTILUS_SHELL,
                1,
                20.0
        ));

        // Серия #2
        registerSeries(new EnchantmentSeries(
                plugin, // Передаем плагин
                "second",
                "§5Серия #2",
                "Более классические зачарования (version 1.1)",
                secondSeries,
                Material.AMETHYST_SHARD,
                1,
                20.0
        ));

        // Серия #3
        registerSeries(new EnchantmentSeries(
                plugin, // Передаем плагин
                "third",
                "§3Серия #3",
                "Экспериментальные зачарования (version 2.0)",
                thirdSeries,
                Material.GOLDEN_APPLE,
                1,
                20.0
        ));
    }

    private Enchantment getEnchantmentByKey(String key) {
        return Enchantment.getByKey(NamespacedKey.fromString("unusuality:" + key));
    }

    public void registerSeries(EnchantmentSeries series) {
        this.series.put(series.getId(), series);
    }

    public EnchantmentSeries getSeries(String id) {
        return series.get(id);
    }

    public Collection<EnchantmentSeries> getAllSeries() {
        return series.values();
    }

    public List<EnchantmentSeries> getAvailableSeries() {
        return series.values().stream()
                .filter(s -> !s.getEnchantments().isEmpty())
                .toList();
    }

    public List<String> getEnchantmentNames(String seriesId) {
        return enchantmentNames.getOrDefault(seriesId, Collections.emptyList());
    }

    public boolean hasSeriesWithId(String id) {
        return series.containsKey(id);
    }
}