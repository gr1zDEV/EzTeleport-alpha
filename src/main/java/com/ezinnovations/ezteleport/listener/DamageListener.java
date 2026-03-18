package com.ezinnovations.ezteleport.listener;

import com.ezinnovations.ezteleport.service.TeleportManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class DamageListener implements Listener {
    private final TeleportManager teleportManager;

    public DamageListener(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        teleportManager.cancelForDamage(player);
    }
}
