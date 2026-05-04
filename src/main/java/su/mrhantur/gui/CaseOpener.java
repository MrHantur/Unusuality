package su.mrhantur.gui;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import su.mrhantur.Unusuality;
import su.mrhantur.series.EnchantmentSeries;

import java.util.*;

public class CaseOpener implements Listener {

    private final Unusuality plugin;
    private final Map<UUID, Boolean> rollingPlayers = new HashMap<>();

    private int poolSize = 60;

    // Константы анимации
    private static final int ANIMATION_SLOTS = 9;
    private static final int INDICATOR_TOP = 4;
    private static final int INDICATOR_BOTTOM = 22;
    private static final long ANIMATION_BASE_DELAY = 2L;
    private static final long ANIMATION_CLOSE_DELAY = 40L;
    private static final int ANIMATION_MAX_TICKS = 40;
    private static final int REWARD_OFFSET_BEFORE_END = 5;

    // Холдер для инвентаря анимации
    private static class CaseInventoryHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    // Холдер для меню предложения открыть ещё кейс (не статический, хранит серию)
    private class OfferCaseHolder implements InventoryHolder {
        final EnchantmentSeries series;

        OfferCaseHolder(EnchantmentSeries series) {
            this.series = series;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public CaseOpener(Unusuality plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Блокировка инвентаря анимации
        if (holder instanceof CaseInventoryHolder) {
            event.setCancelled(true);
            return;
        }

        // Меню предложения открыть ещё кейс
        if (holder instanceof OfferCaseHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            OfferCaseHolder offerHolder = (OfferCaseHolder) holder;
            EnchantmentSeries series = offerHolder.series;

            if (clicked.getType() == series.getDisplayMaterial()) {
                // Открыть тот же кейс
                player.closeInventory();
                openCase(player, series);
            } else if (clicked.getType() == Material.ENDER_CHEST) {
                // Открыть выбор кейса
                player.closeInventory();
                plugin.getCaseSelectionGUI().open(player);
            } else if (clicked.getType() == Material.BARRIER) {
                // Закрыть
                player.closeInventory();
            }
            return;
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CaseInventoryHolder) {
            event.setCancelled(true);
        }
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
            player.sendMessage("§cОшибка: выбранная серия не найдена!");
            return;
        }

        // Проверка наличия физического ключа
        ItemStack physicalKey = findPhysicalKey(player);
        boolean hasPhysicalKey = physicalKey != null;

        String playerName = player.getName().toLowerCase();
        int playerKeys = plugin.getDataManager().getKeys(playerName);
        double playerProgress = plugin.getDataManager().getProgress(playerName);
        double totalProgress = playerKeys + playerProgress;
        double cost = series.getCost();

        // Списываем стоимость
        if (hasPhysicalKey && cost == 1) {
            if (physicalKey.getAmount() > 1) {
                physicalKey.setAmount(physicalKey.getAmount() - 1);
            } else {
                player.getInventory().remove(physicalKey);
            }
        } else {
            if (totalProgress < cost) {
                player.sendMessage("§cНедостаточно ключей для открытия этого кейса!");
                player.sendMessage("§7Требуется: " + cost + " ключ(ей)");
                player.sendMessage("§7У вас: " + String.format("%.2f", totalProgress) + " ключ(ей)");
                return;
            }
            plugin.getDataManager().removeProgress(playerName, cost);
        }

        // Определяем награду (один вызов рандома)
        Enchantment randomEnchant = series.getRandomEnchantment();
        boolean success = Math.random() * 100.0 < series.getBaseChance();
        ItemStack reward = success
                ? createUnusualBook(randomEnchant)
                : createDummyBook();

        // Копия для анимации без метаданных игрока
        ItemStack animationReward = reward.clone();

        // Создаём инвентарь анимации
        Inventory rollInv = Bukkit.createInventory(new CaseInventoryHolder(), 27,
                "§9Открываем " + series.getDisplayName());
        ItemStack glass = createGlassPane();
        for (int i = 0; i < rollInv.getSize(); i++) {
            rollInv.setItem(i, glass);
        }

        List<ItemStack> pool = createAnimationPool(series);
        int rewardPos = Math.max(new Random().nextInt(poolSize - 10), 30);
        pool.set(rewardPos, animationReward);

        // Добавляем в реальную награду информацию о получившем игроке
        ItemMeta rewardMeta = reward.getItemMeta();
        List<String> lore = rewardMeta.getLore() != null
                ? new ArrayList<>(rewardMeta.getLore())
                : new ArrayList<>();
        lore.add("§7Получено игроком §e" + player.getName());
        rewardMeta.setLore(lore);
        reward.setItemMeta(rewardMeta);

        player.openInventory(rollInv);
        rollingPlayers.put(player.getUniqueId(), true);

        new AnimationTask(player, rollInv, pool, rewardPos, success, series, reward, randomEnchant).start();
    }

    // Ищет первый физический ключ в инвентаре игрока
    private ItemStack findPhysicalKey(Player player) {
        for (ItemStack item : player.getInventory()) {
            if (plugin.getKeyConverter().isPhysicalKey(item)) {
                return item;
            }
        }
        return null;
    }

    private ItemStack createGlassPane() {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        glass.setItemMeta(meta);
        return glass;
    }

    private List<ItemStack> createAnimationPool(EnchantmentSeries series) {
        List<ItemStack> pool = new ArrayList<>();
        int goodCount = (int) (poolSize * 0.6);
        int badCount = poolSize - goodCount;

        for (int i = 0; i < goodCount; i++) {
            pool.add(createUnusualBook(series.getRandomEnchantment()));
        }
        for (int i = 0; i < badCount; i++) {
            pool.add(createDummyBook());
        }

        Collections.shuffle(pool);
        return pool;
    }

    private void handleCaseResult(Player player, boolean success, ItemStack reward,
                                  EnchantmentSeries series, Enchantment enchantment) {
        String playerName = player.getName().toLowerCase();

        // Если настройка включена и остались ключи — показываем меню предложения
        if (plugin.getDataManager().getOfferAnotherCase(playerName)) {
            double total = plugin.getDataManager().getKeys(playerName)
                    + plugin.getDataManager().getProgress(playerName);
            if (total >= 1.0) {
                offerAnotherCase(player, series);
            }
        }

        // Оповещение о джекпоте
        if (success) {
            String enchantName = enchantment != null
                    ? plugin.getEnchantmentDisplayName(enchantment)
                    : "Неизвестное зачарование";

            if (plugin.getDataManager().getAnnounceJackpot(playerName)) {
                Bukkit.broadcastMessage("§d⚡ " + player.getName() + " получает " + enchantName + "!");
            } else {
                player.sendMessage("§d✨ Джекпот! Вы получили " + enchantName + "!");
            }

            // Выдача предмета
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(reward);
            if (!leftover.isEmpty()) {
                leftover.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
                player.sendMessage("§eИнвентарь полон! Предметы упали на землю.");
            }
            
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.spawnParticle(Particle.TOTEM_OF_UNDYING,
                    player.getLocation().add(0, 1, 0), 50, 0.6, 1.0, 0.6, 0.2);
        } else {
            player.sendMessage("§c💀 В следующий раз повезёт больше.");
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.8f);
            player.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE,
                    player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
        }
    }

    // Показывает меню с предложением открыть ещё один кейс (два варианта)
    private void offerAnotherCase(Player player, EnchantmentSeries series) {
        Inventory inv = Bukkit.createInventory(new OfferCaseHolder(series), 9,
                "§aОткрыть ещё один кейс?");

        ItemStack glass = createGlassPane();
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
        }

        // Кнопка «Открыть ещё этот кейс» (материал серии)
        ItemStack sameCase = new ItemStack(series.getDisplayMaterial());
        ItemMeta sameMeta = sameCase.getItemMeta();
        sameMeta.setDisplayName("§aОткрыть ещё этот кейс");
        sameMeta.setLore(List.of("§7Попытать удачу снова!"));
        sameCase.setItemMeta(sameMeta);
        inv.setItem(3, sameCase);

        // Кнопка «Выбрать другой кейс»
        ItemStack chooseAnother = new ItemStack(Material.ENDER_CHEST);
        ItemMeta chooseMeta = chooseAnother.getItemMeta();
        chooseMeta.setDisplayName("§eВыбрать другой кейс");
        chooseMeta.setLore(List.of("§7Перейти к выбору кейса"));
        chooseAnother.setItemMeta(chooseMeta);
        inv.setItem(5, chooseAnother);

        // Закрыть
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.setDisplayName("§cЗакрыть");
        close.setItemMeta(closeMeta);
        inv.setItem(8, close);

        player.openInventory(inv);
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
        ItemStack item = new ItemStack(Material.IRON_CHAIN);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(symbol);
        item.setItemMeta(meta);
        return item;
    }

    // ── Внутренний класс анимации ──────────────────────────────────────────
    private class AnimationTask {
        private final Player player;
        private final Inventory inventory;
        private final List<ItemStack> pool;
        private final int rewardPos;
        private final boolean success;
        private final EnchantmentSeries series;
        private final ItemStack reward;
        private final Enchantment enchantment;

        private int ticks;
        private int offset;

        AnimationTask(Player player, Inventory inventory, List<ItemStack> pool, int rewardPos,
                      boolean success, EnchantmentSeries series, ItemStack reward, Enchantment enchantment) {
            this.player = player;
            this.inventory = inventory;
            this.pool = pool;
            this.rewardPos = rewardPos;
            this.success = success;
            this.series = series;
            this.reward = reward;
            this.enchantment = enchantment;
            this.ticks = 0;
            this.offset = 0;
        }

        void start() {
            runTask();
        }

        void runTask() {
            if (!player.isOnline()) {
                rollingPlayers.remove(player.getUniqueId());
                return;
            }

            for (int i = 0; i < ANIMATION_SLOTS; i++) {
                int index = (offset + i) % pool.size();
                inventory.setItem(9 + i, pool.get(index));
            }

            inventory.setItem(INDICATOR_TOP, createIndicator("§e▼"));
            inventory.setItem(INDICATOR_BOTTOM, createIndicator("§e▲"));

            float pitch = 1.0f + ticks * 0.02f;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, pitch);

            if (ticks > rewardPos - REWARD_OFFSET_BEFORE_END) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.closeInventory();
                        handleCaseResult(player, success, reward, series, enchantment);
                        rollingPlayers.remove(player.getUniqueId());
                    }
                }.runTaskLater(plugin, ANIMATION_CLOSE_DELAY);
                return;
            }

            ticks++;
            offset++;
            long delay = ANIMATION_BASE_DELAY + (long) ((8.0 * ticks) / ANIMATION_MAX_TICKS);
            new BukkitRunnable() {
                @Override
                public void run() {
                    AnimationTask.this.runTask();
                }
            }.runTaskLater(plugin, delay);
        }
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}