package com.ezinnovations.ezteleport.model;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;

import java.util.UUID;

public record ActiveTeleport(
        UUID playerId,
        String commandName,
        TeleportCommandDefinition definition,
        Location startLocation,
        ScheduledTask task
) {
}
