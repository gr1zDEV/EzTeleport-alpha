package com.ezinnovations.ezteleport.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class MessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MessageUtil() {
    }

    public static void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, message, Map.of());
    }

    public static void sendMessage(CommandSender sender, String message, Map<String, String> placeholders) {
        if (sender == null || message == null || message.isBlank()) {
            return;
        }
        sender.sendMessage(deserialize(message, placeholders));
    }

    public static Component deserialize(String message, Map<String, String> placeholders) {
        String output = message == null ? "" : message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return MINI_MESSAGE.deserialize(output);
    }
}
