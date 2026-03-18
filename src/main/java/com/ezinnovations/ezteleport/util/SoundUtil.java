package com.ezinnovations.ezteleport.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class SoundUtil {
    private SoundUtil() {
    }

    public static void play(Player player, String soundName) {
        if (player == null || soundName == null || soundName.isBlank()) {
            return;
        }

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        } catch (IllegalArgumentException ignored) {
            // Invalid sound names are ignored to keep teleport flow resilient.
        }
    }
}
