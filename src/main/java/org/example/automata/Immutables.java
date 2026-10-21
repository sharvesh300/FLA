package org.example.automata;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;

// Read-only copies that keep insertion order, so output is deterministic.
final class Immutables {
    private Immutables() {
    }

    static <T> Set<T> set(Collection<T> source) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Objects.requireNonNull(source)));
    }

    static <K, V> Map<K, V> map(Map<K, V> source) {
        return map(source, UnaryOperator.identity());
    }

    static <K, V> Map<K, V> map(Map<K, V> source, UnaryOperator<V> copyValue) {
        Map<K, V> copy = new LinkedHashMap<>();
        Objects.requireNonNull(source).forEach((key, value) -> copy.put(key, copyValue.apply(value)));
        return Collections.unmodifiableMap(copy);
    }
}
