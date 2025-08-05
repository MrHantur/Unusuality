package su.mrhantur.series;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.mrhantur.Unusuality;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentSeries {
    private final String id;
    private final String displayName;
    private final String description;
    private final List<Enchantment> enchantments;
    private final Material displayMaterial;
    private final int cost;
    private final double baseChance;
    private final Unusuality plugin;

    public EnchantmentSeries(Unusuality plugin, String id, String displayName, String description,
                             List<Enchantment> enchantments, Material displayMaterial,
                             int cost, double baseChance) {
        this.plugin = plugin;
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.enchantments = enchantments;
        this.displayMaterial = displayMaterial;
        this.cost = cost;
        this.baseChance = baseChance;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Enchantment> getEnchantments() {
        return enchantments;
    }

    public Material getDisplayMaterial() {
        return displayMaterial;
    }

    public int getCost() {
        return cost;
    }

    public double getBaseChance() {
        return baseChance;
    }

    public ItemStack createDisplayItem() {
        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(displayName);
        meta.setLore(List.of(
                "§7" + description,
                "§7Стоимость: §c" + cost + " ключ",
                "§7Зачарований в серии: §b" + enchantments.size(),
                "",
                "§eЛКМ: открыть кейс",
                "§eПКМ: показать описание"
        ));

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createInfoItem() {
        ItemStack item = new ItemStack(displayMaterial);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(displayName);
        List<String> lore = new ArrayList<>();
        lore.add("§7" + description);
        lore.add("");
        lore.add("§7Список зачарований:");

        // Получаем ручные названия из SeriesManager
        List<String> names = plugin.getSeriesManager().getEnchantmentNames(id);
        for (String name : names) {
            lore.add("§7- " + name);
        }

        lore.add("");
        lore.add("§eЛКМ: открыть кейс");
        lore.add("§eПКМ: скрыть описание");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public Enchantment getRandomEnchantment() {
        if (enchantments.isEmpty()) return null;
        return enchantments.get((int) (Math.random() * enchantments.size()));
    }
}