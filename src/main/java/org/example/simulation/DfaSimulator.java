package org.example.simulation;

import org.example.automata.Dfa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DfaSimulator {
    private DfaSimulator() {
    }

    // Outcome of one input: states visited, the verdict, and why.
    public record Run(String input, List<String> path, boolean accepted, String reason) {
        public Run {
            path = List.copyOf(path);
        }
    }

    public static Run run(Dfa dfa, String input) {
        List<String> path = new ArrayList<>(List.of(dfa.start()));
        String current = dfa.start();
        for (int position = 0; position < input.length(); position++) {
            char symbol = input.charAt(position);
            // Reject early with a clear message instead of just "no transition".
            if (!dfa.alphabet().contains(symbol)) {
                return new Run(input, path, false,
                        "symbol '" + symbol + "' at position " + position + " is not in the alphabet");
            }
            Optional<String> next = dfa.next(current, symbol);
            if (next.isEmpty()) {
                return new Run(input, path, false, "no transition from " + current + " on '" + symbol + "'");
            }
            current = next.get();
            path.add(current);
        }
        boolean accepted = dfa.accepting().contains(current);
        String reason = (accepted ? "ended in accepting state " : "ended in non-accepting state ") + current;
        return new Run(input, path, accepted, reason);
    }
}
