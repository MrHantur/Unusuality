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
        setDescription("Выдаёт книгу с необычным зачарованием.");
        setPermission("unusuality.giveunusual");
        setAliases(List.of("unusualbook"));
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /giveunusual <игрок> <effect|random>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage("§cИгрок не найден.");
            return true;
        }

        Enchantment chosen;

        // Выбор зачарования: случайное или указанное
        if (args[1].equalsIgnoreCase("random")) {
            chosen = plugin.getRandomUnusualEnchantment();
            if (chosen == null) {
                sender.sendMessage("§cНет доступных необычных зачарований.");
                return true;
            }
        } else {
            NamespacedKey key = NamespacedKey.fromString("unusuality:" + args[1].toLowerCase());
            if (key == null) {
                sender.sendMessage("§cНеверный ключ зачарования.");
                return true;
            }

            Enchantment enchant = Enchantment.getByKey(key);
            if (enchant == null || !plugin.isUnusualEnchantment(enchant)) {
                sender.sendMessage("§cНеизвестное или неподдерживаемое зачарование: " + args[1]);
                return true;
            }

            chosen = enchant;
        }

        // Создаём и выдаём книгу
        ItemStack book = plugin.createUnusualBook(chosen);
        target.getInventory().addItem(book);

        // Показываем название зачарования
        String displayName = plugin.getEnchantmentDisplayName(chosen);
        target.sendMessage("§5Вы получили книгу с необычным зачарованием: " + displayName);
        sender.sendMessage("§aКнига с " + displayName + "§a выдана игроку §5" + target.getName());
        return true;
    }

    // Автодополнение аргументов
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