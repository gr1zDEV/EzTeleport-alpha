package com.ezinnovations.ezteleport.service;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.command.DynamicTeleportCommand;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class CommandRegistrationService {
    private final EzTeleport plugin;
    private final TeleportManager teleportManager;
    private final CommandMap commandMap;
    private final Map<String, Command> knownCommands;
    private final List<DynamicTeleportCommand> registeredCommands = new ArrayList<>();
    private final Map<String, Permission> registeredPermissions = new HashMap<>();

    public CommandRegistrationService(EzTeleport plugin, TeleportManager teleportManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.teleportManager = Objects.requireNonNull(teleportManager, "teleportManager");
        this.commandMap = resolveCommandMap();
        this.knownCommands = resolveKnownCommands(commandMap);
    }

    public void registerCommands(Map<String, TeleportCommandDefinition> definitions) {
        unregisterDynamicCommands();
        definitions.values().forEach(this::registerCommand);
    }

    public void unregisterDynamicCommands() {
        for (DynamicTeleportCommand command : registeredCommands) {
            command.unregister(commandMap);
            removeKnownCommand(command.getName(), command);
            removeKnownCommand(plugin.getName().toLowerCase(Locale.ROOT) + ":" + command.getName(), command);
            for (String alias : command.getAliases()) {
                removeKnownCommand(alias, command);
                removeKnownCommand(plugin.getName().toLowerCase(Locale.ROOT) + ":" + alias, command);
            }
        }
        registeredCommands.clear();

        registeredPermissions.values().forEach(permission -> plugin.getServer().getPluginManager().removePermission(permission));
        registeredPermissions.clear();
    }

    private void registerCommand(TeleportCommandDefinition definition) {
        DynamicTeleportCommand command = new DynamicTeleportCommand(plugin, teleportManager, definition);
        registerPermission(definition.permission());
        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
        registeredCommands.add(command);
    }

    private void registerPermission(String permissionNode) {
        if (plugin.getServer().getPluginManager().getPermission(permissionNode) != null) {
            return;
        }

        Permission permission = new Permission(permissionNode, PermissionDefault.OP);
        plugin.getServer().getPluginManager().addPermission(permission);
        registeredPermissions.put(permissionNode, permission);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Command> resolveKnownCommands(CommandMap map) {
        try {
            Field field = findField(map.getClass(), "knownCommands");
            field.setAccessible(true);
            return (Map<String, Command>) field.get(map);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access knownCommands from Bukkit CommandMap.", exception);
        }
    }

    private CommandMap resolveCommandMap() {
        try {
            Field field = findField(Bukkit.getServer().getClass(), "commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to access Bukkit CommandMap.", exception);
        }
    }

    private Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private void removeKnownCommand(String label, DynamicTeleportCommand command) {
        Command mapped = knownCommands.get(label);
        if (mapped == command) {
            knownCommands.remove(label);
        }
    }
}
