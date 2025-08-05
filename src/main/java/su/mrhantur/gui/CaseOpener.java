package su.mrhantur.gui;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import su.mrhantur.Unusuality;
import su.mrhantur.series.EnchantmentSeries;

import java.util.*;

import static java.lang.Math.max;

public class CaseOpener {
    private final Unusuality plugin;
    private final Map<UUID, Boolean> rollingPlayers = new HashMap<>();

    private int poolSize = 60;

    public CaseOpener(Unusuality plugin) {
        this.plugin = plugin;
    }

    public boolean isPlayerRolling(Player player) {
        return rollingPlayers.getOrDefault(player.getUniqueId(), false);
    }

    public void openCase(Player player, EnchantmentSeries series) {
        if (isPlayerRolling(player)) {
            player.sendMessage("§cПодождите завершения текущей крутки.");
            return;
        }

        if (series == null) {
            player.sendMessage(ChatColor.RED + "Ошибка: выбранная серия не найдена!");
            return;
        }

        // Проверяем наличие физического ключа
        boolean hasPhysicalKey = false;
        ItemStack physicalKey = null;
        for (ItemStack item : player.getInventory()) {
            if (plugin.getKeyConverter().isPhysicalKey(item)) {
                hasPhysicalKey = true;
                physicalKey = item;
                break;
            }
        }

        String playerName = player.getName().toLowerCase();
        int playerKeys = plugin.getDataManager().getKeys(playerName);
        double playerProgress = plugin.getDataManager().getProgress(playerName);
        double totalProgress = playerKeys + playerProgress;

        double cost = series.getCost();

        // Если есть физический ключ и кейс стоит 1 ключ
        if (hasPhysicalKey && cost == 1) {
            // Удаляем один физический ключ
            if (physicalKey.getAmount() > 1) {
                physicalKey.setAmount(physicalKey.getAmount() - 1);
            } else {
                player.getInventory().remove(physicalKey);
            }
        } else {
            // Проверяем достаточно ли цифровых ключей
            if (totalProgress < cost) {
                player.sendMessage(ChatColor.RED + "Недостаточно ключей для открытия этого кейса!");
                player.sendMessage(ChatColor.GRAY + "Требуется: " + cost + " ключ(ей)");
                player.sendMessage(ChatColor.GRAY + "У вас: " + String.format("%.2f", totalProgress) + " ключ(ей)");
                return;
            }
            plugin.getDataManager().removeProgress(playerName, cost);
        }

        rollingPlayers.put(player.getUniqueId(), true);

        boolean success = Math.random() * 100.0 < series.getBaseChance();
        ItemStack reward = success
                ? createUnusualBook(series.getRandomEnchantment())
                : createDummyBook();

        // Создаем КОПИЮ награды для анимации (без информации об игроке)
        ItemStack animationReward = reward.clone();

        Inventory rollInv = Bukkit.createInventory(null, 27, "§9Открываем " + series.getDisplayName());
        player.openInventory(rollInv);

        ItemStack glass = createGlassPane();
        for (int i = 0; i < rollInv.getSize(); i++) {
            rollInv.setItem(i, glass);
        }

        List<ItemStack> pool = createAnimationPool(series);

        // Вставляем в пул КОПИЮ без меты игрока
        int rewardPosition = max(new Random().nextInt(poolSize - 10), 30);
        pool.set(rewardPosition, animationReward); // Используем копию

        // Добавляем информацию об игроке ТОЛЬКО к выдаваемой награде
        ItemMeta rewardMeta = reward.getItemMeta();
        List<String> lore = rewardMeta.getLore() != null ?
                new ArrayList<>(rewardMeta.getLore()) :
                new ArrayList<>();

        lore.add("§7Получено игроком §e" + player.getName());
        rewardMeta.setLore(lore);
        reward.setItemMeta(rewardMeta);

        // Передаем оригинальную награду (с метой) для выдачи
        startCaseAnimation(player, rollInv, pool, rewardPosition, success, series, reward);
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        return glass;
    }

    private List<ItemStack> createAnimationPool(EnchantmentSeries series) {
        List<ItemStack> pool = new ArrayList<>();

        // Add enchantments from series (60%)
        for (int i = 0; i < poolSize * 0.6; i++) {
            Enchantment enchant = series.getRandomEnchantment();
            pool.add(createUnusualBook(enchant));
        }

        // Add dummy books (40%)
        for (int i = 0; i < poolSize * 0.4; i++) {
            pool.add(createDummyBook());
        }

        Collections.shuffle(pool);
        return pool;
    }

    private void startCaseAnimation(Player player, Inventory rollInv, List<ItemStack> pool,
                                    int rewardPosition, boolean success, EnchantmentSeries series, ItemStack reward) {
        final int maxTicks = 40;
        final int centerIndex = 4; // Center slot in the middle row (index 4 of 0-8)

        class AnimationTask extends BukkitRunnable {
            int ticks;
            int offset;
            long delay;

            AnimationTask(int ticks, int offset, long delay) {
                this.ticks = ticks;
                this.offset = offset;
                this.delay = delay;
            }

            @Override
            public void run() {
                if (!player.isOnline()) {
                    rollingPlayers.remove(player.getUniqueId());
                    cancel();
                    return;
                }

                // Display current items in the middle row
                for (int i = 0; i < 9; i++) {
                    int index = (offset + i) % poolSize;
                    rollInv.setItem(9 + i, pool.get(index));
                }

                // Indicators
                rollInv.setItem(4, createIndicator("§e▼")); // Top indicator
                rollInv.setItem(22, createIndicator("§e▲")); // Bottom indicator

                // Sound effect
                float pitch = 1.0f + (float) ticks * 0.02f;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, pitch);

                // End of animation
                if (ticks > rewardPosition - 5) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.closeInventory();
                            handleCaseResult(player, success, reward, series);
                            rollingPlayers.remove(player.getUniqueId());
                        }
                    }.runTaskLater(plugin, 40L);

                    cancel();
                    return;
                }

                // Next animation frame
                ticks++;
                offset++;
                long newDelay = 2L + (long) ((8.0 * ticks) / maxTicks);
                new AnimationTask(ticks, offset, newDelay).runTaskLater(plugin, newDelay);
                cancel();
            }
        }

        new AnimationTask(0, 0, 0).runTask(plugin);
    }

    private void handleCaseResult(Player player, boolean success, ItemStack reward, EnchantmentSeries series) {
        if (success) {
            // Пытаемся добавить награду в инвентарь
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(reward);

            // Если не поместилось полностью - дропаем остаток
            if (!leftover.isEmpty()) {
                for (ItemStack item : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), item);
                }
                player.sendMessage("§eПредметы упали на землю, так как инвентарь полон!");
            }

            player.sendMessage("§d✨ Удача! Вы получили зачарование из серии " + series.getDisplayName() + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 0.6, 1.0, 0.6, 0.2);
        } else {
            player.sendMessage("§c💀 Неудача! В следующий раз повезёт больше");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.8f);
            player.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
        }
    }

    private ItemStack createDummyBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName("§7Обычная книга");
        meta.setLore(List.of("§7Ничего особенного..."));
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack createUnusualBook(Enchantment enchantment) {
        if (enchantment == null) return createDummyBook();

        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(enchantment, 1, true);
        book.setItemMeta(meta);
        return book;
    }

    private ItemStack createIndicator(String symbol) {
        ItemStack item = new ItemStack(Material.CHAIN);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(symbol);
        item.setItemMeta(meta);
        return item;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}