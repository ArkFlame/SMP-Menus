package com.arkflame.smpmenus.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.bukkit.Material;

public final class MaterialResolver {
    private static final Map<String, List<String>> ALIASES = createAliases();
    private static final ConcurrentMap<String, ResolvedMaterial> CACHE = new ConcurrentHashMap<String, ResolvedMaterial>();

    private MaterialResolver() {
    }

    public static ResolvedMaterial resolve(final String primary, final String legacy, final int data, final Material fallback) {
        final Material safeFallback = fallback == null ? Material.CHEST : fallback;
        final String cacheKey = String.valueOf(primary) + '|' + String.valueOf(legacy) + '|' + data + '|' + safeFallback.name();
        final ResolvedMaterial cached = CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        final ResolvedMaterial resolved = resolveUncached(primary, legacy, data, safeFallback);
        final ResolvedMaterial previous = CACHE.putIfAbsent(cacheKey, resolved);
        return previous == null ? resolved : previous;
    }

    private static ResolvedMaterial resolveUncached(final String primary, final String legacy, final int data, final Material fallback) {
        final ResolvedMaterial direct = find(primary, data);
        if (direct != null) {
            return direct;
        }
        final ResolvedMaterial legacyResolved = find(legacy, data);
        if (legacyResolved != null) {
            return legacyResolved;
        }
        return new ResolvedMaterial(fallback, (short) 0);
    }

    public static Material resolveMaterialOnly(final String primary, final String legacy, final Material fallback) {
        return resolve(primary, legacy, 0, fallback).getMaterial();
    }

    private static ResolvedMaterial find(final String rawName, final int data) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return null;
        }
        final String normalized = normalize(rawName);
        final Material exact = Material.getMaterial(normalized);
        if (exact != null) {
            return new ResolvedMaterial(exact, (short) Math.max(0, data));
        }
        final List<String> aliases = ALIASES.get(normalized);
        if (aliases == null) {
            return null;
        }
        for (final String alias : aliases) {
            final Material material = Material.getMaterial(alias);
            if (material != null) {
                return new ResolvedMaterial(material, (short) Math.max(0, data));
            }
        }
        return null;
    }

    private static String normalize(final String name) {
        return name.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private static Map<String, List<String>> createAliases() {
        final Map<String, List<String>> aliases = new HashMap<String, List<String>>();
        aliases.put("GRASS_BLOCK", Arrays.asList("GRASS", "DIRT"));
        aliases.put("OAK_SAPLING", Arrays.asList("SAPLING"));
        aliases.put("BLACK_STAINED_GLASS_PANE", Arrays.asList("STAINED_GLASS_PANE", "THIN_GLASS"));
        aliases.put("GRAY_STAINED_GLASS_PANE", Arrays.asList("STAINED_GLASS_PANE", "THIN_GLASS"));
        aliases.put("LIGHT_BLUE_STAINED_GLASS_PANE", Arrays.asList("STAINED_GLASS_PANE", "THIN_GLASS"));
        aliases.put("WHITE_STAINED_GLASS_PANE", Arrays.asList("STAINED_GLASS_PANE", "THIN_GLASS"));
        aliases.put("RED_STAINED_GLASS_PANE", Arrays.asList("STAINED_GLASS_PANE", "THIN_GLASS"));
        aliases.put("GREEN_STAINED_GLASS_PANE", Arrays.asList("STAINED_GLASS_PANE", "THIN_GLASS"));
        aliases.put("BLACK_STAINED_GLASS", Arrays.asList("STAINED_GLASS", "GLASS"));
        aliases.put("RED_DYE", Arrays.asList("INK_SACK"));
        aliases.put("GRAY_DYE", Arrays.asList("INK_SACK"));
        aliases.put("AMETHYST_SHARD", Arrays.asList("PRISMARINE_CRYSTALS", "QUARTZ"));
        aliases.put("RED_CANDLE", Arrays.asList("REDSTONE_TORCH_ON", "REDSTONE_TORCH"));
        aliases.put("BARREL", Arrays.asList("CHEST"));
        aliases.put("LECTERN", Arrays.asList("BOOK"));
        aliases.put("PLAYER_HEAD", Arrays.asList("SKULL_ITEM"));
        aliases.put("OAK_SIGN", Arrays.asList("SIGN", "SIGN_POST"));
        aliases.put("ENCHANTED_GOLDEN_APPLE", Arrays.asList("GOLDEN_APPLE"));
        aliases.put("EXPERIENCE_BOTTLE", Arrays.asList("EXP_BOTTLE"));
        aliases.put("WRITABLE_BOOK", Arrays.asList("BOOK_AND_QUILL", "BOOK"));
        aliases.put("KNOWLEDGE_BOOK", Arrays.asList("BOOK"));
        aliases.put("IRON_BARS", Arrays.asList("IRON_FENCE"));
        return Collections.unmodifiableMap(aliases);
    }

    public static final class ResolvedMaterial {
        private final Material material;
        private final short data;

        public ResolvedMaterial(final Material material, final short data) {
            this.material = material;
            this.data = data;
        }

        public Material getMaterial() {
            return material;
        }

        public short getData() {
            return data;
        }
    }
}
