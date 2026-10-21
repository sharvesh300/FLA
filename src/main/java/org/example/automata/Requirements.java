package org.example.automata;

import java.util.Collection;
import java.util.Set;

// Validation helpers shared by the automaton constructors.
final class Requirements {
    private Requirements() {
    }

    static void requireState(Set<String> states, String state, String role) {
        if (!states.contains(state)) {
            throw new IllegalArgumentException(role + " '" + state + "' is not a declared state");
        }
    }

    static void requireStates(Set<String> states, Collection<String> candidates, String role) {
        candidates.forEach(state -> requireState(states, state, role));
    }

    static void requireSymbol(Set<Character> alphabet, char symbol, String role) {
        if (!alphabet.contains(symbol)) {
            throw new IllegalArgumentException(role + " '" + symbol + "' is not in the alphabet " + alphabet);
        }
    }

    static void requireNoEpsilon(Set<Character> alphabet) {
        if (alphabet.contains(Symbols.EPSILON)) {
            throw new IllegalArgumentException("ε is reserved for empty moves and cannot be part of an alphabet");
        }
    }
}
