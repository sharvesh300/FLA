package org.example.io;

import org.example.automata.Dfa;
import org.example.automata.Nfa;
import org.example.automata.Pda;
import org.example.automata.Symbols;
import org.example.simulation.DfaSimulator;
import org.example.simulation.PdaConfiguration;
import org.example.simulation.PdaSimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public final class AutomatonPrinter {
    private static final String EMPTY = String.valueOf(Symbols.EPSILON);
    private static final String LEGEND = "(-> start, * accepting)";

    private AutomatonPrinter() {
    }

    public static String table(Dfa dfa) {
        return grid(dfa.states(), dfa.start(), dfa.accepting(), new ArrayList<>(dfa.alphabet()),
                (state, symbol) -> dfa.next(state, symbol).orElse("-"));
    }

    public static String table(Nfa nfa) {
        List<Character> columns = new ArrayList<>(nfa.alphabet());
        boolean hasEpsilonMoves = nfa.states().stream().anyMatch(state -> !nfa.targets(state, Symbols.EPSILON).isEmpty());
        if (hasEpsilonMoves) {
            columns.add(Symbols.EPSILON);
        }
        return grid(nfa.states(), nfa.start(), nfa.accepting(), columns, (state, symbol) -> {
            Set<String> targets = nfa.targets(state, symbol);
            return targets.isEmpty() ? "-" : "{" + String.join(",", targets) + "}";
        });
    }

    public static String transitions(Pda pda) {
        List<String> lines = new ArrayList<>();
        lines.add("start: " + pda.start() + ", initial stack: " + pda.initialStackSymbol()
                + ", accept by: " + describe(pda));
        pda.transitions().forEach(transition -> lines.add("  " + transition));
        return String.join("\n", lines);
    }

    public static String run(DfaSimulator.Run run) {
        StringBuilder path = new StringBuilder(run.path().get(0));
        for (int i = 1; i < run.path().size(); i++) {
            path.append(" --").append(run.input().charAt(i - 1)).append("--> ").append(run.path().get(i));
        }
        return "%s: %s (%s)%n  %s".formatted(quoted(run.input()), verdict(run.accepted()), run.reason(), path);
    }

    // For accepted input, prints each transition, its stack effect and the resulting configuration.
    public static String result(PdaSimulator.Result result) {
        String input = result.input();
        if (!result.accepted()) {
            String reason = result.searchTruncated()
                    ? "search stopped after " + result.exploredConfigurations() + " configurations"
                    : "no accepting computation among " + result.exploredConfigurations() + " configurations";
            return "%s: REJECTED (%s)".formatted(quoted(input), reason);
        }

        List<String> transitions = result.steps().stream().map(step -> step.transition().toString()).toList();
        List<String> operations = result.steps().stream().map(step -> operations(step.transition())).toList();
        int transitionWidth = transitions.stream().mapToInt(String::length).max().orElse(0);
        int operationWidth = operations.stream().mapToInt(String::length).max().orElse(0);

        List<String> lines = new ArrayList<>();
        lines.add("%s: ACCEPTED (explored %d configurations)".formatted(quoted(input), result.exploredConfigurations()));
        lines.add("  " + pad("start", transitionWidth + operationWidth + 2) + "   " + configuration(result.initial(), input));
        for (int i = 0; i < result.steps().size(); i++) {
            lines.add(("  " + pad(transitions.get(i), transitionWidth) + "  " + pad(operations.get(i), operationWidth)
                    + " → " + configuration(result.steps().get(i).result(), input)).stripTrailing());
        }
        return String.join("\n", lines);
    }

    // Transition table with aligned columns; '->' marks the start state and '*' accepting states.
    private static String grid(Set<String> states, String start, Set<String> accepting, List<Character> columns,
                               BiFunction<String, Character, String> cell) {
        List<List<String>> rows = new ArrayList<>();
        for (String state : states) {
            List<String> row = new ArrayList<>();
            row.add((state.equals(start) ? "->" : "  ") + (accepting.contains(state) ? "*" : " ") + state);
            columns.forEach(symbol -> row.add(cell.apply(state, symbol)));
            rows.add(row);
        }
        List<String> header = new ArrayList<>(List.of(""));
        columns.forEach(symbol -> header.add(String.valueOf(symbol)));
        rows.add(0, header);

        // Each column is as wide as its widest cell.
        int[] widths = new int[header.size()];
        for (List<String> row : rows) {
            for (int column = 0; column < row.size(); column++) {
                widths[column] = Math.max(widths[column], row.get(column).length());
            }
        }

        List<String> lines = new ArrayList<>();
        for (List<String> row : rows) {
            StringBuilder line = new StringBuilder();
            for (int column = 0; column < row.size(); column++) {
                line.append(pad(row.get(column), widths[column])).append("  ");
            }
            lines.add(line.toString().stripTrailing());
        }
        lines.add(LEGEND);
        return String.join("\n", lines);
    }

    private static String operations(Pda.Transition transition) {
        List<String> parts = new ArrayList<>();
        if (transition.readsInput()) {
            parts.add("read " + transition.input());
        }
        if (transition.popsStack()) {
            parts.add("pop " + transition.pop());
        }
        if (!transition.push().isEmpty()) {
            parts.add("push " + transition.push());
        }
        return parts.isEmpty() ? "ε-move" : String.join(", ", parts);
    }

    private static String configuration(PdaConfiguration configuration, String input) {
        return "(%s, %s, %s)".formatted(configuration.state(),
                orEmpty(configuration.remainingInput(input)), orEmpty(configuration.stack()));
    }

    private static String describe(Pda pda) {
        return switch (pda.acceptanceMode()) {
            case FINAL_STATE -> "final state " + pda.accepting();
            case EMPTY_STACK -> "empty stack";
        };
    }

    private static String verdict(boolean accepted) {
        return accepted ? "ACCEPTED" : "REJECTED";
    }

    private static String quoted(String input) {
        return input.isEmpty() ? EMPTY : "\"" + input + "\"";
    }

    private static String orEmpty(String text) {
        return text.isEmpty() ? EMPTY : text;
    }

    private static String pad(String text, int width) {
        return text + " ".repeat(Math.max(0, width - text.length()));
    }
}
