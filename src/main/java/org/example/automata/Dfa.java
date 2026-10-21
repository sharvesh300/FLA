package org.example.automata;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Deterministic finite automaton. Missing transitions act like an implicit dead state.
public record Dfa(
        Set<String> states,
        Set<Character> alphabet,
        Map<String, Map<Character, String>> transitions,
        String start,
        Set<String> accepting) {

    public Dfa {
        states = Immutables.set(states);
        alphabet = Immutables.set(alphabet);
        transitions = Immutables.map(transitions, row -> Immutables.map(row));
        accepting = Immutables.set(accepting);
        validate(states, alphabet, transitions, start, accepting);
    }

    private static void validate(
            Set<String> states,
            Set<Character> alphabet,
            Map<String, Map<Character, String>> transitions,
            String start,
            Set<String> accepting) {
        Requirements.requireNoEpsilon(alphabet);
        Requirements.requireState(states, start, "start state");
        Requirements.requireStates(states, accepting, "accepting state");
        transitions.forEach((from, row) -> {
            Requirements.requireState(states, from, "transition source");
            row.forEach((symbol, to) -> {
                Requirements.requireSymbol(alphabet, symbol, "transition symbol");
                Requirements.requireState(states, to, "transition target");
            });
        });
    }

    // Empty when this partial DFA has no move here.
    public Optional<String> next(String state, char symbol) {
        return Optional.ofNullable(transitions.getOrDefault(state, Map.of()).get(symbol));
    }

    // Follow one transition per symbol; falling off the graph means reject.
    public boolean accepts(String input) {
        String current = start;
        for (char symbol : input.toCharArray()) {
            Optional<String> next = next(current, symbol);
            if (next.isEmpty()) {
                return false;
            }
            current = next.get();
        }
        return accepting.contains(current);
    }

    public boolean isComplete() {
        return states.stream().allMatch(state -> alphabet.stream().allMatch(symbol -> next(state, symbol).isPresent()));
    }

    // Route every missing transition to a fresh dead state so the DFA is total.
    public Dfa completed() {
        if (isComplete()) {
            return this;
        }
        String dead = StateNames.fresh(StateNames.DEAD_STATE, states);
        Set<String> allStates = new LinkedHashSet<>(states);
        allStates.add(dead);

        Map<String, Map<Character, String>> total = new LinkedHashMap<>();
        for (String state : allStates) {
            Map<Character, String> row = new LinkedHashMap<>();
            for (char symbol : alphabet) {
                row.put(symbol, next(state, symbol).orElse(dead));
            }
            total.put(state, row);
        }
        return new Dfa(allStates, alphabet, total, start, accepting);
    }

    // Breadth-first search from the start state.
    public Set<String> reachableStates() {
        Set<String> reached = new LinkedHashSet<>(List.of(start));
        Deque<String> pending = new ArrayDeque<>(reached);
        while (!pending.isEmpty()) {
            for (String target : transitions.getOrDefault(pending.poll(), Map.of()).values()) {
                if (reached.add(target)) {
                    pending.add(target);
                }
            }
        }
        return Collections.unmodifiableSet(reached);
    }
}
