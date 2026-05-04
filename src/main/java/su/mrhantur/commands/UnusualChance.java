package su.mrhantur.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import su.mrhantur.Unusuality;
import su.mrhantur.UnusualityDataManager;

import java.util.*;

/**
 * Команда /unusualchance (и алиасы /uc, /uk и т.д.)
 * Управление шансами выпадения, видимостью эффектов и просмотр статистики.
 */
public class UnusualChance extends Command {

    private final Unusuality plugin;
    private final UnusualityDataManager data;

    public UnusualChance(Unusuality plugin) {
        this(plugin, "unusualchance");
    }

    public UnusualChance(Unusuality plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.data = plugin.getDataManager(); // исправлено с getPlayerData()
        setDescription("Управление шансами игрока и видимостью эффектов");
        setPermission("unusuality.chance");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        // Если нет аргументов и отправитель — игрок → открываем GUI
        if (args.length == 0 && sender instanceof Player player) {
            plugin.getMainUnusualGUI().open(player);
            return true;
        }

        // Настройки видимости эффектов (только для игроков)
        if (args.length >= 1 && sender instanceof Player player) {
            String subCommand = args[0].toLowerCase();

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

        // Информация о текущих настройках игрока
        if (args.length == 1 && args[0].equalsIgnoreCase("info") && sender instanceof Player player) {
            sendVisibilityInfo(player, player.getName().toLowerCase());
            return true;
        }

        // Устаревшие команды (gamble / chance)
        if (args.length == 1 && args[0].equalsIgnoreCase("gamble") && sender instanceof Player player) {
            sender.sendMessage("§cЭта функция устарела. Используйте открытие кейса в /uc");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("chance") && sender instanceof Player player) {
            sender.sendMessage("§cЭта функция устарела. Используйте открытие кейса в /uc");
            return true;
        }

        // Просмотр шансов всех игроков (админская команда)
        if (args.length == 2 && args[0].equalsIgnoreCase("get") && args[1].equalsIgnoreCase("all")) {
            Map<String, Integer> allKeys = data.getAllKeys();
            Map<String, Double> allProgress = data.getAllProgress();

            sender.sendMessage("§5📊 Шансы всех игроков:");

            allKeys.keySet().stream()
                    .sorted(Comparator.comparingInt((String name) -> allKeys.getOrDefault(name, 0)).reversed())
                    .forEach(name -> {
                        int keys = allKeys.getOrDefault(name, 0);
                        double progress = allProgress.getOrDefault(name, 0.0);
                        double chance = keys * 100.0 + progress * 100.0;
                        sender.sendMessage("§7- " + name + ": " + keys + " ключ(ей), прогресс " + String.format(Locale.US, "%.2f", progress) + " (" + String.format(Locale.US, "%.2f", chance) + "%)");
                    });

            return true;
        }

        // Админские команды: set / add / remove
        if (args.length == 3 && sender.hasPermission("unusuality.chance.admin")) {
            String action = args[0].toLowerCase();
            String target = args[1].toLowerCase();
            double value;

            try {
                value = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cНеверное число: " + args[2]);
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
                    sender.sendMessage("§cИспользование: /unusualchance [set|add|remove] <игрок> <значение>");
                    return true;
                }
            }

            double chance = data.getKeys(target) * 100.0 + data.getProgress(target) * 100.0;
            sender.sendMessage("§aШанс для " + target + " теперь " + String.format(Locale.US, "%.2f", chance) + "%");
            return true;
        }

        sender.sendMessage("§cИспользование: /unusualchance ИЛИ /unusualchance [set|add|remove|get all] <игрок> <значение>");
        return true;
    }

    /**
     * Обрабатывает команды включения/выключения настроек видимости.
     */
    private boolean handleToggleCommand(Player player, String setting, String[] args, String settingName) {
        if (args.length == 1) {
            // Переключение состояния
            boolean current = getSettingValue(player.getName(), setting);
            setSettingValue(player.getName(), setting, !current);
            player.sendMessage((current ? "§c" : "§a") + settingName + " " +
                    (current ? "выключены" : "включены"));
            return true;
        }
        else if (args.length == 2) {
            String value = args[1].toLowerCase();
            if (value.equals("on") || value.equals("вкл")) {
                setSettingValue(player.getName(), setting, true);
                player.sendMessage("§a" + settingName + " включены");
                return true;
            }
            else if (value.equals("off") || value.equals("выкл")) {
                setSettingValue(player.getName(), setting, false);
                player.sendMessage("§c" + settingName + " выключены");
                return true;
            }
        }

        player.sendMessage("§cИспользование: /" + getName() + " " + args[0] + " [on/off]");
        return true;
    }

    /**
     * Отправляет игроку информацию о текущих настройках видимости.
     */
    private void sendVisibilityInfo(Player player, String playerName) {
        player.sendMessage("§6--- Настройки видимости эффектов ---");
        player.sendMessage(formatSetting("Показ ваших эффектов (seemine)", data.getShowEffectPlayer(playerName)));
        player.sendMessage(formatSetting("Показ чужих эффектов (seeother)", data.getShowAllEffects(playerName)));
        player.sendMessage(formatSetting("Другие видят ваши эффекты (showmine)", data.getCanSeeMyEffect(playerName)));
    }

    private String formatSetting(String name, boolean value) {
        return "§7- " + name + ": " + (value ? "§aВКЛ" : "§cВЫКЛ");
    }

    private boolean getSettingValue(String player, String setting) {
        String name = player.toLowerCase();
        switch (setting) {
            case "showEffectPlayer": return data.getShowEffectPlayer(name);
            case "showAllEffects": return data.getShowAllEffects(name);
            case "canSeeMyEffect": return data.getCanSeeMyEffect(name);
            default: return false;
        }
    }

    private void setSettingValue(String player, String setting, boolean value) {
        String name = player.toLowerCase();
        switch (setting) {
            case "showEffectPlayer":
                data.setShowEffectPlayer(name, value);
                break;
            case "showAllEffects":
                data.setShowAllEffects(name, value);
                break;
            case "canSeeMyEffect":
                data.setCanSeeMyEffect(name, value);
                break;
        }
    }

    // Автодополнение аргументов
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();

            if (sender.hasPermission("unusuality.chance.admin")) {
                options.addAll(List.of("set", "add", "remove", "get"));
            }

            if (sender instanceof Player) {
                options.addAll(List.of("seemine", "seeother", "showmine", "info"));
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