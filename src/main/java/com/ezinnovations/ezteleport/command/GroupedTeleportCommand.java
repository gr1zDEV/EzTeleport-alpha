package com.ezinnovations.ezteleport.command;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import com.ezinnovations.ezteleport.service.TeleportManager;
import com.ezinnovations.ezteleport.util.MessageUtil;
import com.ezinnovations.ezteleport.util.TeleportMessageKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class GroupedTeleportCommand extends Command implements PluginIdentifiableCommand {
    private final EzTeleport plugin;
    private final TeleportManager teleportManager;
    private final Map<String, List<Route>> routesByRoot;

    public GroupedTeleportCommand(EzTeleport plugin,
                                  TeleportManager teleportManager,
                                  String root,
                                  List<String> aliases,
                                  Map<String, List<Route>> routesByRoot) {
        super(root);
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.teleportManager = Objects.requireNonNull(teleportManager, "teleportManager");
        this.routesByRoot = Objects.requireNonNull(routesByRoot, "routesByRoot");
        this.setAliases(aliases);
        this.setDescription("Teleport command group for " + root);
        this.setUsage(buildUsage(root, routesByRoot.getOrDefault(root, List.of())));
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player player)) {
            return teleportManager.handleConsoleExecution(sender);
        }

        String invokedRoot = commandLabel.toLowerCase(Locale.ROOT);
        List<Route> routes = routesByRoot.getOrDefault(invokedRoot, List.of());
        if (routes.isEmpty()) {
            routes = routesByRoot.getOrDefault(this.getName().toLowerCase(Locale.ROOT), List.of());
        }

        Route route = findRoute(routes, args);
        if (route == null) {
            return false;
        }

        TeleportCommandDefinition definition = route.definition();
        if (!player.hasPermission(definition.permission())) {
            MessageUtil.sendConfiguredMessage(player, definition, TeleportMessageKey.NO_PERMISSION);
            return true;
        }

        teleportManager.beginTeleport(player, definition);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        String root = alias.toLowerCase(Locale.ROOT);
        List<Route> routes = routesByRoot.getOrDefault(root, List.of());

        if (args.length == 0) {
            return List.of();
        }

        int argIndex = args.length - 1;
        String prefix = args[argIndex].toLowerCase(Locale.ROOT);

        TreeSet<String> completions = new TreeSet<>();
        for (Route route : routes) {
            if (argIndex >= route.remainingPath().size()) {
                continue;
            }

            boolean previousMatches = true;
            for (int i = 0; i < argIndex; i++) {
                if (!route.remainingPath().get(i).equalsIgnoreCase(args[i])) {
                    previousMatches = false;
                    break;
                }
            }
            if (!previousMatches) {
                continue;
            }

            String completion = route.remainingPath().get(argIndex);
            if (completion.startsWith(prefix)) {
                completions.add(completion);
            }
        }

        return List.copyOf(completions);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    private Route findRoute(List<Route> routes, String[] args) {
        for (Route route : routes) {
            if (route.remainingPath().size() != args.length) {
                continue;
            }

            boolean matches = true;
            for (int i = 0; i < args.length; i++) {
                if (!route.remainingPath().get(i).equalsIgnoreCase(args[i])) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return route;
            }
        }

        return null;
    }

    private String buildUsage(String root, List<Route> routes) {
        if (routes.isEmpty()) {
            return "/" + root;
        }

        List<String> usages = new ArrayList<>();
        for (Route route : routes.stream()
                .sorted(Comparator.comparing(route -> String.join(" ", route.remainingPath())))
                .toList()) {
            if (route.remainingPath().isEmpty()) {
                usages.add("/" + root);
            } else {
                usages.add("/" + root + " " + String.join(" ", route.remainingPath()));
            }
        }

        return usages.stream().distinct().collect(Collectors.joining(" | "));
    }

    public record Route(List<String> remainingPath, TeleportCommandDefinition definition) {
        public Route {
            remainingPath = List.copyOf(remainingPath);
            Objects.requireNonNull(definition, "definition");
        }
    }
}
