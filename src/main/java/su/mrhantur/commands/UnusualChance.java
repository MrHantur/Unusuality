package su.mrhantur.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import su.mrhantur.Unusuality;
import su.mrhantur.gui.MainUnusualGUI;

import java.util.*;

public class UnusualChance extends Command {

    private final Unusuality plugin;

    public UnusualChance(Unusuality plugin) {
        this(plugin, "unusualchance");
    }

    public UnusualChance(Unusuality plugin, String name) {
        super(name);
        this.plugin = plugin;
        setDescription("Manage player's unusual chance");
        setPermission("unusuality.chance");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length == 0 && sender instanceof Player player) {
            plugin.getMainUnusualGUI().open(player);
            return true;

            // double chance = plugin.getChance(player.getName().toLowerCase());
            // sender.sendMessage(ChatColor.LIGHT_PURPLE + "Ваш шанс на зачарование необычного типа: " + String.format(Locale.US, "%.2f", chance) + "%");
            // return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("gamble") && sender instanceof Player player) {
            String name = player.getName().toLowerCase();
            double chance = plugin.getChance(name);

            if (chance <= 0.0) {
                player.sendMessage(ChatColor.RED + "У вас нет шанса на получение необычного зачарования.");
                return true;
            }

            double current = plugin.getChance(name);
            double newValue = Math.max(0.0, current - 100.0);
            plugin.setChance(name, newValue);

            plugin.setChance(name, newValue); // шанс обнуляем сразу

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks == 0) {
                        player.sendMessage(ChatColor.GRAY + "🎰 Крутка началась...");
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                    }

                    if (ticks > 0 && ticks <= 20) {
                        player.spawnParticle(Particle.CRIT, player.getLocation().add(0, 2, 0), 5, 0.2, 0.2, 0.2, 0.01);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.0f + ticks * 0.05f);
                    }

                    if (ticks == 30) {
                        boolean success = Math.random() * 100.0 < chance;

                        if (success) {
                            Enchantment enchant = plugin.getRandomUnusualEnchantment();
                            if (enchant == null) {
                                player.sendMessage(ChatColor.RED + "Ошибка: нет доступных необычных зачарований.");
                                cancel();
                                return;
                            }

                            ItemStack book = plugin.createUnusualBook(enchant);
                            player.getInventory().addItem(book);

                            player.sendMessage(ChatColor.LIGHT_PURPLE + "✨ Удача! Вы получили необычное зачарование!");
                            player.sendMessage(ChatColor.GRAY + "Ваш шанс сброшен до " + newValue + "%");
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            player.spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 0.6, 1.0, 0.6, 0.2);
                        } else {
                            player.sendMessage(ChatColor.RED + "💀 Неудача! Попробуйте в следующий раз.");
                            player.sendMessage(ChatColor.GRAY + "Ваш шанс сброшен до " + newValue + "%");
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.8f, 0.8f);
                            player.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.05);
                        }

                        cancel();
                    }

                    ticks++;
                }
            }.runTaskTimer(plugin, 0L, 2L); // каждые 2 тика (0.1 сек)
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("chance") && sender instanceof Player player) {
            double chance = plugin.getChance(player.getName().toLowerCase());
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Ваш шанс на зачарование необычного типа: " + String.format(Locale.US, "%.2f", chance) + "%");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            String target = args[1].toLowerCase();

            if (target.equals("all")) {
                if (!sender.hasPermission("unusuality.chance.admin")) {
                    sender.sendMessage(ChatColor.RED + "У вас нет прав для этой команды.");
                    return true;
                }

                Map<String, Double> allChances = plugin.getAllChances();
                if (allChances.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "Нет сохранённых шансов.");
                    return true;
                }

                sender.sendMessage(ChatColor.LIGHT_PURPLE + "📊 Шансы всех игроков:");
                allChances.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .forEach(entry -> sender.sendMessage(ChatColor.GRAY + "- " + entry.getKey() + ": " +
                                String.format(Locale.US, "%.2f", entry.getValue()) + "%"));
                return true;
            } else {
                double value = plugin.getChance(target);
                sender.sendMessage(ChatColor.LIGHT_PURPLE + "🎯 Шанс игрока " + target + ": " +
                        String.format(Locale.US, "%.2f", value) + "%");
                return true;
            }
        }

        if (args.length == 3 && sender.hasPermission("unusuality.chance.admin")) {
            String action = args[0].toLowerCase();
            String target = args[1].toLowerCase();
            double value;

            try {
                value = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number: " + args[2]);
                return true;
            }

            switch (action) {
                case "set" -> plugin.setChance(target, value);
                case "add" -> plugin.addChance(target, value);
                case "remove" -> plugin.removeChance(target, value);
                default -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /unusualchance [set|add|remove] <player> <value>");
                    return true;
                }
            }

            sender.sendMessage(ChatColor.GREEN + "Chance for " + target + " is now " + String.format("%.2f", plugin.getChance(target)) + "%");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /unusualchance OR /unusualchance [set|add|remove] <player> <value>");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();

            if (sender.hasPermission("unusuality.chance.admin")) {
                options.addAll(List.of("set", "add", "remove", "get"));
            }

            if (sender instanceof Player) {
                options.addAll(List.of("gamble", "chance"));
            }

            return options.stream()
                    .filter(opt -> opt.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("get") || (args[0].equalsIgnoreCase("add")) || (args[0].equalsIgnoreCase("remove")))) {
            List<String> names = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList();

            if (sender.hasPermission("unusuality.chance.admin")) {
                names = new ArrayList<>(names);
                names.add("all");
            }

            return names.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }

}
