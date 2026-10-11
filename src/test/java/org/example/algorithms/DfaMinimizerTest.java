package org.example.algorithms;

import org.example.TestStrings;
import org.example.automata.Dfa;
import org.example.io.AutomatonParser;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DfaMinimizerTest {
    private static final String SIX_STATES = """
            states: A B C D E F
            alphabet: 0 1
            start: A
            accept: C D E
            A 0 -> B
            A 1 -> C
            B 0 -> A
            B 1 -> D
            C 0 -> E
            C 1 -> F
            D 0 -> E
            D 1 -> F
            E 0 -> E
            E 1 -> F
            F 0 -> F
            F 1 -> F
            """;

    @Test
    void mergesEquivalentStates() {
        Dfa dfa = AutomatonParser.parseDfa(SIX_STATES);

        Dfa minimal = DfaMinimizer.minimize(dfa);

        assertEquals(Set.of("{A,B}", "{C,D,E}", "F"), minimal.states());
        assertEquals("{A,B}", minimal.start());
        assertEquals(Set.of("{C,D,E}"), minimal.accepting());
        assertEquivalent(dfa, minimal);
    }

    @Test
    void removesUnreachableStates() {
        Dfa dfa = AutomatonParser.parseDfa(SIX_STATES.replace("states: A B C D E F", "states: A B C D E F G")
                + "G 0 -> A\nG 1 -> G\n");

        Dfa minimal = DfaMinimizer.minimize(dfa);

        assertEquals(3, minimal.states().size());
        assertFalse(minimal.states().stream().anyMatch(state -> state.contains("G")));
    }

    @Test
    void keepsAlreadyMinimalDfa() {
        Dfa dfa = AutomatonParser.parseDfa("""
                states: even odd
                alphabet: 0 1
                start: even
                accept: even
                even 0 -> odd
                even 1 -> even
                odd 0 -> even
                odd 1 -> odd
                """);

        Dfa minimal = DfaMinimizer.minimize(dfa);

        assertEquals(dfa.states(), minimal.states());
        assertEquivalent(dfa, minimal);
    }

    @Test
    void completesPartialDfaBeforeMinimizing() {
        Dfa dfa = AutomatonParser.parseDfa("""
                states: s t u
                alphabet: a b
                start: s
                accept: t u
                s a -> t
                t a -> u
                u a -> t
                """);

        Dfa minimal = DfaMinimizer.minimize(dfa);

        assertEquals(3, minimal.states().size());
        assertEquivalent(dfa, minimal);
    }

    private static void assertEquivalent(Dfa expected, Dfa actual) {
        for (String input : TestStrings.allUpTo(expected.alphabet(), 7)) {
            assertEquals(expected.accepts(input), actual.accepts(input), input);
        }
    }
}
