package org.example.algorithms;

import org.example.TestStrings;
import org.example.automata.Dfa;
import org.example.automata.Nfa;
import org.example.automata.StateNames;
import org.example.io.AutomatonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubsetConstructionTest {

    @Test
    void convertsEndsWithAb() {
        Nfa nfa = AutomatonParser.parseNfa("""
                states: q0 q1 q2
                alphabet: a b
                start: q0
                accept: q2
                q0 a -> q0 q1
                q0 b -> q0
                q1 b -> q2
                """);

        Dfa dfa = SubsetConstruction.convert(nfa);

        assertEquals(3, dfa.states().size());
        assertEquals("{q0}", dfa.start());
        assertTrue(dfa.accepting().contains("{q0,q2}"));
        assertEquivalent(nfa, dfa);
        for (String input : TestStrings.allUpTo(nfa.alphabet(), 6)) {
            assertEquals(input.endsWith("ab"), dfa.accepts(input), input);
        }
    }

    @Test
    void followsEpsilonMoves() {
        Nfa nfa = AutomatonParser.parseNfa("""
                states: q0 q1
                alphabet: a b
                start: q0
                accept: q1
                q0 a -> q0
                q0 ε -> q1
                q1 b -> q1
                """);

        Dfa dfa = SubsetConstruction.convert(nfa);

        assertEquals("{q0,q1}", dfa.start());
        assertTrue(dfa.accepts(""));
        assertTrue(dfa.accepts("aabb"));
        assertFalse(dfa.accepts("aba"));
        assertEquivalent(nfa, dfa);
    }

    @Test
    void producesCompleteDfaWithDeadState() {
        Nfa nfa = AutomatonParser.parseNfa("""
                states: s t
                alphabet: a b
                start: s
                accept: t
                s a -> t
                """);

        Dfa dfa = SubsetConstruction.convert(nfa);

        assertTrue(dfa.isComplete());
        assertTrue(dfa.states().contains(StateNames.DEAD_STATE));
        assertEquivalent(nfa, dfa);
    }

    private static void assertEquivalent(Nfa nfa, Dfa dfa) {
        for (String input : TestStrings.allUpTo(nfa.alphabet(), 6)) {
            assertEquals(nfa.accepts(input), dfa.accepts(input), input);
        }
    }
}
