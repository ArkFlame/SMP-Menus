package com.arkflame.smpmenus.gui;

import java.util.Objects;
import java.util.Optional;
import org.bukkit.Color;

public final class RgbColor {
    private static final int CHANNEL_COUNT = 3;
    private static final int MIN_CHANNEL = 0;
    private static final int MAX_CHANNEL = 255;

    private final int red;
    private final int green;
    private final int blue;

    private RgbColor(final int red, final int green, final int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static Optional<RgbColor> parse(final String input) {
        if (input == null || input.trim().isEmpty()) {
            return Optional.empty();
        }
        final String[] parts = input.split(",");
        if (parts.length != CHANNEL_COUNT) {
            return Optional.empty();
        }
        final Optional<Integer> red = parseChannel(parts[0]);
        final Optional<Integer> green = parseChannel(parts[1]);
        final Optional<Integer> blue = parseChannel(parts[2]);
        if (!red.isPresent() || !green.isPresent() || !blue.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(new RgbColor(red.get().intValue(), green.get().intValue(), blue.get().intValue()));
    }

    private static Optional<Integer> parseChannel(final String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        final String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        try {
            final int value = Integer.parseInt(trimmed);
            if (value < MIN_CHANNEL || value > MAX_CHANNEL) {
                return Optional.empty();
            }
            return Optional.of(Integer.valueOf(value));
        } catch (final NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public Color toBukkitColor() {
        return Color.fromRGB(red, green, blue);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RgbColor)) {
            return false;
        }
        final RgbColor that = (RgbColor) other;
        return red == that.red && green == that.green && blue == that.blue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Integer.valueOf(red), Integer.valueOf(green), Integer.valueOf(blue));
    }

    @Override
    public String toString() {
        return red + "," + green + "," + blue;
    }
}
