package com.ezinnovations.ezteleport;

import com.ezinnovations.ezteleport.command.EzTeleportCommand;
import com.ezinnovations.ezteleport.config.TeleportConfigManager;
import com.ezinnovations.ezteleport.listener.DamageListener;
import com.ezinnovations.ezteleport.service.CommandRegistrationService;
import com.ezinnovations.ezteleport.service.TeleportManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class EzTeleport extends JavaPlugin {
    private TeleportConfigManager teleportConfigManager;
    private TeleportManager teleportManager;
    private CommandRegistrationService commandRegistrationService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.teleportManager = new TeleportManager(this);
        this.teleportConfigManager = new TeleportConfigManager(this);
        this.commandRegistrationService = new CommandRegistrationService(this, teleportManager);

        registerStaticCommand();
        getServer().getPluginManager().registerEvents(new DamageListener(teleportManager), this);

        reloadPlugin();
    }

    @Override
    public void onDisable() {
        if (commandRegistrationService != null) {
            commandRegistrationService.unregisterDynamicCommands();
        }
        if (teleportManager != null) {
            teleportManager.shutdown();
        }
    }

    public void reloadPlugin() {
        teleportManager.cancelAllActiveTeleports();
        teleportConfigManager.reload();
        commandRegistrationService.registerCommands(teleportConfigManager.getDefinitions());
    }

    public TeleportConfigManager getTeleportConfigManager() {
        return teleportConfigManager;
    }

    private void registerStaticCommand() {
        PluginCommand command = getCommand("ezteleport");
        if (command == null) {
            throw new IllegalStateException("Static ezteleport command was not defined in plugin.yml.");
        }

        EzTeleportCommand executor = new EzTeleportCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
