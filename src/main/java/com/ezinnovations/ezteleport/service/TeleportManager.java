package com.ezinnovations.ezteleport.service;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.model.ActiveTeleport;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import com.ezinnovations.ezteleport.util.LocationUtil;
import com.ezinnovations.ezteleport.util.MessageUtil;
import com.ezinnovations.ezteleport.util.SoundUtil;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class TeleportManager {
    private final EzTeleport plugin;
    private final Map<UUID, ActiveTeleport> activeTeleports = new ConcurrentHashMap<>();
    private final Map<String, Map<UUID, Long>> cooldowns = new ConcurrentHashMap<>();

    public TeleportManager(EzTeleport plugin) {
        this.plugin = plugin;
    }

    public void cancelAllActiveTeleports() {
        activeTeleports.values().forEach(activeTeleport -> activeTeleport.task().cancel());
        activeTeleports.clear();
    }

    public void shutdown() {
        cancelAllActiveTeleports();
        cooldowns.clear();
    }

    public void beginTeleport(Player player, TeleportCommandDefinition definition) {
        cancelActiveTeleport(player.getUniqueId());

        Location destination = definition.destination();
        if (destination.getWorld() == null) {
            MessageUtil.sendMessage(player, definition.messages().invalidWorld());
            return;
        }

        long remaining = getRemainingCooldownSeconds(player.getUniqueId(), definition);
        if (remaining > 0L) {
            MessageUtil.sendMessage(player, definition.messages().cooldown(), Map.of("time", Long.toString(remaining)));
            return;
        }

        if (definition.countdownSeconds() <= 0) {
            startCooldown(player.getUniqueId(), definition);
            performTeleport(player, definition);
            return;
        }

        Location startLocation = player.getLocation().clone();
        CountdownState countdownState = new CountdownState(definition.countdownSeconds());
        ScheduledTask task = player.getScheduler().runAtFixedRate(
                plugin,
                scheduledTask -> {
                    if (!player.isOnline()) {
                        cleanup(player.getUniqueId(), scheduledTask);
                        return;
                    }

                    ActiveTeleport activeTeleport = activeTeleports.get(player.getUniqueId());
                    if (activeTeleport == null) {
                        scheduledTask.cancel();
                        return;
                    }

                    if (definition.cancelOnMove() && LocationUtil.hasMovedBeyondThreshold(activeTeleport.startLocation(), player.getLocation())) {
                        cancelForMovement(player);
                        return;
                    }

                    if (countdownState.secondsRemaining() > 0) {
                        MessageUtil.sendMessage(player, definition.messages().counting(), Map.of("time", Integer.toString(countdownState.secondsRemaining())));
                        SoundUtil.play(player, definition.sounds().tick());
                        countdownState.decrement();
                        return;
                    }

                    performTeleport(player, definition);
                    cleanup(player.getUniqueId(), scheduledTask);
                },
                () -> activeTeleports.remove(player.getUniqueId()),
                1L,
                20L
        );

        if (task == null) {
            return;
        }

        startCooldown(player.getUniqueId(), definition);
        activeTeleports.put(player.getUniqueId(), new ActiveTeleport(player.getUniqueId(), definition.name(), definition, startLocation, task));
    }

    public boolean hasActiveTeleport(UUID playerId) {
        return activeTeleports.containsKey(playerId);
    }

    public void cancelForDamage(Player player) {
        ActiveTeleport activeTeleport = activeTeleports.get(player.getUniqueId());
        if (activeTeleport == null || !activeTeleport.definition().cancelOnDamage()) {
            return;
        }

        activeTeleport.task().cancel();
        cleanup(player.getUniqueId(), activeTeleport.task());
        MessageUtil.sendMessage(player, activeTeleport.definition().messages().cancelledDamage());
        SoundUtil.play(player, activeTeleport.definition().sounds().cancelled());
    }

    public void cancelForMovement(Player player) {
        ActiveTeleport activeTeleport = activeTeleports.get(player.getUniqueId());
        if (activeTeleport == null) {
            return;
        }

        activeTeleport.task().cancel();
        cleanup(player.getUniqueId(), activeTeleport.task());
        MessageUtil.sendMessage(player, activeTeleport.definition().messages().cancelledMove());
        SoundUtil.play(player, activeTeleport.definition().sounds().cancelled());
    }

    public void cancelActiveTeleport(UUID playerId) {
        ActiveTeleport existing = activeTeleports.remove(playerId);
        if (existing != null) {
            existing.task().cancel();
        }
    }

    public long getRemainingCooldownSeconds(UUID playerId, TeleportCommandDefinition definition) {
        if (definition.cooldownSeconds() <= 0) {
            return 0L;
        }

        Map<UUID, Long> commandCooldowns = cooldowns.computeIfAbsent(definition.name(), ignored -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();
        long expiresAt = commandCooldowns.getOrDefault(playerId, 0L);
        if (expiresAt <= now) {
            commandCooldowns.remove(playerId);
            return 0L;
        }
        return TimeUnit.MILLISECONDS.toSeconds(expiresAt - now) + 1L;
    }

    public void cleanup(UUID playerId, ScheduledTask task) {
        activeTeleports.remove(playerId);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    private void startCooldown(UUID playerId, TeleportCommandDefinition definition) {
        if (definition.cooldownSeconds() <= 0) {
            return;
        }
        long expiresAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(definition.cooldownSeconds());
        cooldowns.computeIfAbsent(definition.name(), ignored -> new ConcurrentHashMap<>()).put(playerId, expiresAt);
    }

    private void performTeleport(Player player, TeleportCommandDefinition definition) {
        Location destination = definition.destination().clone();
        if (destination.getWorld() == null) {
            MessageUtil.sendMessage(player, definition.messages().invalidWorld());
            return;
        }

        player.teleportAsync(destination).thenAccept(success -> {
            if (success) {
                MessageUtil.sendMessage(player, definition.messages().success());
                SoundUtil.play(player, definition.sounds().success());
            }
        });
    }

    public boolean handleConsoleExecution(CommandSender sender) {
        sender.sendMessage("Only players can use teleport commands.");
        return true;
    }

    private static final class CountdownState {
        private int secondsRemaining;

        private CountdownState(int secondsRemaining) {
            this.secondsRemaining = secondsRemaining;
        }

        private int secondsRemaining() {
            return secondsRemaining;
        }

        private void decrement() {
            this.secondsRemaining--;
        }
    }
}
