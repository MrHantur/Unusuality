package su.mrhantur.commands;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.mrhantur.Unusuality;

import java.util.*;

public class GiveUnusual extends Command {

    private final Unusuality plugin;

    public GiveUnusual(Unusuality plugin) {
        super("giveunusual");
        this.plugin = plugin;
        setDescription("Gives a book with an Unusual enchantment to a player.");
        setPermission("unusuality.giveunusual");
        setAliases(List.of("unusualbook"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /giveunusual <player> <effect|random>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        Enchantment chosen;

        if (args[1].equalsIgnoreCase("random")) {
            chosen = plugin.getRandomUnusualEnchantment();
            if (chosen == null) {
                sender.sendMessage(ChatColor.RED + "No unusual enchantments available.");
                return true;
            }
        } else {
            NamespacedKey key = NamespacedKey.fromString("unusuality:" + args[1].toLowerCase());
            if (key == null) {
                sender.sendMessage(ChatColor.RED + "Invalid enchantment key.");
                return true;
            }

            Enchantment enchant = Enchantment.getByKey(key);
            if (enchant == null || !plugin.isUnusualEnchantment(enchant)) {
                sender.sendMessage(ChatColor.RED + "Unknown or unsupported enchantment: " + args[1]);
                return true;
            }

            chosen = enchant;
        }

        ItemStack book = plugin.createUnusualBook(chosen);
        target.getInventory().addItem(book);

        target.sendMessage(ChatColor.LIGHT_PURPLE + "Вы получили зачарование необычного типа!");

        sender.sendMessage(ChatColor.GREEN + "Book with " + chosen.getKey().getKey() + " sent to " + target.getName() + "!");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            List<String> keys = plugin.getUnusualEnchantments().stream()
                    .map(e -> e.getKey().getKey())
                    .toList();

            List<String> options = new ArrayList<>(keys);
            options.add("random");

            return options.stream()
                    .filter(opt -> opt.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}
