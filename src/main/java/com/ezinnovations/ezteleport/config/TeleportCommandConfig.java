package com.ezinnovations.ezteleport.config;

import org.bukkit.Location;
import org.bukkit.Server;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import com.ezinnovations.ezteleport.util.TeleportMessageKey;
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
            ConfigurationSection actionbarMessageSection = commandSection.getConfigurationSection("actionbar-messages");
            ConfigurationSection messageDeliverySection = commandSection.getConfigurationSection("message-delivery");
            ConfigurationSection soundSection = commandSection.getConfigurationSection("sounds");

            if (destinationSection == null || messageSection == null || soundSection == null) {
                logger.warning("Skipping command '" + rawName + "' because required sections are missing.");
                continue;
            }

            String worldName = destinationSection.getString("world", "");
            String destinationCommand = destinationSection.getString("command", "").trim();
            World world = worldName.isBlank() ? null : server.getWorld(worldName);
            if (destinationCommand.isBlank() && world == null) {
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
                    destinationCommand,
                    commandSection.getBoolean("cancel-on-move", true),
                    commandSection.getBoolean("cancel-on-damage", true),
                    loadMessages(messageSection),
                    loadMessages(actionbarMessageSection),
                    loadMessageDelivery(messageDeliverySection),
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

    private TeleportCommandDefinition.Messages loadMessages(ConfigurationSection section) {
        if (section == null) {
            return new TeleportCommandDefinition.Messages("", "", "", "", "", "", "");
        }

        return new TeleportCommandDefinition.Messages(
                section.getString(TeleportMessageKey.COUNTING.configKey(), ""),
                section.getString(TeleportMessageKey.CANCELLED_MOVE.configKey(), ""),
                section.getString(TeleportMessageKey.CANCELLED_DAMAGE.configKey(), ""),
                section.getString(TeleportMessageKey.COOLDOWN.configKey(), ""),
                section.getString(TeleportMessageKey.SUCCESS.configKey(), ""),
                section.getString(TeleportMessageKey.NO_PERMISSION.configKey(), ""),
                section.getString(TeleportMessageKey.INVALID_WORLD.configKey(), "")
        );
    }

    private TeleportCommandDefinition.MessageDelivery loadMessageDelivery(ConfigurationSection section) {
        ConfigurationSection chatSection = section == null ? null : section.getConfigurationSection("chat");
        ConfigurationSection actionbarSection = section == null ? null : section.getConfigurationSection("actionbar");

        return new TeleportCommandDefinition.MessageDelivery(
                loadChannel(chatSection, true, true),
                loadChannel(actionbarSection, false, true)
        );
    }

    private TeleportCommandDefinition.Channel loadChannel(ConfigurationSection section,
                                                          boolean defaultValue,
                                                          boolean defaultCountingValue) {
        return new TeleportCommandDefinition.Channel(
                getBoolean(section, TeleportMessageKey.COUNTING.configKey(), defaultCountingValue),
                getBoolean(section, TeleportMessageKey.CANCELLED_MOVE.configKey(), defaultValue),
                getBoolean(section, TeleportMessageKey.CANCELLED_DAMAGE.configKey(), defaultValue),
                getBoolean(section, TeleportMessageKey.COOLDOWN.configKey(), defaultValue),
                getBoolean(section, TeleportMessageKey.SUCCESS.configKey(), defaultValue),
                getBoolean(section, TeleportMessageKey.NO_PERMISSION.configKey(), defaultValue),
                getBoolean(section, TeleportMessageKey.INVALID_WORLD.configKey(), defaultValue)
        );
    }

    private boolean getBoolean(ConfigurationSection section, String key, boolean fallback) {
        return section == null ? fallback : section.getBoolean(key, fallback);
    }
}
