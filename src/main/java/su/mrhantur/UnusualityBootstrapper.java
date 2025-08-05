package su.mrhantur;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.registry.event.RegistryEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.List;

public final class UnusualityBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLogger().info("[UNUSUALITY_BOOTSTRAP] Unusuality bootstrap started.");

        context.getLifecycleManager().registerEventHandler(
                RegistryEvents.ENCHANTMENT.freeze().newHandler(event -> {
                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:fireflies")),
                            builder -> builder
                                    .description(Component.text("§3♦ Светлячки"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)

                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:confetti")),
                            builder -> builder
                                    .description(Component.text("§3♦ Конфетти"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:green_energy")),
                            builder -> builder
                                    .description(Component.text("§3♦ Зелёная энергия"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:galaxy")),
                            builder -> builder
                                    .description(Component.text("§3♦ Галактика"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:restless_souls")),
                            builder -> builder
                                    .description(Component.text("§3♦ Беспокойные души"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:astral_step")),
                            builder -> builder
                                    .description(Component.text("§3♦ Астральный шаг"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    // Серия #2

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:stormcloud")),
                            builder -> builder
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Штормовое облако"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:memory_leak")),
                            builder -> builder
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Утечка памяти"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:neutron_star")),
                            builder -> builder
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Нейтронная звезда"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:flaming_lantern")),
                            builder -> builder
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Пылающая лампа"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:bubbling")),
                            builder -> builder
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Пузырьки"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:orbiting_fire")),
                            builder -> builder
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Орбитальный огонёк"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:mountain_halo")),
                            builder -> builder
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Горный нимб"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:miami_nights")),
                            builder -> builder
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Майами"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    // Серия #3

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:carousel")),
                            builder -> builder
                                    .description(Component.text("§6♦ Карусель"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:tornado")),
                            builder -> builder
                                    .description(Component.text("§6♦ Смерч"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:rejection")),
                            builder -> builder
                                    .description(Component.text("§6♦ Отторжение"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:stargazer")),
                            builder -> builder
                                    .description(Component.text("§6♦ Звездочёт"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:devil_horns")),
                            builder -> builder
                                    .description(Component.text("§6♦ Дьяволиские рога"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:neon_electricity")),
                            builder -> builder
                                    .description(Component.text("§6♦ Неоновое электричество"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:radiation")),
                            builder -> builder
                                    .description(Component.text("§6♦ Радиация"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );

                    event.registry().register(
                            EnchantmentKeys.create(NamespacedKey.fromString("unusuality:blood_pact")),
                            builder -> builder
                                    .description(Component.text("§6♦ Кровавый договор"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HEAD_ARMOR))
                                    .maxLevel(1)
                                    .anvilCost(5)
                                    .weight(10)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                                    .activeSlots(EquipmentSlotGroup.ARMOR)
                    );
                })
        );
    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        context.getLogger().info("[UNUSUALITY_BOOTSTRAP] Creating Unusuality instance.");
        return new Unusuality();
    }
}
