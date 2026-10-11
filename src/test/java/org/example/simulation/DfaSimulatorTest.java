package org.example.simulation;

import org.example.TestStrings;
import org.example.automata.Dfa;
import org.example.io.AutomatonParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DfaSimulatorTest {
    private final Dfa evenZeros = AutomatonParser.parseDfa("""
            states: even odd
            alphabet: 0 1
            start: even
            accept: even
            even 0 -> odd
            even 1 -> even
            odd 0 -> even
            odd 1 -> odd
            """);

    @Test
    void acceptsEvenNumberOfZerosAndRecordsPath() {
        DfaSimulator.Run run = DfaSimulator.run(evenZeros, "1001");

        assertTrue(run.accepted());
        assertEquals(List.of("even", "even", "odd", "even", "even"), run.path());
    }

    @Test
    void rejectsOddNumberOfZeros() {
        DfaSimulator.Run run = DfaSimulator.run(evenZeros, "1000");

        assertFalse(run.accepted());
        assertEquals("ended in non-accepting state odd", run.reason());
    }

    @Test
    void acceptsEmptyStringWhenStartStateIsAccepting() {
        assertTrue(DfaSimulator.run(evenZeros, "").accepted());
    }

    @Test
    void rejectsSymbolOutsideAlphabet() {
        DfaSimulator.Run run = DfaSimulator.run(evenZeros, "01x");

        assertFalse(run.accepted());
        assertTrue(run.reason().contains("'x' at position 2"));
    }

    @Test
    void rejectsWhenTransitionIsMissing() {
        Dfa partial = AutomatonParser.parseDfa("""
                states: s t
                alphabet: a b
                start: s
                accept: t
                s a -> t
                """);

        DfaSimulator.Run run = DfaSimulator.run(partial, "ab");

        assertFalse(run.accepted());
        assertEquals("no transition from t on 'b'", run.reason());
    }

    @Test
    void agreesWithDfaAccepts() {
        for (String input : TestStrings.allUpTo(evenZeros.alphabet(), 6)) {
            assertEquals(evenZeros.accepts(input), DfaSimulator.run(evenZeros, input).accepted(), input);
        }
    }
}
