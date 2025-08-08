package su.mrhantur.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExchangeOffer {
    private static final Map<Material, String> MATERIAL_NAMES = new HashMap<>();
    static {
        MATERIAL_NAMES.put(Material.DIAMOND, "Алмаз");
        MATERIAL_NAMES.put(Material.EMERALD, "Изумруд");
        MATERIAL_NAMES.put(Material.ECHO_SHARD, "Осколок эха");
        MATERIAL_NAMES.put(Material.NETHER_STAR, "Звезда Незера");
        MATERIAL_NAMES.put(Material.TOTEM_OF_UNDYING, "Тотем бессмертия");
        MATERIAL_NAMES.put(Material.ANCIENT_DEBRIS, "Древние обломки");
        MATERIAL_NAMES.put(Material.NETHERITE_INGOT, "Незеритовый слиток");
        MATERIAL_NAMES.put(Material.DIAMOND_BLOCK, "Блок алмазов");
        MATERIAL_NAMES.put(Material.EMERALD_BLOCK, "Блок изумрудов");
        MATERIAL_NAMES.put(Material.GOLD_BLOCK, "Блок золота");
        MATERIAL_NAMES.put(Material.IRON_BLOCK, "Блок железа");
        MATERIAL_NAMES.put(Material.COPPER_INGOT, "Медный слиток");
        MATERIAL_NAMES.put(Material.RAW_GOLD, "Сырое золото");
        MATERIAL_NAMES.put(Material.RAW_IRON, "Сырое железо");
        MATERIAL_NAMES.put(Material.ELYTRA, "Элитры");
    }

    private final Material material;
    private final int amount;
    private final int keys;

    public ExchangeOffer(Material material, int amount, int keys) {
        this.material = material;
        this.amount = amount;
        this.keys = keys;
    }

    public ItemStack createDisplayItem() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        String materialName = getMaterialName();
        meta.setDisplayName("§eОбмен: " + amount + " " + materialName);

        List<String> lore = new ArrayList<>();
        lore.add("§7Получите: §6" + keys + " ключ" + getKeySuffix(keys));
        lore.add("");
        lore.add("§eЛКМ: §7обменять");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String getMaterialName() {
        return MATERIAL_NAMES.getOrDefault(material,
                material.name().toLowerCase().replace("_", " "));
    }

    private String getKeySuffix(int keys) {
        if (keys % 10 == 1 && keys % 100 != 11) return "";
        if (keys % 10 >= 2 && keys % 10 <= 4 &&
                (keys % 100 < 10 || keys % 100 >= 20)) return "а";
        return "ей";
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public int getKeys() {
        return keys;
    }

    public String getMaterialName(Material material) {
        return MATERIAL_NAMES.getOrDefault(material,
                material.name().toLowerCase().replace("_", " "));
    }
}