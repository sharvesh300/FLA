package org.example.automata;

import java.util.Set;

public final class Symbols {
    public static final char EPSILON = 'ε';

    private static final Set<String> EPSILON_TOKENS = Set.of("ε", "eps", "epsilon");

    private Symbols() {
    }

    public static boolean isEpsilonToken(String token) {
        return EPSILON_TOKENS.contains(token);
    }
}
