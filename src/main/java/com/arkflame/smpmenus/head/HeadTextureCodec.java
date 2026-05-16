package com.arkflame.smpmenus.head;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public final class HeadTextureCodec {
    private static final String TEXTURE_PREFIX = "https://textures.minecraft.net/texture/";

    private HeadTextureCodec() {
    }

    public static String base64FromTextureId(final String textureId) {
        final String normalized = normalizeTextureId(textureId);
        final String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + TEXTURE_PREFIX + normalized + "\"}}}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static Optional<String> decodeSkinUrl(final String base64) {
        if (base64 == null || base64.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            final String decoded = new String(Base64.getDecoder().decode(base64.trim()), StandardCharsets.UTF_8);
            final String marker = "\"url\":\"";
            final int start = decoded.indexOf(marker);
            if (start < 0) {
                return Optional.empty();
            }
            final int valueStart = start + marker.length();
            final int valueEnd = decoded.indexOf('"', valueStart);
            if (valueEnd <= valueStart) {
                return Optional.empty();
            }
            return Optional.of(decoded.substring(valueStart, valueEnd));
        } catch (final IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public static String normalizeTextureId(final String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        final String http = "http://textures.minecraft.net/texture/";
        final String https = "https://textures.minecraft.net/texture/";
        if (normalized.startsWith(http)) {
            normalized = normalized.substring(http.length());
        } else if (normalized.startsWith(https)) {
            normalized = normalized.substring(https.length());
        }
        return normalized;
    }
}