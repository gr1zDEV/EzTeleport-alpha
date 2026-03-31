package com.ezinnovations.ezteleport.service;

import com.ezinnovations.ezteleport.EzTeleport;
import com.ezinnovations.ezteleport.model.ActiveTeleport;
import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import com.ezinnovations.ezteleport.util.LocationUtil;
import com.ezinnovations.ezteleport.util.MessageUtil;
import com.ezinnovations.ezteleport.util.SoundUtil;
import com.ezinnovations.ezteleport.util.TeleportMessageKey;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class TeleportManager {
    private final EzTeleport plugin;
    private final Logger logger;
    private final Map<UUID, ActiveTeleport> activeTeleports = new ConcurrentHashMap<>();
    private final Map<String, Map<UUID, Long>> cooldowns = new ConcurrentHashMap<>();
    private final Map<String, TeleportMetricCounter> metricsByCommand = new ConcurrentHashMap<>();

    public TeleportManager(EzTeleport plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
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
        recordAttempt(definition.name());

        Location destination = definition.destination();
        if (!definition.usesDestinationCommand() && destination.getWorld() == null) {
            MessageUtil.sendConfiguredMessage(player, definition, TeleportMessageKey.INVALID_WORLD);
            recordFailure(definition.name(), "invalid_world");
            return;
        }

        long remaining = getRemainingCooldownSeconds(player.getUniqueId(), definition);
        if (remaining > 0L) {
            MessageUtil.sendConfiguredMessage(player, definition, TeleportMessageKey.COOLDOWN, Map.of("time", Long.toString(remaining)));
            recordFailure(definition.name(), "cooldown_active");
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
                        MessageUtil.sendConfiguredMessage(
                                player,
                                definition,
                                TeleportMessageKey.COUNTING,
                                Map.of("time", Integer.toString(countdownState.secondsRemaining()))
                        );
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
            recordFailure(definition.name(), "scheduler_unavailable");
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
        MessageUtil.sendConfiguredMessage(player, activeTeleport.definition(), TeleportMessageKey.CANCELLED_DAMAGE);
        SoundUtil.play(player, activeTeleport.definition().sounds().cancelled());
        recordCancelled(activeTeleport.commandName(), "damage");
    }

    public void cancelForMovement(Player player) {
        ActiveTeleport activeTeleport = activeTeleports.get(player.getUniqueId());
        if (activeTeleport == null) {
            return;
        }

        activeTeleport.task().cancel();
        cleanup(player.getUniqueId(), activeTeleport.task());
        MessageUtil.sendConfiguredMessage(player, activeTeleport.definition(), TeleportMessageKey.CANCELLED_MOVE);
        SoundUtil.play(player, activeTeleport.definition().sounds().cancelled());
        recordCancelled(activeTeleport.commandName(), "movement");
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
        if (definition.usesDestinationCommand()) {
            String command = definition.destinationCommand();
            if (command.startsWith("/")) {
                command = command.substring(1);
            }

            boolean executed = player.performCommand(command);
            if (!executed) {
                recordFailure(definition.name(), "destination_command_failed");
                return;
            }

            MessageUtil.sendConfiguredMessage(player, definition, TeleportMessageKey.SUCCESS);
            SoundUtil.play(player, definition.sounds().success());
            recordSuccess(definition.name());
            return;
        }

        Location destination = definition.destination().clone();
        if (destination.getWorld() == null) {
            MessageUtil.sendConfiguredMessage(player, definition, TeleportMessageKey.INVALID_WORLD);
            recordFailure(definition.name(), "invalid_world");
            return;
        }

        player.teleportAsync(destination).thenAccept(success -> {
            if (!success) {
                recordFailure(definition.name(), "teleport_async_failed");
                return;
            }

            player.getScheduler().run(
                    plugin,
                    scheduledTask -> {
                        if (!player.isOnline()) {
                            return;
                        }
                        MessageUtil.sendConfiguredMessage(player, definition, TeleportMessageKey.SUCCESS);
                        SoundUtil.play(player, definition.sounds().success());
                        recordSuccess(definition.name());
                    },
                    null
            );
        });
    }

    public boolean handleConsoleExecution(CommandSender sender) {
        sender.sendMessage("Only players can use teleport commands.");
        return true;
    }

    public Map<String, TeleportMetrics> metricsSnapshot() {
        Map<String, TeleportMetrics> snapshot = new LinkedHashMap<>();
        metricsByCommand.forEach((command, counter) -> snapshot.put(command, counter.snapshot()));
        return snapshot;
    }

    private void recordAttempt(String commandName) {
        TeleportMetricCounter counter = metricsByCommand.computeIfAbsent(commandName, ignored -> new TeleportMetricCounter());
        long attempts = counter.incrementAttempts();
        debug("teleport_attempt", commandName, "attempts=" + attempts);
    }

    private void recordSuccess(String commandName) {
        TeleportMetricCounter counter = metricsByCommand.computeIfAbsent(commandName, ignored -> new TeleportMetricCounter());
        long successes = counter.incrementSuccesses();
        debug("teleport_success", commandName, "successes=" + successes);
    }

    private void recordCancelled(String commandName, String reason) {
        TeleportMetricCounter counter = metricsByCommand.computeIfAbsent(commandName, ignored -> new TeleportMetricCounter());
        long cancelled = counter.incrementCancelled();
        debug("teleport_cancelled", commandName, "reason=" + reason + " cancelled=" + cancelled);
    }

    private void recordFailure(String commandName, String reason) {
        debug("teleport_failed", commandName, "reason=" + reason);
    }

    private void debug(String event, String commandName, String details) {
        if (!plugin.getTeleportConfigManager().isDebugEnabled()) {
            return;
        }
        logger.info("[EzTeleportDebug] event=" + event + " command=" + commandName + " " + details);
    }

    public record TeleportMetrics(long attempts, long successes, long cancelled) {
    }

    private static final class TeleportMetricCounter {
        private long attempts;
        private long successes;
        private long cancelled;

        private synchronized long incrementAttempts() {
            return ++attempts;
        }

        private synchronized long incrementSuccesses() {
            return ++successes;
        }

        private synchronized long incrementCancelled() {
            return ++cancelled;
        }

        private synchronized TeleportMetrics snapshot() {
            return new TeleportMetrics(attempts, successes, cancelled);
        }
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
