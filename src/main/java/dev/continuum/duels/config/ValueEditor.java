package dev.continuum.duels.config;

public interface ValueEditor<V> {
    V edit(final V currentValue);
}
