package org.example.algorithms;

import org.example.automata.Dfa;
import org.example.automata.StateNames;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// Runs two DFAs side by side: a state is a pair (state in first, state in second).
public final class ProductConstruction {
    private ProductConstruction() {
    }

    private record Pair(String left, String right) {
        String name() {
            return StateNames.ofPair(left, right);
        }
    }

    public static Dfa intersect(Dfa first, Dfa second) {
        if (!first.alphabet().equals(second.alphabet())) {
            throw new IllegalArgumentException(
                    "both DFAs must share an alphabet, got " + first.alphabet() + " and " + second.alphabet());
        }
        // Complete both so every pair always has a successor.
        Dfa left = first.completed();
        Dfa right = second.completed();

        Pair start = new Pair(left.start(), right.start());
        Set<Pair> reached = new LinkedHashSet<>(Set.of(start));
        Deque<Pair> pending = new ArrayDeque<>(reached);
        Map<String, Map<Character, String>> transitions = new LinkedHashMap<>();

        // Only explore pairs reachable from the start pair.
        while (!pending.isEmpty()) {
            Pair current = pending.poll();
            Map<Character, String> row = new LinkedHashMap<>();
            for (char symbol : left.alphabet()) {
                Pair target = new Pair(
                        left.next(current.left(), symbol).orElseThrow(),
                        right.next(current.right(), symbol).orElseThrow());
                if (reached.add(target)) {
                    pending.add(target);
                }
                row.put(symbol, target.name());
            }
            transitions.put(current.name(), row);
        }

        Set<String> states = new LinkedHashSet<>();
        Set<String> accepting = new LinkedHashSet<>();
        for (Pair pair : reached) {
            states.add(pair.name());
            // Intersection: accept only when both sides accept.
            if (left.accepting().contains(pair.left()) && right.accepting().contains(pair.right())) {
                accepting.add(pair.name());
            }
        }
        return new Dfa(states, left.alphabet(), transitions, start.name(), accepting);
    }
}
