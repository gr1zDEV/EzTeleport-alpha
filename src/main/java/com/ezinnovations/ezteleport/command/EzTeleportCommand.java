package com.ezinnovations.ezteleport.command;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public final class EzTeleportCommand implements CommandExecutor, TabCompleter {
    private final EzTeleport plugin;

    public EzTeleportCommand(EzTeleport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ezteleport.admin")) {
            MessageUtil.sendMessage(sender, plugin.getTeleportConfigManager().getAdminNoPermissionMessage());
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadPlugin();
            MessageUtil.sendMessage(sender, plugin.getTeleportConfigManager().getAdminReloadMessage());
            return true;
        }

        sender.sendMessage("/ezteleport reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return List.of();
    }
}
