package com.ezinnovations.ezteleport.util;

import com.ezinnovations.ezteleport.model.TeleportCommandDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("(?i)&\\#([0-9a-f]{6})");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("(?i)&([0-9a-fk-or])");

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
        return MINI_MESSAGE.deserialize(convertLegacyFormatting(output));
    }

    private static String convertLegacyFormatting(String message) {
        String withHex = LEGACY_HEX_PATTERN.matcher(message).replaceAll("<#$1>");
        Matcher matcher = LEGACY_COLOR_PATTERN.matcher(withHex);
        StringBuilder converted = new StringBuilder();
        while (matcher.find()) {
            String replacement = switch (Character.toLowerCase(matcher.group(1).charAt(0))) {
                case '0' -> "<black>";
                case '1' -> "<dark_blue>";
                case '2' -> "<dark_green>";
                case '3' -> "<dark_aqua>";
                case '4' -> "<dark_red>";
                case '5' -> "<dark_purple>";
                case '6' -> "<gold>";
                case '7' -> "<gray>";
                case '8' -> "<dark_gray>";
                case '9' -> "<blue>";
                case 'a' -> "<green>";
                case 'b' -> "<aqua>";
                case 'c' -> "<red>";
                case 'd' -> "<light_purple>";
                case 'e' -> "<yellow>";
                case 'f' -> "<white>";
                case 'k' -> "<obfuscated>";
                case 'l' -> "<bold>";
                case 'm' -> "<strikethrough>";
                case 'n' -> "<underlined>";
                case 'o' -> "<italic>";
                case 'r' -> "<reset>";
                default -> matcher.group();
            };
            matcher.appendReplacement(converted, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(converted);
        return converted.toString();
    }
}
