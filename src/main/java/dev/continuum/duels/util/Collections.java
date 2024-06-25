package dev.continuum.duels.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Collections {
    @NotNull
    public static <V> List<V> emptyList() {
        return new ArrayList<>();
    }

    @NotNull
    public static <V> List<V> list() {
        return emptyList();
    }

    @NotNull
    @SafeVarargs
    public static <V> List<V> list(final @Nullable V @Nullable ... contents) {
        final List<V> list = emptyList();

        if (contents != null) list.addAll(
            Arrays.asList(contents)
        );

        return list;
    }

    @NotNull
    public static <V> List<V> convert(final @Nullable V @Nullable [] contents) {
        final List<V> list = emptyList();

        if (contents != null) {
            list.addAll(Arrays.asList(contents));
        }

        return list;
    }

    @NotNull
    public static <E> Set<E> emptySet() {
        return new HashSet<>();
    }

    @NotNull
    public static <E> Set<E> set() {
        return emptySet();
    }

    @NotNull
    @SafeVarargs
    public static <E> Set<E> set(final @Nullable E @Nullable ... contents) {
        final Set<E> set = emptySet();

        if (contents != null) set.addAll(
            Arrays.asList(contents)
        );

        return set;
    }

    @NotNull
    public static <K, V> Map<K, V> emptyMap() {
        return new ConcurrentHashMap<>();
    }

    @NotNull
    public static <K, V> Map<K, V> map() {
        return emptyMap();
    }

    @NotNull
    public static <K, V> Entry<K, V> entry(final K key, final V value) {
        return Entry.entry(key, value);
    }

    @NotNull
    @SafeVarargs
    public static <K, V> Map<K, V> map(final @Nullable Entry<K, V> @Nullable ... entries) {
        final Map<K, V> map = emptyMap();

        if (entries != null) for (final Entry<K, V> entry : entries) {
            if (entry == null) continue;

            final K key = entry.key();
            if (key == null) continue;

            final V value = entry.value();

            map.put(key, value);
        }

        return map;
    }
}
