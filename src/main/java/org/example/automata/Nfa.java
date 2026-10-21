package org.example.automata;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

// Nondeterministic finite automaton. ε-moves are stored under Symbols.EPSILON.
public record Nfa(
        Set<String> states,
        Set<Character> alphabet,
        Map<String, Map<Character, Set<String>>> transitions,
        String start,
        Set<String> accepting) {

    public Nfa {
        states = Immutables.set(states);
        alphabet = Immutables.set(alphabet);
        transitions = Immutables.map(transitions, row -> Immutables.map(row, targets -> Immutables.set(targets)));
        accepting = Immutables.set(accepting);
        validate(states, alphabet, transitions, start, accepting);
    }

    private static void validate(
            Set<String> states,
            Set<Character> alphabet,
            Map<String, Map<Character, Set<String>>> transitions,
            String start,
            Set<String> accepting) {
        Requirements.requireNoEpsilon(alphabet);
        Requirements.requireState(states, start, "start state");
        Requirements.requireStates(states, accepting, "accepting state");
        transitions.forEach((from, row) -> {
            Requirements.requireState(states, from, "transition source");
            row.forEach((symbol, targets) -> {
                if (symbol != Symbols.EPSILON) {
                    Requirements.requireSymbol(alphabet, symbol, "transition symbol");
                }
                Requirements.requireStates(states, targets, "transition target");
            });
        });
    }

    public Set<String> targets(String state, char symbol) {
        return transitions.getOrDefault(state, Map.of()).getOrDefault(symbol, Set.of());
    }

    // Every state reachable from 'from' in exactly one 'symbol' step.
    public Set<String> move(Collection<String> from, char symbol) {
        Set<String> result = new LinkedHashSet<>();
        for (String state : from) {
            result.addAll(targets(state, symbol));
        }
        return Collections.unmodifiableSet(result);
    }

    // Everything reachable using only ε-moves, including the starting states.
    public Set<String> epsilonClosure(Collection<String> from) {
        Set<String> closure = new LinkedHashSet<>(from);
        Deque<String> pending = new ArrayDeque<>(from);
        while (!pending.isEmpty()) {
            for (String target : targets(pending.pop(), Symbols.EPSILON)) {
                if (closure.add(target)) {
                    pending.push(target);
                }
            }
        }
        return Collections.unmodifiableSet(closure);
    }

    // Track the whole set of states the NFA could be in at once.
    public boolean accepts(String input) {
        Set<String> current = epsilonClosure(Set.of(start));
        for (char symbol : input.toCharArray()) {
            current = epsilonClosure(move(current, symbol));
        }
        return current.stream().anyMatch(accepting::contains);
    }
}
