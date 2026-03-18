package com.ezinnovations.ezteleport.model;

import org.bukkit.Location;

import java.util.List;
import java.util.Objects;

public record TeleportCommandDefinition(
        String name,
        List<String> aliases,
        int countdownSeconds,
        int cooldownSeconds,
        Location destination,
        boolean cancelOnMove,
        boolean cancelOnDamage,
        Messages messages,
        Sounds sounds
) {
    public TeleportCommandDefinition {
        Objects.requireNonNull(name, "name");
        aliases = List.copyOf(aliases);
        Objects.requireNonNull(destination, "destination");
        Objects.requireNonNull(messages, "messages");
        Objects.requireNonNull(sounds, "sounds");
    }

    public String permission() {
        return "ezteleport." + name.toLowerCase();
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

    public record Sounds(String tick, String success, String cancelled) {
    }
}
