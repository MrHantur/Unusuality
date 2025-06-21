package su.mrhantur.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import su.mrhantur.Unusuality;

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
            double chance = plugin.getChance(player.getName().toLowerCase());
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Ваш шанс на зачарование необычного типа: " + String.format(Locale.US, "%.2f", chance) + "%");
            return true;
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
        if (args.length == 1)
            return List.of("set", "add", "remove").stream()
                    .filter(opt -> opt.startsWith(args[0].toLowerCase())).toList();

        if (args.length == 2)
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();

        return Collections.emptyList();
    }
}
