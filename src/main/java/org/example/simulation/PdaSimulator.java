package org.example.simulation;

import org.example.automata.Pda;
import org.example.automata.Pda.Transition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class PdaSimulator {
    public static final int DEFAULT_MAX_CONFIGURATIONS = 100_000;

    private PdaSimulator() {
    }

    public record Step(Transition transition, PdaConfiguration result) {
    }

    public record Result(
            String input,
            boolean accepted,
            PdaConfiguration initial,
            List<Step> steps,
            int exploredConfigurations,
            boolean searchTruncated) {

        public Result {
            steps = List.copyOf(steps);
        }
    }

    private record Origin(PdaConfiguration previous, Transition transition) {
    }

    public static Result simulate(Pda pda, String input) {
        return simulate(pda, input, DEFAULT_MAX_CONFIGURATIONS);
    }

    public static Result simulate(Pda pda, String input, int maxConfigurations) {
        Map<String, List<Transition>> transitionsByState = pda.transitions().stream()
                .collect(Collectors.groupingBy(Transition::from));
        PdaConfiguration initial = new PdaConfiguration(pda.start(), 0, String.valueOf(pda.initialStackSymbol()));

        Map<PdaConfiguration, Origin> origins = new HashMap<>();
        Set<PdaConfiguration> visited = new HashSet<>(Set.of(initial));
        Deque<PdaConfiguration> pending = new ArrayDeque<>(visited);

        // ε-pushes can grow the stack forever, so the visited set alone cannot guarantee termination.
        while (!pending.isEmpty()) {
            PdaConfiguration current = pending.poll();
            if (isAccepting(pda, current, input)) {
                return new Result(input, true, initial, pathTo(current, origins), visited.size(), false);
            }
            for (Transition transition : transitionsByState.getOrDefault(current.state(), List.of())) {
                if (!current.canApply(transition, input)) {
                    continue;
                }
                if (visited.size() >= maxConfigurations) {
                    return new Result(input, false, initial, List.of(), visited.size(), true);
                }
                PdaConfiguration next = current.apply(transition);
                if (visited.add(next)) {
                    origins.put(next, new Origin(current, transition));
                    pending.add(next);
                }
            }
        }
        return new Result(input, false, initial, List.of(), visited.size(), false);
    }

    private static boolean isAccepting(Pda pda, PdaConfiguration configuration, String input) {
        if (configuration.position() < input.length()) {
            return false;
        }
        return switch (pda.acceptanceMode()) {
            case FINAL_STATE -> pda.accepting().contains(configuration.state());
            case EMPTY_STACK -> configuration.stack().isEmpty();
        };
    }

    private static List<Step> pathTo(PdaConfiguration last, Map<PdaConfiguration, Origin> origins) {
        List<Step> steps = new ArrayList<>();
        for (PdaConfiguration current = last; origins.containsKey(current); current = origins.get(current).previous()) {
            steps.add(new Step(origins.get(current).transition(), current));
        }
        Collections.reverse(steps);
        return steps;
    }
}
