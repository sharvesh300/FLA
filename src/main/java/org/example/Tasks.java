package org.example;

import org.example.algorithms.DfaMinimizer;
import org.example.algorithms.ProductConstruction;
import org.example.algorithms.SubsetConstruction;
import org.example.automata.Dfa;
import org.example.automata.Nfa;
import org.example.automata.Pda;
import org.example.automata.Symbols;
import org.example.io.AutomatonParser;
import org.example.io.AutomatonPrinter;
import org.example.simulation.DfaSimulator;
import org.example.simulation.PdaSimulator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class Tasks {
    private Tasks() {
    }

    static String simulateDfa(Path file, List<String> inputs) throws IOException {
        Dfa dfa = AutomatonParser.readDfa(file);
        List<String> sections = new ArrayList<>(List.of(section("DFA", AutomatonPrinter.table(dfa))));
        normalized(inputs).forEach(input -> sections.add(AutomatonPrinter.run(DfaSimulator.run(dfa, input))));
        return String.join("\n\n", sections);
    }

    static String convertNfa(Path file) throws IOException {
        Nfa nfa = AutomatonParser.readNfa(file);
        Dfa dfa = SubsetConstruction.convert(nfa);
        return String.join("\n\n",
                section("NFA", AutomatonPrinter.table(nfa)),
                section("Equivalent DFA (" + dfa.states().size() + " states)", AutomatonPrinter.table(dfa)));
    }

    static String minimizeDfa(Path file) throws IOException {
        Dfa dfa = AutomatonParser.readDfa(file);
        Dfa minimal = DfaMinimizer.minimize(dfa);
        return String.join("\n\n",
                section("Original DFA (" + dfa.states().size() + " states)", AutomatonPrinter.table(dfa)),
                section("Minimized DFA (" + minimal.states().size() + " states)", AutomatonPrinter.table(minimal)));
    }

    static String intersectDfas(Path firstFile, Path secondFile) throws IOException {
        Dfa first = AutomatonParser.readDfa(firstFile);
        Dfa second = AutomatonParser.readDfa(secondFile);
        Dfa product = ProductConstruction.intersect(first, second);
        return String.join("\n\n",
                section("L1", AutomatonPrinter.table(first)),
                section("L2", AutomatonPrinter.table(second)),
                section("L1 ∩ L2 (" + product.states().size() + " states)", AutomatonPrinter.table(product)));
    }

    static String simulatePda(Path file, List<String> inputs) throws IOException {
        Pda pda = AutomatonParser.readPda(file);
        List<String> sections = new ArrayList<>(List.of(section("PDA", AutomatonPrinter.transitions(pda))));
        normalized(inputs).forEach(input -> sections.add(AutomatonPrinter.result(PdaSimulator.simulate(pda, input))));
        return String.join("\n\n", sections);
    }

    private static List<String> normalized(List<String> inputs) {
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("provide at least one input string (use ε for the empty string)");
        }
        return inputs.stream().map(input -> Symbols.isEpsilonToken(input) ? "" : input).toList();
    }

    private static String section(String title, String body) {
        return "== " + title + " ==\n" + body;
    }
}
