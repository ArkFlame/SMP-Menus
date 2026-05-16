package com.arkflame.smpmenus.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public final class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&##?([A-Fa-f0-9]{6})");
    private static final boolean HEX_SUPPORTED = Material.getMaterial("NETHERITE_INGOT") != null;

    private static final LegacyColor[] LEGACY_COLORS = new LegacyColor[]{
            new LegacyColor('0', 0x000000), new LegacyColor('1', 0x0000AA), new LegacyColor('2', 0x00AA00),
            new LegacyColor('3', 0x00AAAA), new LegacyColor('4', 0xAA0000), new LegacyColor('5', 0xAA00AA),
            new LegacyColor('6', 0xFFAA00), new LegacyColor('7', 0xAAAAAA), new LegacyColor('8', 0x555555),
            new LegacyColor('9', 0x5555FF), new LegacyColor('a', 0x55FF55), new LegacyColor('b', 0x55FFFF),
            new LegacyColor('c', 0xFF5555), new LegacyColor('d', 0xFF55FF), new LegacyColor('e', 0xFFFF55),
            new LegacyColor('f', 0xFFFFFF)
    };

    private ColorUtil() {
    }

    public static String color(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        final Matcher matcher = HEX_PATTERN.matcher(input);
        final StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            final String hex = matcher.group(1).toUpperCase(Locale.ROOT);
            matcher.appendReplacement(output, Matcher.quoteReplacement(resolveHex(hex)));
        }
        matcher.appendTail(output);
        return ChatColor.translateAlternateColorCodes('&', output.toString());
    }

    private static String resolveHex(final String hex) {
        if (HEX_SUPPORTED) {
            final StringBuilder builder = new StringBuilder("§x");
            for (int index = 0; index < hex.length(); index++) {
                builder.append('§').append(hex.charAt(index));
            }
            return builder.toString();
        }
        final int rgb = Integer.parseInt(hex, 16);
        return "§" + nearestLegacyCode(rgb);
    }

    private static char nearestLegacyCode(final int rgb) {
        final int red = (rgb >> 16) & 0xFF;
        final int green = (rgb >> 8) & 0xFF;
        final int blue = rgb & 0xFF;
        LegacyColor best = LEGACY_COLORS[15];
        int bestDistance = Integer.MAX_VALUE;
        for (final LegacyColor color : LEGACY_COLORS) {
            final int candidateRed = (color.rgb >> 16) & 0xFF;
            final int candidateGreen = (color.rgb >> 8) & 0xFF;
            final int candidateBlue = color.rgb & 0xFF;
            final int distance = squared(red - candidateRed) + squared(green - candidateGreen) + squared(blue - candidateBlue);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = color;
            }
        }
        return best.code;
    }

    private static int squared(final int value) {
        return value * value;
    }

    private static final class LegacyColor {
        private final char code;
        private final int rgb;

        private LegacyColor(final char code, final int rgb) {
            this.code = code;
            this.rgb = rgb;
        }
    }
}
