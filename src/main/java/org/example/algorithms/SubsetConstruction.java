package org.example.algorithms;

import org.example.automata.Dfa;
import org.example.automata.Nfa;
import org.example.automata.StateNames;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// Each DFA state is the set of NFA states the NFA could be in at that point.
public final class SubsetConstruction {
    private SubsetConstruction() {
    }

    public static Dfa convert(Nfa nfa) {
        Set<String> startSubset = nfa.epsilonClosure(Set.of(nfa.start()));
        Map<Set<String>, String> names = new LinkedHashMap<>();
        Map<String, Map<Character, String>> transitions = new LinkedHashMap<>();
        Deque<Set<String>> pending = new ArrayDeque<>();

        names.put(startSubset, nameOf(nfa, startSubset));
        pending.add(startSubset);

        // Only subsets reachable from the start subset become DFA states.
        while (!pending.isEmpty()) {
            Set<String> current = pending.poll();
            Map<Character, String> row = new LinkedHashMap<>();
            for (char symbol : nfa.alphabet()) {
                // Step every state on the symbol, then follow ε-moves. An empty result is the dead state.
                Set<String> target = nfa.epsilonClosure(nfa.move(current, symbol));
                if (!names.containsKey(target)) {
                    names.put(target, nameOf(nfa, target));
                    pending.add(target);
                }
                row.put(symbol, names.get(target));
            }
            transitions.put(names.get(current), row);
        }

        Set<String> accepting = new LinkedHashSet<>();
        names.forEach((subset, name) -> {
            // A subset accepts if any NFA state in it accepts.
            if (subset.stream().anyMatch(nfa.accepting()::contains)) {
                accepting.add(name);
            }
        });

        return new Dfa(new LinkedHashSet<>(names.values()), nfa.alphabet(), transitions, names.get(startSubset), accepting);
    }

    // Use declaration order so the same subset always gets the same name.
    private static String nameOf(Nfa nfa, Set<String> subset) {
        return StateNames.ofSet(nfa.states().stream().filter(subset::contains).toList());
    }
}
