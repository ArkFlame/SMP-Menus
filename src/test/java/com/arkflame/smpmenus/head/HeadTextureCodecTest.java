package com.arkflame.smpmenus.head;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class HeadTextureCodecTest {
    @Test
    public void textureEncodingProducesDecodableUrl() {
        final String encoded = HeadTextureCodec.base64FromTextureId("abc123");
        Assertions.assertEquals("https://textures.minecraft.net/texture/abc123", HeadTextureCodec.decodeSkinUrl(encoded).orElse(""));
    }

    @Test
    public void fullTextureUrlNormalizesToTextureId() {
        Assertions.assertEquals("abc123", HeadTextureCodec.normalizeTextureId("https://textures.minecraft.net/texture/abc123"));
    }

    @Test
    public void invalidBase64ReturnsEmptyOptional() {
        Assertions.assertFalse(HeadTextureCodec.decodeSkinUrl("not-base64").isPresent());
    }
}