package com.ezinnovations.ezteleport.config;

import org.bukkit.Location;
import org.bukkit.Server;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public final class TeleportCommandConfig {
    private final Server server;
    private final Logger logger;

    public TeleportCommandConfig(Server server, Logger logger) {
        this.server = Objects.requireNonNull(server, "server");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public Map<String, TeleportCommandDefinition> loadCommands(FileConfiguration config) {
        Map<String, TeleportCommandDefinition> definitions = new LinkedHashMap<>();
        ConfigurationSection commandsSection = config.getConfigurationSection("commands");
        if (commandsSection == null) {
            return definitions;
        }

        for (String rawName : commandsSection.getKeys(false)) {
            String name = rawName.toLowerCase(Locale.ROOT);
            ConfigurationSection commandSection = commandsSection.getConfigurationSection(rawName);
            if (commandSection == null) {
                logger.warning("Skipping command '" + rawName + "' because it is not a valid section.");
                continue;
            }

            ConfigurationSection destinationSection = commandSection.getConfigurationSection("destination");
            ConfigurationSection messageSection = commandSection.getConfigurationSection("messages");
            ConfigurationSection soundSection = commandSection.getConfigurationSection("sounds");

            if (destinationSection == null || messageSection == null || soundSection == null) {
                logger.warning("Skipping command '" + rawName + "' because required sections are missing.");
                continue;
            }

            String worldName = destinationSection.getString("world", "");
            World world = worldName.isBlank() ? null : server.getWorld(worldName);
            if (world == null) {
                logger.warning("Command '" + rawName + "' references unavailable world '" + worldName + "'. It will remain loaded but fail gracefully at runtime.");
            }

            Location destination = new Location(
                    world,
                    destinationSection.getDouble("x"),
                    destinationSection.getDouble("y"),
                    destinationSection.getDouble("z"),
                    (float) destinationSection.getDouble("yaw"),
                    (float) destinationSection.getDouble("pitch")
            );

            List<String> aliases = new ArrayList<>();
            for (String alias : commandSection.getStringList("aliases")) {
                if (!alias.isBlank()) {
                    aliases.add(alias.toLowerCase(Locale.ROOT));
                }
            }

            TeleportCommandDefinition definition = new TeleportCommandDefinition(
                    name,
                    aliases,
                    Math.max(0, commandSection.getInt("countdown", 0)),
                    Math.max(0, commandSection.getInt("cooldown", 0)),
                    destination,
                    commandSection.getBoolean("cancel-on-move", true),
                    commandSection.getBoolean("cancel-on-damage", true),
                    new TeleportCommandDefinition.Messages(
                            messageSection.getString("counting", ""),
                            messageSection.getString("cancelled-move", ""),
                            messageSection.getString("cancelled-damage", ""),
                            messageSection.getString("cooldown", ""),
                            messageSection.getString("success", ""),
                            messageSection.getString("no-permission", ""),
                            messageSection.getString("invalid-world", "")
                    ),
                    new TeleportCommandDefinition.Sounds(
                            soundSection.getString("tick", ""),
                            soundSection.getString("success", ""),
                            soundSection.getString("cancelled", "")
                    )
            );

            definitions.put(name, definition);
        }

        return definitions;
    }

    public String adminReloadMessage(FileConfiguration config) {
        return config.getString("admin.reload-message", "<green>EzTeleport config reloaded.");
    }

    public String adminNoPermissionMessage(FileConfiguration config) {
        return config.getString("admin.no-permission", "<red>You do not have permission.");
    }
}
