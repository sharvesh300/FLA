package org.example.algorithms;

import org.example.automata.Dfa;
import org.example.automata.StateNames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DfaMinimizer {
    private DfaMinimizer() {
    }

    public static Dfa minimize(Dfa dfa) {
        Dfa complete = withoutUnreachableStates(dfa).completed();
        List<Set<String>> partition = initialPartition(complete);
        while (true) {
            List<Set<String>> refined = refine(complete, partition);
            if (refined.size() == partition.size()) {
                return quotient(complete, refined);
            }
            partition = refined;
        }
    }

    private static Dfa withoutUnreachableStates(Dfa dfa) {
        Set<String> reachable = dfa.reachableStates();
        if (reachable.size() == dfa.states().size()) {
            return dfa;
        }
        Set<String> states = new LinkedHashSet<>();
        Map<String, Map<Character, String>> transitions = new LinkedHashMap<>();
        for (String state : dfa.states()) {
            if (reachable.contains(state)) {
                states.add(state);
                transitions.put(state, dfa.transitions().getOrDefault(state, Map.of()));
            }
        }
        Set<String> accepting = new LinkedHashSet<>(dfa.accepting());
        accepting.retainAll(reachable);
        return new Dfa(states, dfa.alphabet(), transitions, dfa.start(), accepting);
    }

    private static List<Set<String>> initialPartition(Dfa dfa) {
        Set<String> accepting = new LinkedHashSet<>();
        Set<String> rejecting = new LinkedHashSet<>();
        for (String state : dfa.states()) {
            (dfa.accepting().contains(state) ? accepting : rejecting).add(state);
        }
        List<Set<String>> blocks = dfa.accepting().contains(dfa.start())
                ? List.of(accepting, rejecting)
                : List.of(rejecting, accepting);
        return blocks.stream().filter(block -> !block.isEmpty()).toList();
    }

    private static List<Set<String>> refine(Dfa dfa, List<Set<String>> partition) {
        Map<String, Integer> blockOf = blockIndex(partition);
        List<Set<String>> refined = new ArrayList<>();
        for (Set<String> block : partition) {
            Map<List<Integer>, Set<String>> bySignature = new LinkedHashMap<>();
            for (String state : block) {
                List<Integer> signature = dfa.alphabet().stream()
                        .map(symbol -> blockOf.get(dfa.next(state, symbol).orElseThrow()))
                        .toList();
                bySignature.computeIfAbsent(signature, key -> new LinkedHashSet<>()).add(state);
            }
            refined.addAll(bySignature.values());
        }
        return refined;
    }

    private static Dfa quotient(Dfa dfa, List<Set<String>> partition) {
        Map<String, Integer> blockOf = blockIndex(partition);
        List<String> names = partition.stream()
                .map(block -> block.size() == 1 ? block.iterator().next() : StateNames.ofSet(block))
                .toList();

        Map<String, Map<Character, String>> transitions = new LinkedHashMap<>();
        Set<String> accepting = new LinkedHashSet<>();
        for (int i = 0; i < partition.size(); i++) {
            String representative = partition.get(i).iterator().next();
            Map<Character, String> row = new LinkedHashMap<>();
            for (char symbol : dfa.alphabet()) {
                row.put(symbol, names.get(blockOf.get(dfa.next(representative, symbol).orElseThrow())));
            }
            transitions.put(names.get(i), row);
            if (dfa.accepting().contains(representative)) {
                accepting.add(names.get(i));
            }
        }
        return new Dfa(new LinkedHashSet<>(names), dfa.alphabet(), transitions, names.get(blockOf.get(dfa.start())), accepting);
    }

    private static Map<String, Integer> blockIndex(List<Set<String>> partition) {
        Map<String, Integer> blockOf = new HashMap<>();
        for (int i = 0; i < partition.size(); i++) {
            for (String state : partition.get(i)) {
                blockOf.put(state, i);
            }
        }
        return blockOf;
    }
}
