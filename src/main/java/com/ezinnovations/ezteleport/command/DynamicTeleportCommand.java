package com.ezinnovations.ezteleport.command;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import com.ezinnovations.ezteleport.service.TeleportManager;
import com.ezinnovations.ezteleport.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Objects;

public final class DynamicTeleportCommand extends Command implements PluginIdentifiableCommand {
    private final EzTeleport plugin;
    private final TeleportManager teleportManager;
    private final TeleportCommandDefinition definition;

    public DynamicTeleportCommand(EzTeleport plugin, TeleportManager teleportManager, TeleportCommandDefinition definition) {
        super(definition.name());
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.teleportManager = Objects.requireNonNull(teleportManager, "teleportManager");
        this.definition = Objects.requireNonNull(definition, "definition");
        this.setAliases(definition.aliases());
        this.setPermission(definition.permission());
        this.setDescription("Teleport command for " + definition.name());
        this.setUsage("/" + definition.name());
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player player)) {
            return teleportManager.handleConsoleExecution(sender);
        }

        if (!player.hasPermission(definition.permission())) {
            MessageUtil.sendMessage(player, definition.messages().noPermission());
            return true;
        }

        teleportManager.beginTeleport(player, definition);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return List.of();
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    public TeleportCommandDefinition definition() {
        return definition;
    }
}
