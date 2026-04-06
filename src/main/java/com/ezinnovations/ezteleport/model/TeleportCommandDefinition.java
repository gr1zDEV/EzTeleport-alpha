package com.ezinnovations.ezteleport.model;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public record TeleportCommandDefinition(
        String name,
        List<String> aliases,
        int countdownSeconds,
        int cooldownSeconds,
        Location destination,
        String destinationCommand,
        boolean cancelOnMove,
        boolean cancelOnDamage,
        Messages messages,
        Messages actionbarMessages,
        MessageDelivery messageDelivery,
        Sounds sounds
) {
    public TeleportCommandDefinition {
        Objects.requireNonNull(name, "name");
        aliases = List.copyOf(aliases);
        Objects.requireNonNull(destination, "destination");
        destinationCommand = destinationCommand == null ? "" : destinationCommand.trim();
        Objects.requireNonNull(messages, "messages");
        Objects.requireNonNull(actionbarMessages, "actionbarMessages");
        Objects.requireNonNull(messageDelivery, "messageDelivery");
        Objects.requireNonNull(sounds, "sounds");
    }

    public String permission() {
        List<String> tokens = commandPathTokens(name);
        if (tokens.isEmpty()) {
            return "ezteleport." + name.toLowerCase(Locale.ROOT).replaceAll("\\s+", ".");
        }
        return "ezteleport." + String.join(".", tokens);
    }

    public boolean usesDestinationCommand() {
        return !destinationCommand.isBlank();
    }

    public List<String> commandPathTokens() {
        return commandPathTokens(name);
    }

    public List<List<String>> aliasPathTokens() {
        List<List<String>> paths = new ArrayList<>();
        for (String alias : aliases) {
            List<String> tokens = commandPathTokens(alias);
            if (!tokens.isEmpty()) {
                paths.add(tokens);
            }
        }
        return List.copyOf(paths);
    }

    private static List<String> commandPathTokens(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            return List.of();
        }

        String[] split = rawPath.toLowerCase(Locale.ROOT).trim().split("\\s+");
        List<String> tokens = new ArrayList<>(split.length);
        for (String token : split) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return List.copyOf(tokens);
    }

    public record Messages(
            String counting,
            String cancelledMove,
            String cancelledDamage,
            String cooldown,
            String success,
            String noPermission,
            String invalidWorld
    ) {
    }

    public record MessageDelivery(Channel chat, Channel actionbar) {
        public MessageDelivery {
            Objects.requireNonNull(chat, "chat");
            Objects.requireNonNull(actionbar, "actionbar");
        }
    }

    public record Channel(
            boolean counting,
            boolean cancelledMove,
            boolean cancelledDamage,
            boolean cooldown,
            boolean success,
            boolean noPermission,
            boolean invalidWorld
    ) {
    }

    public record Sounds(String tick, String success, String cancelled) {
    }
}
