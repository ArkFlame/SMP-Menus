package com.arkflame.smpmenus.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SlotParser {
    private SlotParser() {
    }

    public static List<Integer> parseSlots(final Object rawSlot, final List<String> rawSlots, final int size) {
        final List<Integer> slots = new ArrayList<Integer>();
        if (rawSlot instanceof Number) {
            addIfValid(slots, ((Number) rawSlot).intValue(), size);
        } else if (rawSlot instanceof String) {
            addToken(slots, (String) rawSlot, size);
        }
        if (rawSlots != null) {
            for (final String token : rawSlots) {
                addToken(slots, token, size);
            }
        }
        Collections.sort(slots);
        return slots;
    }

    private static void addToken(final List<Integer> slots, final String raw, final int size) {
        if (raw == null) {
            return;
        }
        final String token = raw.trim();
        if (token.isEmpty()) {
            return;
        }
        if (token.contains("-")) {
            final String[] split = token.split("-", 2);
            if (split.length != 2) {
                return;
            }
            final Integer start = parseInteger(split[0]);
            final Integer end = parseInteger(split[1]);
            if (start == null || end == null) {
                return;
            }
            final int min = Math.min(start, end);
            final int max = Math.max(start, end);
            for (int slot = min; slot <= max; slot++) {
                addIfValid(slots, slot, size);
            }
            return;
        }
        final Integer slot = parseInteger(token);
        if (slot != null) {
            addIfValid(slots, slot, size);
        }
    }

    private static void addIfValid(final List<Integer> slots, final int slot, final int size) {
        if (slot >= 0 && slot < size && !slots.contains(slot)) {
            slots.add(slot);
        }
    }

    private static Integer parseInteger(final String raw) {
        try {
            return Integer.valueOf(raw.trim());
        } catch (final NumberFormatException ignored) {
            return null;
        }
    }
}
