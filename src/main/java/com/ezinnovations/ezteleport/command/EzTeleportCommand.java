package com.ezinnovations.ezteleport.command;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
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

        if (args.length == 2) {
            return executeTeleportForTarget(sender, args[0], args[1]);
        }

        sender.sendMessage("/ezteleport reload");
        sender.sendMessage("/ezteleport <player> <teleport>");
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
            completions.add(0, "reload");
            return completions;
        }

        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            return plugin.getTeleportConfigManager().getDefinitions().values().stream()
                    .map(TeleportCommandDefinition::name)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    private boolean executeTeleportForTarget(CommandSender sender, String targetName, String teleportName) {
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage("Player '" + targetName + "' is not online.");
            return true;
        }

        Map<String, TeleportCommandDefinition> definitions = plugin.getTeleportConfigManager().getDefinitions();
        TeleportCommandDefinition definition = definitions.get(teleportName.toLowerCase(Locale.ROOT));
        if (definition == null) {
            sender.sendMessage("Unknown teleport command '" + teleportName + "'.");
            return true;
        }

        plugin.getTeleportManager().beginTeleport(target, definition);
        sender.sendMessage("Teleport started for " + target.getName() + " using /" + definition.name() + ".");
        return true;
    }

    private boolean isAdmin(CommandSender sender) {
        return !(sender instanceof Player) || sender.hasPermission("ezteleport.admin");
    }
}
