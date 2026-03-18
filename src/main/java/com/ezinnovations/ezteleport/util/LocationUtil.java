package com.ezinnovations.ezteleport.util;

import org.bukkit.Location;

public final class LocationUtil {
    public static final double MOVE_CANCEL_DISTANCE_SQUARED = 0.25D;

    private LocationUtil() {
    }

    public static boolean hasMovedBeyondThreshold(Location start, Location current) {
        if (start == null || current == null) {
            return false;
        }
        if (start.getWorld() == null || current.getWorld() == null) {
            return false;
        }
        if (!start.getWorld().getUID().equals(current.getWorld().getUID())) {
            return true;
        }
        return start.distanceSquared(current) > MOVE_CANCEL_DISTANCE_SQUARED;
    }
}
