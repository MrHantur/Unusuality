package su.mrhantur.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import su.mrhantur.Unusuality;
import su.mrhantur.UnusualityDataManager;

import java.util.*;

public class UnusualChance extends Command {

    private final Unusuality plugin;
    private final UnusualityDataManager data;

    public UnusualChance(Unusuality plugin) {
        this(plugin, "unusualchance");
    }

    public UnusualChance(Unusuality plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.data = plugin.getPlayerData();
        setDescription("Manage player's unusual chance and effect visibility");
        setPermission("unusuality.chance");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        // GUI
        if (args.length == 0 && sender instanceof Player player) {
            plugin.getMainUnusualGUI().open(player);
            return true;
        }

        // Настройки
        if (args.length >= 1 && sender instanceof Player player) {
            String subCommand = args[0].toLowerCase();

            // Команды для управления видимостью эффектов
            if (subCommand.equals("seemine")) {
                return handleToggleCommand(player, "showEffectPlayer", args, "Показ ваших эффектов");
            }
            else if (subCommand.equals("seeother")) {
                return handleToggleCommand(player, "showAllEffects", args, "Показ чужих эффектов");
            }
            else if (subCommand.equals("showmine")) {
                return handleToggleCommand(player, "canSeeMyEffect", args, "Видимость ваших эффектов для других");
            }
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("info") && sender instanceof Player player) {
            String playerName = player.getName();
            sendVisibilityInfo(player, playerName.toLowerCase());
            return true;
        }

        // Legacy
        if (args.length == 1 && args[0].equalsIgnoreCase("gamble") && sender instanceof Player player) {
            sender.sendMessage(ChatColor.RED + "Эта функция устарела. Используйте открытие кейса в /uc");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("chance") && sender instanceof Player player) {
            sender.sendMessage(ChatColor.RED + "Эта функция устарела. Используйте открытие кейса в /uc");
            return true;
        }

        // get all
        if (args.length == 2 && args[0].equalsIgnoreCase("get") && args[1].equalsIgnoreCase("all")) {
            Map<String, Integer> allKeys = data.getAllKeys();
            Map<String, Double> allProgress = data.getAllProgress();

            sender.sendMessage(ChatColor.LIGHT_PURPLE + "📊 Шансы всех игроков:");

            allKeys.keySet().stream()
                    .sorted(Comparator.comparingInt((String name) -> allKeys.getOrDefault(name, 0)).reversed())
                    .forEach(name -> {
                        int keys = allKeys.getOrDefault(name, 0);
                        double progress = allProgress.getOrDefault(name, 0.0);
                        double chance = keys * 100.0 + progress * 100.0;
                        sender.sendMessage(ChatColor.GRAY + "- " + name + ": " + keys + " ключ(ей), прогресс " + String.format(Locale.US, "%.2f", progress) + " (" + String.format(Locale.US, "%.2f", chance) + "%)");
                    });

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
                case "set" -> {
                    int keys = (int) (value / 100.0);
                    double progress = (value % 100.0) / 100.0;
                    data.setKeys(target, keys);
                    data.setProgress(target, progress);
                }
                case "add" -> data.addProgress(target, value / 100.0);
                case "remove" -> data.removeProgress(target, value / 100.0);
                default -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /unusualchance [set|add|remove] <player> <value>");
                    return true;
                }
            }

            double chance = data.getKeys(target) * 100.0 + data.getProgress(target) * 100.0;
            sender.sendMessage(ChatColor.GREEN + "Chance for " + target + " is now " + String.format(Locale.US, "%.2f", chance) + "%");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /unusualchance OR /unusualchance [set|add|remove|get all] <player> <value>");
        return true;
    }

    private boolean handleToggleCommand(Player player, String setting, String[] args, String settingName) {
        if (args.length == 1) {
            // Переключение состояния
            boolean current = getSettingValue(player.getName(), setting);
            setSettingValue(player.getName(), setting, !current);
            player.sendMessage(ChatColor.GREEN + settingName + " " +
                    (current ? ChatColor.RED + "выключены" : ChatColor.GREEN + "включены"));
            return true;
        }
        else if (args.length == 2) {
            // Установка конкретного значения
            String value = args[1].toLowerCase();
            if (value.equals("on") || value.equals("вкл")) {
                setSettingValue(player.getName(), setting, true);
                player.sendMessage(ChatColor.GREEN + settingName + " включены");
                return true;
            }
            else if (value.equals("off") || value.equals("выкл")) {
                setSettingValue(player.getName(), setting, false);
                player.sendMessage(ChatColor.RED + settingName + " выключены");
                return true;
            }
        }

        player.sendMessage(ChatColor.RED + "Использование: /" + getName() + " " + args[0] + " [on/off]");
        return true;
    }

    // Новый метод для отправки информации о настройках
    private void sendVisibilityInfo(Player player, String playerName) {
        player.sendMessage(ChatColor.GOLD + "--- Настройки видимости эффектов ---");
        player.sendMessage(formatSetting("Показ ваших эффектов", data.getShowEffectPlayer(playerName)));
        player.sendMessage(formatSetting("Показ чужих эффектов", data.getShowAllEffects(playerName)));
        player.sendMessage(formatSetting("Другие видят ваши эффекты", data.getCanSeeMyEffect(playerName)));
    }

    private String formatSetting(String name, boolean value) {
        return ChatColor.GRAY + "- " + name + ": " +
                (value ? ChatColor.GREEN + "ВКЛ" : ChatColor.RED + "ВЫКЛ");
    }

    private boolean getSettingValue(String player, String setting) {
        switch (setting) {
            case "showEffectPlayer": return data.getShowEffectPlayer(player.toLowerCase());
            case "showAllEffects": return data.getShowAllEffects(player.toLowerCase());
            case "canSeeMyEffect": return data.getCanSeeMyEffect(player.toLowerCase());
            default: return false;
        }
    }

    private void setSettingValue(String player, String setting, boolean value) {
        switch (setting) {
            case "showEffectPlayer":
                data.setShowEffectPlayer(player, value);
                break;
            case "showAllEffects":
                data.setShowAllEffects(player, value);
                break;
            case "canSeeMyEffect":
                data.setCanSeeMyEffect(player, value);
                break;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();

            if (sender.hasPermission("unusuality.chance.admin")) {
                options.addAll(List.of("set", "add", "remove", "get"));
            }

            if (sender instanceof Player) {
                options.addAll(List.of("gamble", "chance", "seemine", "seeother", "showmine", "info"));
            }

            return options.stream()
                    .filter(opt -> opt.startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            return List.of("all").stream()
                    .filter(opt -> opt.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && List.of("set", "add", "remove").contains(args[0].toLowerCase())) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }

        if (args.length == 2 && (
                args[0].equalsIgnoreCase("seemine") ||
                        args[0].equalsIgnoreCase("seeother") ||
                        args[0].equalsIgnoreCase("showmine"))) {

            return Arrays.asList("on", "off", "вкл", "выкл").stream()
                    .filter(opt -> opt.startsWith(args[1].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }
}
