package com.arkflame.smpmenus.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.Sound;

public final class SoundResolver {
    private static final Map<String, List<String>> ALIASES = createAliases();
    private static final ConcurrentMap<String, Optional<Sound>> CACHE = new ConcurrentHashMap<String, Optional<Sound>>();

    private SoundResolver() {
    }

    public static Optional<Sound> resolve(final String configured) {
        if (configured == null || configured.trim().isEmpty()) {
            return Optional.empty();
        }
        final String normalized = configured.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        final Optional<Sound> cached = CACHE.get(normalized);
        if (cached != null) {
            return cached;
        }
        final Optional<Sound> resolved = resolveUncached(normalized);
        final Optional<Sound> previous = CACHE.putIfAbsent(normalized, resolved);
        return previous == null ? resolved : previous;
    }

    private static Optional<Sound> resolveUncached(final String normalized) {
        final Sound exact = valueOf(normalized);
        if (exact != null) {
            return Optional.of(exact);
        }
        final List<String> aliases = ALIASES.get(normalized);
        if (aliases == null) {
            return Optional.empty();
        }
        for (final String alias : aliases) {
            final Sound sound = valueOf(alias);
            if (sound != null) {
                return Optional.of(sound);
            }
        }
        return Optional.empty();
    }

    private static Sound valueOf(final String name) {
        try {
            return Sound.valueOf(name);
        } catch (final IllegalArgumentException ignored) {
            return null;
        }
    }

    private static Map<String, List<String>> createAliases() {
        final Map<String, List<String>> aliases = new HashMap<String, List<String>>();
        aliases.put("UI_BUTTON_CLICK", Arrays.asList("CLICK", "WOOD_CLICK"));
        aliases.put("BLOCK_WOODEN_BUTTON_CLICK_ON", Arrays.asList("CLICK", "WOOD_CLICK"));
        aliases.put("BLOCK_WOODEN_BUTTON_CLICK_OFF", Arrays.asList("CLICK", "WOOD_CLICK"));
        aliases.put("BLOCK_LEVER_CLICK", Arrays.asList("CLICK", "WOOD_CLICK"));
        aliases.put("BLOCK_CHEST_OPEN", Arrays.asList("CHEST_OPEN"));
        aliases.put("BLOCK_CHEST_CLOSE", Arrays.asList("CHEST_CLOSE"));
        aliases.put("BLOCK_ENDER_CHEST_OPEN", Arrays.asList("CHEST_OPEN"));
        aliases.put("BLOCK_ENDER_CHEST_CLOSE", Arrays.asList("CHEST_CLOSE"));
        aliases.put("ENTITY_PLAYER_LEVELUP", Arrays.asList("LEVEL_UP"));
        aliases.put("ENTITY_ITEM_PICKUP", Arrays.asList("ITEM_PICKUP", "ORB_PICKUP"));
        aliases.put("ENTITY_EXPERIENCE_ORB_PICKUP", Arrays.asList("ORB_PICKUP"));
        aliases.put("BLOCK_NOTE_BLOCK_BASS", Arrays.asList("NOTE_BASS", "NOTE_BASS_DRUM"));
        aliases.put("BLOCK_NOTE_BLOCK_PLING", Arrays.asList("NOTE_PLING", "NOTE_PIANO"));
        aliases.put("BLOCK_NOTE_BLOCK_HARP", Arrays.asList("NOTE_PIANO"));
        aliases.put("BLOCK_NOTE_BLOCK_BELL", Arrays.asList("NOTE_PLING", "NOTE_PIANO"));
        aliases.put("BLOCK_ANVIL_LAND", Arrays.asList("ANVIL_LAND"));
        aliases.put("BLOCK_ANVIL_USE", Arrays.asList("ANVIL_USE"));
        aliases.put("BLOCK_ANVIL_BREAK", Arrays.asList("ANVIL_BREAK"));
        aliases.put("ENTITY_VILLAGER_NO", Arrays.asList("VILLAGER_NO"));
        aliases.put("ENTITY_VILLAGER_YES", Arrays.asList("VILLAGER_YES"));
        aliases.put("ENTITY_ARROW_HIT_PLAYER", Arrays.asList("SUCCESSFUL_HIT"));
        aliases.put("ENTITY_GENERIC_EXPLODE", Arrays.asList("EXPLODE"));
        aliases.put("ENTITY_FIREWORK_ROCKET_BLAST", Arrays.asList("FIREWORK_BLAST"));
        return Collections.unmodifiableMap(aliases);
    }
}
