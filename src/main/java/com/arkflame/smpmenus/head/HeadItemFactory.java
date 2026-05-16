package com.arkflame.smpmenus.head;

import com.arkflame.smpmenus.hook.PlaceholderHook;
import com.arkflame.smpmenus.menu.MenuItemDefinition;
import com.arkflame.smpmenus.menu.MenuOpenContext;
import com.arkflame.smpmenus.util.MaterialResolver;
import com.arkflame.smpmenus.util.ReflectionUtil;
import com.arkflame.smpmenus.util.StringUtil;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

public final class HeadItemFactory {
    private static final String NAMED_PREFIX = "head-";
    private static final String BASEHEAD_PREFIX = "basehead-";
    private static final String TEXTURE_PREFIX = "texture-";

    private final Plugin plugin;
    private final PlaceholderHook placeholderHook;
    private final ConcurrentMap<String, ItemStack> namedCache = new ConcurrentHashMap<String, ItemStack>();
    private final ConcurrentMap<String, ItemStack> base64Cache = new ConcurrentHashMap<String, ItemStack>();
    private final ConcurrentMap<String, ItemStack> textureCache = new ConcurrentHashMap<String, ItemStack>();
    private final ConcurrentMap<String, Boolean> warnedTokens = new ConcurrentHashMap<String, Boolean>();

    public HeadItemFactory(final Plugin plugin, final PlaceholderHook placeholderHook) {
        this.plugin = plugin;
        this.placeholderHook = placeholderHook;
    }

    public Optional<ItemStack> create(final Player player, final String menuId, final MenuOpenContext context, final MenuItemDefinition definition) {
        final String raw = definition.getMaterial();
        final String resolvedToken = resolveToken(player, menuId, context, raw);
        if (resolvedToken.startsWith(BASEHEAD_PREFIX)) {
            final String base64 = resolvedToken.substring(BASEHEAD_PREFIX.length());
            final String key = base64.trim();
            ItemStack cached = base64Cache.get(key);
            if (cached == null) {
                cached = createBase64Head(base64, key);
                base64Cache.put(key, cached);
            }
            final ItemStack clone = cached.clone();
            final int amount = Math.max(1, Math.min(64, definition.getAmount()));
            clone.setAmount(amount);
            return Optional.of(clone);
        }
        if (resolvedToken.startsWith(TEXTURE_PREFIX)) {
            final String textureId = resolvedToken.substring(TEXTURE_PREFIX.length());
            final String key = HeadTextureCodec.normalizeTextureId(textureId).toLowerCase(Locale.ROOT);
            ItemStack cached = textureCache.get(key);
            if (cached == null) {
                final String base64 = HeadTextureCodec.base64FromTextureId(textureId);
                cached = createBase64Head(base64, key);
                textureCache.put(key, cached);
            }
            final ItemStack clone = cached.clone();
            final int amount = Math.max(1, Math.min(64, definition.getAmount()));
            clone.setAmount(amount);
            return Optional.of(clone);
        }
        if (resolvedToken.startsWith(NAMED_PREFIX)) {
            final String playerName = resolvedToken.substring(NAMED_PREFIX.length());
            final String key = playerName.toLowerCase(Locale.ROOT);
            ItemStack cached = namedCache.get(key);
            if (cached == null) {
                cached = createNamedHead(playerName, key);
                namedCache.put(key, cached);
            }
            final ItemStack clone = cached.clone();
            final int amount = Math.max(1, Math.min(64, definition.getAmount()));
            clone.setAmount(amount);
            return Optional.of(clone);
        }
        return Optional.empty();
    }

    private String resolveToken(final Player player, final String menuId, final MenuOpenContext context, final String raw) {
        final String contextValue = context == null ? (raw == null ? "" : raw) : context.replaceVariables(raw);
        final String builtIns = StringUtil.replaceBuiltIns(player, contextValue, menuId);
        return placeholderHook.apply(player, builtIns).trim();
    }

    private ItemStack createPlainHead() {
        final MaterialResolver.ResolvedMaterial resolved = MaterialResolver.resolve("PLAYER_HEAD", "SKULL_ITEM", 3, Material.CHEST);
        return new ItemStack(resolved.getMaterial(), 1, resolved.getData());
    }

    private ItemStack createBase64Head(final String base64, final String warnKey) {
        final ItemStack head = createPlainHead();
        if (base64 == null || base64.trim().isEmpty()) {
            return head;
        }
        final ItemMeta itemMeta = head.getItemMeta();
        if (!(itemMeta instanceof SkullMeta)) {
            warnOnce(warnKey, "Resolved player head material did not produce SkullMeta.", null);
            return head;
        }
        final SkullMeta meta = (SkullMeta) itemMeta;
        if (!applyModernProfile(meta, base64) && !applyLegacyGameProfile(meta, base64)) {
            warnOnce(warnKey, "Could not apply base64 skull texture. Returning plain head.", null);
        }
        head.setItemMeta(meta);
        return head;
    }

    private boolean applyModernProfile(final SkullMeta meta, final String base64) {
        final Optional<String> skinUrl = HeadTextureCodec.decodeSkinUrl(base64);
        if (!skinUrl.isPresent()) {
            return false;
        }
        try {
            final Class<?> bukkitClass = Bukkit.class;
            final Method create = ReflectionUtil.getMethod(bukkitClass, "createPlayerProfile", UUID.class, String.class);
            if (create == null) {
                return false;
            }
            final Object profile = create.invoke(null, UUID.randomUUID(), "SMPMenus");
            if (profile == null) {
                return false;
            }
            final Method getTextures = ReflectionUtil.getMethod(profile.getClass(), "getTextures");
            if (getTextures == null) {
                return false;
            }
            final Object textures = getTextures.invoke(profile);
            if (textures == null) {
                return false;
            }
            final Method setSkin = findSingleParameterMethod(textures.getClass(), "setSkin", new URL(skinUrl.get()));
            if (setSkin == null) {
                return false;
            }
            setSkin.invoke(textures, new URL(skinUrl.get()));
            final Method setTextures = findSingleParameterMethod(profile.getClass(), "setTextures", textures);
            if (setTextures != null) {
                setTextures.invoke(profile, textures);
            }
            final Method setOwnerProfile = findSingleParameterMethod(meta.getClass(), "setOwnerProfile", profile);
            if (setOwnerProfile == null) {
                return false;
            }
            setOwnerProfile.invoke(meta, profile);
            return true;
        } catch (final ReflectiveOperationException | MalformedURLException | RuntimeException exception) {
            return false;
        }
    }

    private Method findSingleParameterMethod(final Class<?> owner, final String name, final Object argument) {
        if (owner == null || argument == null) {
            return null;
        }
        for (final Method method : owner.getMethods()) {
            if (!method.getName().equals(name) || method.getParameterTypes().length != 1) {
                continue;
            }
            if (method.getParameterTypes()[0].isAssignableFrom(argument.getClass())) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    private boolean applyLegacyGameProfile(final SkullMeta meta, final String base64) {
        try {
            final Class<?> gameProfileClass = ReflectionUtil.getClass("com.mojang.authlib.GameProfile");
            final Class<?> propertyClass = ReflectionUtil.getClass("com.mojang.authlib.properties.Property");
            if (gameProfileClass == null || propertyClass == null) {
                return false;
            }
            final java.lang.reflect.Constructor<?> gameProfileCtor = gameProfileClass.getConstructor(UUID.class, String.class);
            final Object profile = gameProfileCtor.newInstance(UUID.randomUUID(), "");
            final Method putMethod = ReflectionUtil.getMethod(gameProfileClass, "getProperties", java.util.Map.class);
            if (putMethod == null) {
                return false;
            }
            final Object properties = putMethod.invoke(profile);
            if (properties == null) {
                return false;
            }
            final Method addProperty = findSingleParameterMethod(gameProfileClass, "put", properties);
            if (addProperty == null) {
                return false;
            }
            final Object property = propertyClass.getConstructor(String.class, String.class).newInstance("textures", base64);
            addProperty.invoke(properties, property);
            return ReflectionUtil.setDeclaredFieldSafe(meta, "profile", profile);
        } catch (final Exception exception) {
            return false;
        }
    }

    private ItemStack createNamedHead(final String playerName, final String warnKey) {
        final ItemStack head = createPlainHead();
        if (playerName == null || playerName.trim().isEmpty()) {
            return head;
        }
        final ItemMeta itemMeta = head.getItemMeta();
        if (!(itemMeta instanceof SkullMeta)) {
            warnOnce(warnKey, "Resolved player head material did not produce SkullMeta.", null);
            return head;
        }
        final SkullMeta meta = (SkullMeta) itemMeta;
        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        boolean applied = false;
        final Method setOwningPlayer = ReflectionUtil.getMethod(meta.getClass(), "setOwningPlayer", OfflinePlayer.class);
        if (setOwningPlayer != null) {
            try {
                setOwningPlayer.invoke(meta, offlinePlayer);
                applied = true;
            } catch (final ReflectiveOperationException ignored) {
                applied = false;
            }
        }
        if (!applied) {
            try {
                meta.setOwner(playerName);
                applied = true;
            } catch (final RuntimeException exception) {
                warnOnce(warnKey, "Could not apply named skull owner: " + playerName, exception);
            }
        }
        head.setItemMeta(meta);
        return head;
    }

    private void warnOnce(final String key, final String message, final Throwable throwable) {
        if (warnedTokens.putIfAbsent(key, Boolean.TRUE) != null) {
            return;
        }
        if (throwable == null) {
            plugin.getLogger().warning(message);
        } else {
            plugin.getLogger().log(Level.WARNING, message, throwable);
        }
    }

    public void clearCaches() {
        namedCache.clear();
        base64Cache.clear();
        textureCache.clear();
        warnedTokens.clear();
    }

    public void clearNamedCache(final String playerName) {
        if (playerName != null) {
            namedCache.remove(playerName.toLowerCase(Locale.ROOT));
        }
    }

    static boolean isHeadToken(final String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith(NAMED_PREFIX) || value.startsWith(BASEHEAD_PREFIX) || value.startsWith(TEXTURE_PREFIX);
    }
}