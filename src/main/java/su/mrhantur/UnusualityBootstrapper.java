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
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Светлячки"))
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
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Конфетти"))
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
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Зелёная энергия"))
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
                                    .description(Component.text(ChatColor.DARK_PURPLE + "♦ Галактика"))
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
