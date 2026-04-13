package com.ezinnovations.ezteleport.service;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.command.GroupedTeleportCommand;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public final class CommandRegistrationService {
    private final EzTeleport plugin;
    private final TeleportManager teleportManager;
    private final Logger logger;
    private final CommandMap commandMap;
    private final Map<String, Command> knownCommands;
    private final List<Command> registeredCommands = new ArrayList<>();
    private final Map<String, Permission> registeredPermissions = new HashMap<>();

    public CommandRegistrationService(EzTeleport plugin, TeleportManager teleportManager) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.teleportManager = Objects.requireNonNull(teleportManager, "teleportManager");
        this.logger = plugin.getLogger();
        this.commandMap = resolveCommandMap();
        this.knownCommands = resolveKnownCommands(commandMap);
    }

    public void registerCommands(Map<String, TeleportCommandDefinition> definitions) {
        unregisterDynamicCommands();

        Map<String, List<GroupedTeleportCommand.Route>> routesByRoot = new LinkedHashMap<>();
        Set<String> registeredRouteKeys = new LinkedHashSet<>();

        definitions.values().forEach(definition -> {
            registerPermission(definition.permission());
            addRoute(definition.commandPathTokens(), definition, routesByRoot, registeredRouteKeys);
            definition.aliasPathTokens().forEach(path -> addRoute(path, definition, routesByRoot, registeredRouteKeys));
        });

        for (Map.Entry<String, List<GroupedTeleportCommand.Route>> entry : routesByRoot.entrySet()) {
            String root = entry.getKey();
            Map<String, List<GroupedTeleportCommand.Route>> perCommandRoutes = Map.of(root, entry.getValue());
            GroupedTeleportCommand command = new GroupedTeleportCommand(plugin, teleportManager, root, List.of(), perCommandRoutes);
            commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
            registeredCommands.add(command);
            debug("command_registered", "name=" + root + " aliases=");
        }

        debug("command_registration_complete", "count=" + registeredCommands.size());
        syncCommands();
    }

    public void unregisterDynamicCommands() {
        for (Command command : registeredCommands) {
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
        debug("commands_unregistered", "count=all");
    }

    public void syncCommands() {
        try {
            Bukkit.getServer().getClass().getMethod("syncCommands").invoke(Bukkit.getServer());
            debug("commands_synced", "status=success");
        } catch (ReflectiveOperationException exception) {
            debug("commands_synced", "status=unsupported");
        }
    }

    private void addRoute(List<String> path,
                          TeleportCommandDefinition definition,
                          Map<String, List<GroupedTeleportCommand.Route>> routesByRoot,
                          Set<String> registeredRouteKeys) {
        if (path.isEmpty()) {
            return;
        }

        String root = path.get(0);
        List<String> remainder = path.subList(1, path.size());
        String routeKey = root + " " + String.join(" ", remainder);

        if (!registeredRouteKeys.add(routeKey)) {
            logger.warning("Duplicate teleport route '/" + routeKey + "' detected. Keeping the first configured route.");
            return;
        }

        routesByRoot.computeIfAbsent(root, ignored -> new ArrayList<>())
                .add(new GroupedTeleportCommand.Route(remainder, definition));
    }

    private void registerPermission(String permissionNode) {
        if (plugin.getServer().getPluginManager().getPermission(permissionNode) != null) {
            return;
        }

        Permission permission = new Permission(permissionNode, PermissionDefault.TRUE);
        plugin.getServer().getPluginManager().addPermission(permission);
        registeredPermissions.put(permissionNode, permission);
        debug("permission_registered", "node=" + permissionNode);
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

    private void removeKnownCommand(String label, Command command) {
        Command mapped = knownCommands.get(label);
        if (mapped == command) {
            knownCommands.remove(label);
        }
    }

    private void debug(String event, String details) {
        if (!plugin.getTeleportConfigManager().isDebugEnabled()) {
            return;
        }
        logger.info("[EzTeleportDebug] event=" + event + " " + details);
    }
}
