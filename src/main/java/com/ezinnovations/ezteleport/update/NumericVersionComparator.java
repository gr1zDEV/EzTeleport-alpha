package com.ezinnovations.ezteleport.update;

import java.util.ArrayList;
import java.util.List;

public final class NumericVersionComparator {
    private NumericVersionComparator() {
    }

    public static int compare(String leftVersion, String rightVersion) {
        List<Integer> leftParts = extractNumericParts(leftVersion);
        List<Integer> rightParts = extractNumericParts(rightVersion);

        int maxSize = Math.max(leftParts.size(), rightParts.size());
        for (int i = 0; i < maxSize; i++) {
            int left = i < leftParts.size() ? leftParts.get(i) : 0;
            int right = i < rightParts.size() ? rightParts.get(i) : 0;
            if (left != right) {
                return Integer.compare(left, right);
            }
        }

        return 0;
    }

    private static List<Integer> extractNumericParts(String version) {
        List<Integer> parts = new ArrayList<>();
        StringBuilder currentDigits = new StringBuilder();

        for (int i = 0; i < version.length(); i++) {
            char c = version.charAt(i);
            if (Character.isDigit(c)) {
                currentDigits.append(c);
            } else if (!currentDigits.isEmpty()) {
                parts.add(parsePart(currentDigits.toString()));
                currentDigits.setLength(0);
            }
        }

        if (!currentDigits.isEmpty()) {
            parts.add(parsePart(currentDigits.toString()));
        }

        if (parts.isEmpty()) {
            parts.add(0);
        }

        return parts;
    }

    private static int parsePart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException ignored) {
            return Integer.MAX_VALUE;
        }
    }
}
