package dev.continuum.duels.util;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class Entry<K, V> {
    private final K key;
    private final V value;

    public Entry(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    @NotNull
    public static <K, V> Entry<K, V> entry(final K key, final V value) {
        return new Entry<>(key, value);
    }

    public K key() {
        return key;
    }

    public V value() {
        return value;
    }
}
