package com.ezinnovations.ezteleport.command;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import com.ezinnovations.ezteleport.service.TeleportManager;
import com.ezinnovations.ezteleport.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.List;

public final class EzTeleportCommand implements CommandExecutor, TabCompleter {
    private final EzTeleport plugin;

    public EzTeleportCommand(EzTeleport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!isAdmin(sender)) {
            MessageUtil.sendMessage(sender, plugin.getTeleportConfigManager().getAdminNoPermissionMessage());
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            MessageUtil.sendMessage(sender, plugin.getTeleportConfigManager().getAdminReloadMessage());
            return true;
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("metrics")) {
            return executeMetrics(sender, args);
        }

        if (args.length == 2) {
            return executeTeleportForTarget(sender, args[0], args[1]);
        }

        MessageUtil.sendMessage(sender, "<gray><bold>EzTeleport Admin Commands</bold></gray>");
        MessageUtil.sendMessage(sender, "<yellow>/ezteleport reload</yellow> <gray>- Reload plugin config</gray>");
        MessageUtil.sendMessage(sender, "<yellow>/ezteleport metrics [command]</yellow> <gray>- Show teleport counters</gray>");
        MessageUtil.sendMessage(sender, "<yellow>/ezteleport <player> <teleport></yellow> <gray>- Trigger teleport for a player</gray>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!isAdmin(sender)) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> completions = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
            completions.addAll(List.of("reload", "metrics"));
            return completions.stream()
                    .filter(value -> startsWithIgnoreCase(value, args[0]))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("metrics")) {
            return plugin.getTeleportConfigManager().getDefinitions().values().stream()
                    .map(TeleportCommandDefinition::name)
                    .filter(value -> startsWithIgnoreCase(value, args[1]))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            return plugin.getTeleportConfigManager().getDefinitions().values().stream()
                    .map(TeleportCommandDefinition::name)
                    .filter(value -> startsWithIgnoreCase(value, args[1]))
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private boolean executeTeleportForTarget(CommandSender sender, String targetName, String teleportName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            MessageUtil.sendMessage(sender, "<red>Player '<white>" + targetName + "</white>' is not online.</red>");
            return true;
        }

        Map<String, TeleportCommandDefinition> definitions = plugin.getTeleportConfigManager().getDefinitions();
        TeleportCommandDefinition definition = definitions.get(teleportName.toLowerCase(Locale.ROOT));
        if (definition == null) {
            MessageUtil.sendMessage(sender, "<red>Unknown teleport command '<white>" + teleportName + "</white>'.</red>");
            return true;
        }

        plugin.getTeleportManager().beginTeleport(target, definition);
        MessageUtil.sendMessage(sender, "<green>Teleport started for <white>" + target.getName() + "</white> using <yellow>/" + definition.name() + "</yellow>.</green>");
        return true;
    }

    private boolean executeMetrics(CommandSender sender, String[] args) {
        Map<String, TeleportManager.TeleportMetrics> metrics = plugin.getTeleportManager().metricsSnapshot();
        if (metrics.isEmpty()) {
            MessageUtil.sendMessage(sender, "<gray>No teleport metrics collected yet.</gray>");
            return true;
        }

        if (args.length == 2) {
            String command = args[1].toLowerCase(Locale.ROOT);
            TeleportManager.TeleportMetrics metric = metrics.get(command);
            if (metric == null) {
                MessageUtil.sendMessage(sender, "<red>No metrics found for command <white>" + command + "</white>.</red>");
                return true;
            }
            MessageUtil.sendMessage(
                    sender,
                    "<aqua>metrics</aqua> <white>command=" + command
                            + " attempts=" + metric.attempts()
                            + " succeeded=" + metric.successes()
                            + " cancelled=" + metric.cancelled() + "</white>"
            );
            return true;
        }

        MessageUtil.sendMessage(sender, "<gray><bold>Teleport Metrics</bold></gray>");
        metrics.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(entry -> MessageUtil.sendMessage(
                        sender,
                        "<aqua>metrics</aqua> <white>command=" + entry.getKey()
                                + " attempts=" + entry.getValue().attempts()
                                + " succeeded=" + entry.getValue().successes()
                                + " cancelled=" + entry.getValue().cancelled() + "</white>"
                ));
        return true;
    }

    private boolean isAdmin(CommandSender sender) {
        return !(sender instanceof Player) || sender.hasPermission("ezteleport.admin");
    }

    private boolean startsWithIgnoreCase(String value, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return true;
        }
        return value.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT));
    }
}
