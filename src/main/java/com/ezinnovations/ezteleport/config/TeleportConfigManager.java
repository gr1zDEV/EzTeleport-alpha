package com.ezinnovations.ezteleport.config;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class TeleportConfigManager {
    private final EzTeleport plugin;
    private final TeleportCommandConfig commandConfig;
    private Map<String, TeleportCommandDefinition> definitions = Collections.emptyMap();
    private String adminReloadMessage = "<green>EzTeleport config reloaded.";
    private String adminNoPermissionMessage = "<red>You do not have permission.";
    private boolean debugEnabled;

    public TeleportConfigManager(EzTeleport plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.commandConfig = new TeleportCommandConfig(plugin.getServer(), plugin.getLogger());
    }

    public void reload() {
        plugin.reloadConfig();
        this.definitions = Collections.unmodifiableMap(new LinkedHashMap<>(commandConfig.loadCommands(plugin.getConfig())));
        this.adminReloadMessage = commandConfig.adminReloadMessage(plugin.getConfig());
        this.adminNoPermissionMessage = commandConfig.adminNoPermissionMessage(plugin.getConfig());
        this.debugEnabled = commandConfig.adminDebugEnabled(plugin.getConfig());
    }

    public Map<String, TeleportCommandDefinition> getDefinitions() {
        return definitions;
    }

    public String getAdminReloadMessage() {
        return adminReloadMessage;
    }

    public String getAdminNoPermissionMessage() {
        return adminNoPermissionMessage;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}
