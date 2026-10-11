package org.example.io;

import org.example.automata.Dfa;
import org.example.automata.Nfa;
import org.example.automata.Pda;
import org.example.automata.Symbols;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutomatonParserTest {

    @Test
    void parsesDfaIgnoringCommentsAndBlankLines() {
        Dfa dfa = AutomatonParser.parseDfa("""
                # a comment

                states: s t
                alphabet: a
                start: s
                accept: t
                s a -> t
                """);

        assertEquals(Set.of("s", "t"), dfa.states());
        assertEquals("t", dfa.next("s", 'a').orElseThrow());
    }

    @Test
    void parsesNfaWithMultipleTargetsAndEpsilon() {
        Nfa nfa = AutomatonParser.parseNfa("""
                states: s t u
                alphabet: a
                start: s
                s a -> t u
                s eps -> u
                """);

        assertEquals(Set.of("t", "u"), nfa.targets("s", 'a'));
        assertEquals(Set.of("u"), nfa.targets("s", Symbols.EPSILON));
    }

    @Test
    void parsesPdaTransitionsAndAcceptanceMode() {
        Pda pda = AutomatonParser.parsePda("""
                states: q
                alphabet: a
                stack: Z A
                start: q
                initial-stack: Z
                accept-by: empty-stack
                q a ε -> q A
                q ε Z -> q ε
                """);

        assertEquals(Pda.AcceptanceMode.EMPTY_STACK, pda.acceptanceMode());
        assertEquals(new Pda.Transition("q", 'a', Symbols.EPSILON, "q", "A"), pda.transitions().get(0));
        assertEquals("", pda.transitions().get(1).push());
    }

    @Test
    void reportsLineNumberOfMalformedTransition() {
        AutomatonFormatException error = assertThrows(AutomatonFormatException.class, () -> AutomatonParser.parseDfa("""
                states: s
                alphabet: a
                start: s
                s a -> s s
                """));

        assertTrue(error.getMessage().startsWith("line 4:"), error.getMessage());
    }

    @Test
    void rejectsDuplicateDfaTransition() {
        assertThrows(AutomatonFormatException.class, () -> AutomatonParser.parseDfa("""
                states: s t
                alphabet: a
                start: s
                s a -> s
                s a -> t
                """));
    }

    @Test
    void rejectsEpsilonInDfa() {
        assertThrows(AutomatonFormatException.class, () -> AutomatonParser.parseDfa("""
                states: s
                alphabet: a
                start: s
                s ε -> s
                """));
    }

    @Test
    void reportsMissingDeclaration() {
        AutomatonFormatException error = assertThrows(AutomatonFormatException.class, () -> AutomatonParser.parseDfa("""
                states: s
                alphabet: a
                """));

        assertEquals("missing 'start:' declaration", error.getMessage());
    }

    @Test
    void reportsUnknownKey() {
        AutomatonFormatException error = assertThrows(AutomatonFormatException.class, () -> AutomatonParser.parseDfa("""
                states: s
                initial: s
                """));

        assertTrue(error.getMessage().contains("unknown key 'initial'"));
    }

    @Test
    void wrapsModelValidationErrors() {
        AutomatonFormatException error = assertThrows(AutomatonFormatException.class, () -> AutomatonParser.parseDfa("""
                states: s
                alphabet: a
                start: s
                s a -> nowhere
                """));

        assertTrue(error.getMessage().startsWith("invalid automaton:"));
        assertTrue(error.getMessage().contains("nowhere"));
    }
}
