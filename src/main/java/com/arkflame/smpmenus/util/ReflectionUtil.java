package com.arkflame.smpmenus.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ReflectionUtil {

    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Field> DECLARED_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> REFLECTION_FAILED = java.util.Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private ReflectionUtil() {
    }

    public static Class<?> getCachedClass(final String className) {
        return getCachedClass(className, false);
    }

    public static Class<?> getCachedClass(final String className, final boolean isEnum) {
        final String cacheKey = "class:" + className;
        if (REFLECTION_FAILED.contains(cacheKey)) {
            return null;
        }
        final Class<?> existing = CLASS_CACHE.get(cacheKey);
        if (existing != null) {
            return existing;
        }
        try {
            final Class<?> clazz;
            if (isEnum) {
                clazz = findEnumClass(className);
            } else {
                clazz = Class.forName(className);
            }
            CLASS_CACHE.put(cacheKey, clazz);
            return clazz;
        } catch (final ClassNotFoundException e) {
            REFLECTION_FAILED.add(cacheKey);
            return null;
        }
    }

    private static Class<?> findEnumClass(final String enumName) throws ClassNotFoundException {
        final String[] possiblePaths = {
            "org.bukkit.attribute.Attribute",
            "org.bukkit.event.entity.EntityDamageEvent.DamageCause",
            "org.bukkit.Material",
            "org.bukkit.event.EventPriority"
        };
        for (final String path : possiblePaths) {
            try {
                final Class<?> clazz = Class.forName(path);
                if (clazz.isEnum()) {
                    try {
                        Enum.valueOf((Class<Enum>) clazz, enumName);
                        return clazz;
                    } catch (final IllegalArgumentException ignored) {
                    }
                }
            } catch (final ClassNotFoundException e) {
            }
        }
        throw new ClassNotFoundException("Enum class not found for: " + enumName);
    }

    static Method getCachedMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        if (clazz == null) {
            return null;
        }
        final String cacheKey = buildMethodKey(clazz, methodName, parameterTypes);
        if (REFLECTION_FAILED.contains(cacheKey)) {
            return null;
        }
        final Method existing = METHOD_CACHE.get(cacheKey);
        if (existing != null) {
            return existing;
        }
        try {
            final Method method = clazz.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            METHOD_CACHE.put(cacheKey, method);
            return method;
        } catch (final NoSuchMethodException e) {
            REFLECTION_FAILED.add(cacheKey);
            return null;
        }
    }

    private static String buildMethodKey(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        final StringBuilder sb = new StringBuilder();
        sb.append("method:").append(clazz.getName()).append("#").append(methodName);
        if (parameterTypes != null) {
            for (final Class<?> param : parameterTypes) {
                sb.append("#").append(param.getName());
            }
        }
        return sb.toString();
    }

    private static Field getCachedField(final Class<?> clazz, final String fieldName) {
        if (clazz == null) {
            return null;
        }
        final String cacheKey = "field:" + clazz.getName() + "#" + fieldName;
        if (REFLECTION_FAILED.contains(cacheKey)) {
            return null;
        }
        final Field existing = FIELD_CACHE.get(cacheKey);
        if (existing != null) {
            return existing;
        }
        try {
            final Field field = clazz.getField(fieldName);
            field.setAccessible(true);
            FIELD_CACHE.put(cacheKey, field);
            return field;
        } catch (final NoSuchFieldException e) {
            REFLECTION_FAILED.add(cacheKey);
            return null;
        }
    }

    public static boolean invokeVoidSafe(final Object instance, final String methodName, final Object[] args, final Class<?>... paramTypes) {
        if (instance == null) {
            return false;
        }
        final Method method = getCachedMethod(instance.getClass(), methodName, paramTypes);
        if (method == null) {
            return false;
        }
        try {
            method.invoke(instance, args);
            return true;
        } catch (final ReflectiveOperationException exception) {
            return false;
        }
    }

    public static boolean invokeStaticVoidSafe(final Class<?> clazz, final String methodName, final Object[] args, final Class<?>... paramTypes) {
        if (clazz == null) {
            return false;
        }
        final Method method = getCachedMethod(clazz, methodName, paramTypes);
        if (method == null) {
            return false;
        }
        try {
            method.invoke(null, args);
            return true;
        } catch (final ReflectiveOperationException exception) {
            return false;
        }
    }

    public static <T> T invokeSafe(final Object instance, final String methodName, final T fallback, final Class<?>... paramTypes) {
        return invokeSafe(instance, methodName, fallback, null, paramTypes);
    }

    public static <T> T invokeSafe(final Object instance, final String methodName, final T fallback, final Object[] args, final Class<?>... paramTypes) {
        if (instance == null) {
            return fallback;
        }
        final Method method = getCachedMethod(instance.getClass(), methodName, paramTypes);
        if (method == null) {
            return fallback;
        }
        try {
            final Object result = method.invoke(instance, args);
            return result != null ? (T) result : fallback;
        } catch (final Exception e) {
            return fallback;
        }
    }

    public static <T> T invokeStaticSafe(final Class<?> clazz, final String methodName, final T fallback, final Class<?>... paramTypes) {
        return invokeStaticSafe(clazz, methodName, fallback, null, paramTypes);
    }

    public static <T> T invokeStaticSafe(final Class<?> clazz, final String methodName, final T fallback, final Object[] args, final Class<?>... paramTypes) {
        if (clazz == null) {
            return fallback;
        }
        final Method method = getCachedMethod(clazz, methodName, paramTypes);
        if (method == null) {
            return fallback;
        }
        try {
            final Object result = method.invoke(null, args);
            return result != null ? (T) result : fallback;
        } catch (final Exception e) {
            return fallback;
        }
    }

    public static <T> T getFieldSafe(final Object instance, final String fieldName, final T fallback) {
        if (instance == null) {
            return fallback;
        }
        final Field field = getCachedField(instance.getClass(), fieldName);
        if (field == null) {
            return fallback;
        }
        try {
            final Object result = field.get(instance);
            return result != null ? (T) result : fallback;
        } catch (final Exception e) {
            return fallback;
        }
    }

    public static <T> T getStaticFieldSafe(final Class<?> clazz, final String fieldName, final T fallback) {
        if (clazz == null) {
            return fallback;
        }
        final Field field = getCachedField(clazz, fieldName);
        if (field == null) {
            return fallback;
        }
        try {
            final Object result = field.get(null);
            return result != null ? (T) result : fallback;
        } catch (final Exception e) {
            return fallback;
        }
    }

    public static boolean setFieldSafe(final Object instance, final String fieldName, final Object value) {
        if (instance == null) {
            return false;
        }
        final Field field = getCachedField(instance.getClass(), fieldName);
        if (field == null) {
            return false;
        }
        try {
            field.set(instance, value);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public static <T extends Enum<T>> T getEnumSafe(final Class<T> enumClass, final String name, final T fallback) {
        if (enumClass == null || name == null) {
            return fallback;
        }
        try {
            return Enum.valueOf(enumClass, name);
        } catch (final IllegalArgumentException e) {
            return fallback;
        }
    }

    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
        return getCachedMethod(clazz, methodName, parameterTypes);
    }

    public static Field getField(final Class<?> clazz, final String fieldName) {
        return getCachedField(clazz, fieldName);
    }

    public static Class<?> getClass(final String className) {
        return getCachedClass(className);
    }

    public static Class<?> getEnumClass(final String className) {
        return getCachedClass(className, true);
    }

    private static Field getCachedDeclaredField(final Class<?> clazz, final String fieldName) {
        if (clazz == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        final String cacheKey = "declaredField:" + clazz.getName() + "#" + fieldName;
        if (REFLECTION_FAILED.contains(cacheKey)) {
            return null;
        }
        final Field existing = DECLARED_FIELD_CACHE.get(cacheKey);
        if (existing != null) {
            return existing;
        }
        Class<?> cursor = clazz;
        while (cursor != null) {
            try {
                final Field field = cursor.getDeclaredField(fieldName);
                field.setAccessible(true);
                DECLARED_FIELD_CACHE.put(cacheKey, field);
                return field;
            } catch (final NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            }
        }
        REFLECTION_FAILED.add(cacheKey);
        return null;
    }

    public static Field getDeclaredField(final Class<?> clazz, final String fieldName) {
        return getCachedDeclaredField(clazz, fieldName);
    }

    public static boolean setDeclaredFieldSafe(final Object instance, final String fieldName, final Object value) {
        if (instance == null) {
            return false;
        }
        final Field field = getCachedDeclaredField(instance.getClass(), fieldName);
        if (field == null) {
            return false;
        }
        try {
            field.set(instance, value);
            return true;
        } catch (final IllegalAccessException | IllegalArgumentException exception) {
            return false;
        }
    }

    public static void clearCache() {
        CLASS_CACHE.clear();
        METHOD_CACHE.clear();
        FIELD_CACHE.clear();
        DECLARED_FIELD_CACHE.clear();
        REFLECTION_FAILED.clear();
    }
}