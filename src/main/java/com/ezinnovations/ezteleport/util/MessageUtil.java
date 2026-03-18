package com.ezinnovations.ezteleport.util;

import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class MessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MessageUtil() {
    }

    public static void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, message, Map.of());
    }

    public static void sendMessage(CommandSender sender, String message, Map<String, String> placeholders) {
        sendChatMessage(sender, message, placeholders);
    }

    public static void sendChatMessage(CommandSender sender, String message) {
        sendChatMessage(sender, message, Map.of());
    }

    public static void sendChatMessage(CommandSender sender, String message, Map<String, String> placeholders) {
        if (sender == null || message == null || message.isBlank()) {
            return;
        }
        sender.sendMessage(deserialize(message, placeholders));
    }

    public static void sendActionBarMessage(Player player, String message) {
        sendActionBarMessage(player, message, Map.of());
    }

    public static void sendActionBarMessage(Player player, String message, Map<String, String> placeholders) {
        if (player == null || message == null || message.isBlank()) {
            return;
        }
        player.sendActionBar(deserialize(message, placeholders));
    }

    public static void sendConfiguredMessage(Player player, TeleportCommandDefinition definition, TeleportMessageKey messageKey) {
        sendConfiguredMessage(player, definition, messageKey, Map.of());
    }

    public static void sendConfiguredMessage(Player player,
                                             TeleportCommandDefinition definition,
                                             TeleportMessageKey messageKey,
                                             Map<String, String> placeholders) {
        if (player == null || definition == null || messageKey == null) {
            return;
        }

        if (messageKey.enabled(definition.messageDelivery().chat())) {
            sendChatMessage(player, messageKey.resolve(definition.messages()), placeholders);
        }

        if (messageKey.enabled(definition.messageDelivery().actionbar())) {
            sendActionBarMessage(player, messageKey.resolve(definition.actionbarMessages()), placeholders);
        }
    }

    public static Component deserialize(String message, Map<String, String> placeholders) {
        String output = message == null ? "" : message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return MINI_MESSAGE.deserialize(output);
    }
}
