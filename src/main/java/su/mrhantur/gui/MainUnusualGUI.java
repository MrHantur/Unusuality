package su.mrhantur.gui;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import su.mrhantur.Unusuality;

import java.util.*;

public class MainUnusualGUI implements Listener {

    private final Map<UUID, Boolean> rollingPlayers = new HashMap<>();

    private final Unusuality plugin;

    public MainUnusualGUI(Unusuality plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§d★ Unusual Меню ★");

        // Шанс
        ItemStack chanceItem = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta chanceMeta = chanceItem.getItemMeta();
        double chance = plugin.getChance(player.getName().toLowerCase());
        chanceMeta.setDisplayName("§aВаш шанс: §e" + String.format(Locale.US, "%.2f", chance) + "%");
        chanceMeta.setLore(List.of("§7Шанс на выпадение зачарования необычного типа", "§8Обновляется автоматически"));
        chanceItem.setItemMeta(chanceMeta);
        inv.setItem(10, chanceItem);

        // Крутка
        ItemStack rollItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta rollMeta = rollItem.getItemMeta();
        rollMeta.setDisplayName("§6Открыть кейс");
        rollMeta.setLore(List.of("§7Ваш шанс на зачарование необычного типа", "§7указывает на шанс успеха открытия"));
        rollItem.setItemMeta(rollMeta);
        inv.setItem(12, rollItem);

        // Кейсы
        ItemStack caseItem = new ItemStack(Material.CHEST);
        ItemMeta caseMeta = caseItem.getItemMeta();
        caseMeta.setDisplayName("§bДругие кейсы (в разработке)");
        caseMeta.setLore(List.of("§7Предлагайте свои варианты!"));
        caseItem.setItemMeta(caseMeta);
        inv.setItem(14, caseItem);

        // В разработке
        ItemStack wipItem = new ItemStack(Material.BARRIER);
        ItemMeta wipMeta = wipItem.getItemMeta();
        wipMeta.setDisplayName("§cВ разработке");
        wipMeta.setLore(List.of("§7Эта функция появится позже"));
        wipItem.setItemMeta(wipMeta);
        inv.setItem(16, wipItem);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getView().getTitle().equals("§d★ Unusual Меню ★")) {
            e.setCancelled(true);

            ItemStack item = e.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) return;

            switch (item.getType()) {
                case EXPERIENCE_BOTTLE -> {
                    double chance = plugin.getChance(player.getName().toLowerCase());
                    player.sendMessage("§dТекущий шанс: " + String.format(Locale.US, "%.2f", chance) + "%");
                }

                case NETHER_STAR -> {
                    if (rollingPlayers.getOrDefault(player.getUniqueId(), false)) {
                        player.sendMessage("§cПодождите завершения текущей крутки.");
                        return;
                    }

                    double chance = plugin.getChance(player.getName().toLowerCase());
                    if (chance <= 0.0) {
                        player.sendMessage(ChatColor.RED + "У вас нет шанса на крутку.");
                        return;
                    }

                    plugin.setChance(player.getName().toLowerCase(), Math.max(0.0, chance - 100.0));
                    rollingPlayers.put(player.getUniqueId(), true);

                    Inventory rollInv = Bukkit.createInventory(null, 27, "§9Открываем кейс...");
                    player.openInventory(rollInv);

                    ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                    ItemMeta glassMeta = glass.getItemMeta();
                    glassMeta.setDisplayName(" ");
                    glass.setItemMeta(glassMeta);
                    for (int i = 0; i < rollInv.getSize(); i++) rollInv.setItem(i, glass);

                    List<ItemStack> pool = new ArrayList<>(Collections.nCopies(40, createDummyBook()));

                    int unusualCount = (int) Math.round(40 * (chance / 100.0));
                    Set<Integer> chosenIndices = new HashSet<>();

                    Random random = new Random();

                    final boolean success = Math.random() * 100.0 < chance;
                    final ItemStack reward = success
                            ? plugin.createUnusualBook(plugin.getRandomUnusualEnchantment())
                            : createDummyBook();


                    // Заполняем случайные уникальные индексы необычными книгами
                    while (chosenIndices.size() < unusualCount) {
                        int index = random.nextInt(40);
                        chosenIndices.add(index);
                    }

                    for (int idx : chosenIndices) {
                        Enchantment ench = plugin.getRandomUnusualEnchantment();
                        if (ench != null) {
                            pool.set(idx, plugin.createUnusualBook(ench));
                        } else {
                            pool.set(idx, createDummyBook());
                        }
                    }

                    // Вставляем финальный приз в центр для визуализации
                    int centerIndex = 4;
                    if (pool.size() > centerIndex) {
                        pool.set(centerIndex, reward);
                    } else {
                        while (pool.size() <= centerIndex) pool.add(createDummyBook());
                        pool.set(centerIndex, reward);
                    }


                    final int maxTicks = 40;

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

                            for (int i = 0; i < 9; i++) {
                                int index = (offset + i) % pool.size();
                                rollInv.setItem(9 + i, pool.get(index));
                            }

                            rollInv.setItem(4, createIndicator("§e▼"));
                            rollInv.setItem(22, createIndicator("§e▲"));

                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.0f + ticks * 0.01f);

                            ticks++;
                            offset++;

                            if (ticks >= maxTicks) {
                                ItemStack finalBook = pool.get((offset + centerIndex) % pool.size());
                                rollInv.setItem(13, finalBook);

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        player.closeInventory();
                                        if (success) {
                                            player.getInventory().addItem(finalBook);
                                            player.sendMessage("§d✨ Удача! Вы получили необычное зачарование!");
                                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                                            player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 0.6, 1.0, 0.6, 0.2);
                                        } else {
                                            player.sendMessage("§c💀 Неудача! В следующий раз повезёт больше.");
                                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.8f);
                                            player.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
                                        }
                                        rollingPlayers.remove(player.getUniqueId());
                                    }
                                }.runTaskLater(plugin, 30L);

                                cancel();
                                return;
                            }

                            long newDelay = 2L + (long) ((10.0 * ticks) / maxTicks);
                            new AnimationTask(ticks, offset, newDelay).runTaskLater(plugin, newDelay);

                            cancel();
                        }
                    }

                    new AnimationTask(0, 0, 0).runTask(plugin);
                }

                case CHEST -> {
                    player.sendMessage("§bКейсы временно недоступны.");
                }
                case BARRIER -> {
                    player.sendMessage("§7Функция в разработке.");
                }
            }
        } else if (e.getView().getTitle().equals("§9Открываем кейс...")) {
            e.setCancelled(true);
        }
    }

    private ItemStack createDummyBook() {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        meta.setDisplayName("§7Обычная книга");
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

}
