package su.mrhantur;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.registry.event.RegistryComposeEvent;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.java.JavaPlugin;

public final class UnusualityBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLogger().info("[UNUSUALITY_BOOTSTRAP] Unusuality bootstrap started.");

        context.getLifecycleManager().registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
                    // Серия #1
                    registerEnchantment(event, "fireflies", "§3♦ Светлячки");
                    registerEnchantment(event, "confetti", "§3♦ Конфетти");
                    registerEnchantment(event, "green_energy", "§3♦ Зелёная энергия");
                    registerEnchantment(event, "galaxy", "§3♦ Галактика");
                    registerEnchantment(event, "restless_souls", "§3♦ Беспокойные души");
                    registerEnchantment(event, "astral_step", "§3♦ Астральный шаг");

                    // Серия #2
                    registerEnchantment(event, "stormcloud", "§5♦ Штормовое облако");
                    registerEnchantment(event, "memory_leak", "§5♦ Утечка памяти");
                    registerEnchantment(event, "neutron_star", "§5♦ Нейтронная звезда");
                    registerEnchantment(event, "flaming_lantern", "§5♦ Пылающая лампа");
                    registerEnchantment(event, "bubbling", "§5♦ Пузырьки");
                    registerEnchantment(event, "orbiting_fire", "§5♦ Орбитальный огонёк");
                    registerEnchantment(event, "mountain_halo", "§5♦ Горный нимб");
                    registerEnchantment(event, "miami_nights", "§5♦ Майами");

                    // Серия #3
                    registerEnchantment(event, "carousel", "§6♦ Карусель");
                    registerEnchantment(event, "tornado", "§6♦ Смерч");
                    registerEnchantment(event, "rejection", "§6♦ Отторжение");
                    registerEnchantment(event, "stargazer", "§6♦ Звездочёт");
                    registerEnchantment(event, "devil_horns", "§6♦ Дьявольские рога");
                    registerEnchantment(event, "neon_electricity", "§6♦ Неоновое электричество");
                    registerEnchantment(event, "radiation", "§6♦ Радиация");
                    registerEnchantment(event, "blood_pact", "§6♦ Кровавый договор");

                    // Серия #4
                    registerEnchantment(event, "sputnik", "§1♦ Спутник");
                    registerEnchantment(event, "own_grave", "§1♦ Своя могила");
                    registerEnchantment(event, "silent_nights", "§1♦ Тихие ночи");
                    registerEnchantment(event, "rockets", "§1♦ Ракеты");
                    registerEnchantment(event, "sakura_trails", "§1♦ След сакуры");
                    registerEnchantment(event, "no_sound_no_memory", "§1♦ NO SOUND, NO MEMORY");
                })
        );
    }

    private void registerEnchantment(RegistryComposeEvent<Enchantment, EnchantmentRegistryEntry.Builder> event,
                                     String key, String description) {
        event.registry().register(
                EnchantmentKeys.create(Key.key("unusuality:" + key)),
                builder -> builder
                        .description(Component.text(description))
                        .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR))
                        .primaryItems(event.getOrCreateTag(ItemTypeTagKeys.ENCHANTABLE_HEAD_ARMOR))
                        .maxLevel(1)
                        .anvilCost(5)
                        .weight(10)
                        .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(1, 5))
                        .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(10, 10))
                        .activeSlots(EquipmentSlotGroup.ARMOR)
        );
    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        context.getLogger().info("[UNUSUALITY_BOOTSTRAP] Creating Unusuality instance.");
        return new Unusuality();
    }
}