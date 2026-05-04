package su.mrhantur.series;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import su.mrhantur.Unusuality;

import java.util.*;

public class SeriesManager {
    private final Unusuality plugin;
    private final Map<String, EnchantmentSeries> series = new HashMap<>();
    private final Map<String, List<String>> enchantmentNames = new HashMap<>();

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

        // Серия #3 (порядок соответствует названиям)
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

        // Серия #4
        enchantmentNames.put("fourth", Arrays.asList(
                "Спутник",
                "Своя могила",
                "Тихие ночи",
                "Ракеты",
                "След сакуры",
                "NO SOUND, NO MEMORY",
                "Водный шарф",
                "Радужная кровь"
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

        // Серия #3 (порядок: tornado, carousel, stargazer, ...)
        List<Enchantment> thirdSeries = new ArrayList<>(Arrays.asList(
                getEnchantmentByKey("tornado"),
                getEnchantmentByKey("carousel"),
                getEnchantmentByKey("stargazer"),
                getEnchantmentByKey("rejection"),
                getEnchantmentByKey("radiation"),
                getEnchantmentByKey("neon_electricity"),
                getEnchantmentByKey("devil_horns"),
                getEnchantmentByKey("blood_pact")
        ));
        thirdSeries.removeIf(Objects::isNull);

        // Серия #4
        List<Enchantment> fourthSeries = new ArrayList<>(Arrays.asList(
                getEnchantmentByKey("sputnik"),
                getEnchantmentByKey("own_grave"),
                getEnchantmentByKey("silent_nights"),
                getEnchantmentByKey("rockets"),
                getEnchantmentByKey("sakura_trails"),
                getEnchantmentByKey("no_sound_no_memory"),
                getEnchantmentByKey("water_scarf"),
                getEnchantmentByKey("rainbow_blood")
        ));
        fourthSeries.removeIf(Objects::isNull);

        registerSeries(new EnchantmentSeries(
                plugin,
                "first",
                "§2Серия #1",
                "Самые первые зачарования (snapshot 3)",
                firstSeries,
                Material.NAUTILUS_SHELL,
                1,
                20.0
        ));

        registerSeries(new EnchantmentSeries(
                plugin,
                "second",
                "§5Серия #2",
                "Более классические зачарования (version 1.1)",
                secondSeries,
                Material.AMETHYST_SHARD,
                1,
                20.0
        ));

        registerSeries(new EnchantmentSeries(
                plugin,
                "third",
                "§3Серия #3",
                "Экспериментальные зачарования (version 2.0)",
                thirdSeries,
                Material.GOLDEN_APPLE,
                1,
                20.0
        ));

        registerSeries(new EnchantmentSeries(
                plugin,
                "fourth",
                "§dСерия #4",
                "Зачарования с фишками (version 2.2)",
                fourthSeries,
                Material.DRAGON_BREATH,
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