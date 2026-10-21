package org.example.automata;

import java.util.Collection;
import java.util.Set;

public final class StateNames {
    public static final String DEAD_STATE = "∅";

    private StateNames() {
    }

    // Name for a state built from a set of states; the empty set is the dead state.
    public static String ofSet(Collection<String> members) {
        return members.isEmpty() ? DEAD_STATE : "{" + String.join(",", members) + "}";
    }

    public static String ofPair(String left, String right) {
        return "(" + left + "," + right + ")";
    }

    // Add primes until the name is unused, e.g. ∅ -> ∅'.
    public static String fresh(String base, Set<String> taken) {
        String name = base;
        while (taken.contains(name)) {
            name += "'";
        }
        return name;
    }
}
