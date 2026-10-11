package org.example.simulation;

import org.example.automata.Pda;
import org.example.io.AutomatonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdaSimulatorTest {
    private final Pda anbn = AutomatonParser.parsePda("""
            states: q0 q1 q2
            alphabet: a b
            stack: Z A
            start: q0
            initial-stack: Z
            accept: q2
            q0 a Z -> q0 AZ
            q0 a A -> q0 AA
            q0 b A -> q1 ε
            q1 b A -> q1 ε
            q1 ε Z -> q2 Z
            q0 ε Z -> q2 Z
            """);

    private final Pda evenPalindromes = AutomatonParser.parsePda("""
            states: push pop
            alphabet: a b
            stack: Z A B
            start: push
            initial-stack: Z
            accept-by: empty-stack
            push a ε -> push A
            push b ε -> push B
            push ε ε -> pop ε
            pop a A -> pop ε
            pop b B -> pop ε
            pop ε Z -> pop ε
            """);

    @ParameterizedTest
    @ValueSource(strings = {"", "ab", "aabb", "aaabbb"})
    void acceptsMatchedAsAndBs(String input) {
        assertTrue(PdaSimulator.simulate(anbn, input).accepted());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b", "aab", "abb", "abab", "ba"})
    void rejectsUnmatchedAsAndBs(String input) {
        assertFalse(PdaSimulator.simulate(anbn, input).accepted());
    }

    @Test
    void recordsStackOperationsAlongAcceptingPath() {
        PdaSimulator.Result result = PdaSimulator.simulate(anbn, "aabb");

        List<String> stacks = result.steps().stream().map(step -> step.result().stack()).toList();
        assertEquals(List.of("AZ", "AAZ", "AZ", "Z", "Z"), stacks);
        assertEquals("q2", result.steps().get(result.steps().size() - 1).result().state());
        assertEquals(new PdaConfiguration("q0", 0, "Z"), result.initial());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "aa", "abba", "baab", "abaaba"})
    void acceptsEvenPalindromesByEmptyStack(String input) {
        assertTrue(PdaSimulator.simulate(evenPalindromes, input).accepted());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "ab", "aba", "abab", "abbb"})
    void rejectsNonPalindromes(String input) {
        assertFalse(PdaSimulator.simulate(evenPalindromes, input).accepted());
    }

    @Test
    void terminatesOnUnboundedEpsilonPushes() {
        Pda growing = AutomatonParser.parsePda("""
                states: q
                alphabet: a
                stack: Z A
                start: q
                initial-stack: Z
                accept-by: empty-stack
                q ε ε -> q A
                """);

        PdaSimulator.Result result = PdaSimulator.simulate(growing, "a", 500);

        assertFalse(result.accepted());
        assertTrue(result.searchTruncated());
    }

    @Test
    void terminatesOnEpsilonCycles() {
        Pda cycling = AutomatonParser.parsePda("""
                states: p q
                alphabet: a
                stack: Z
                start: p
                initial-stack: Z
                accept: q
                p ε ε -> p ε
                p ε Z -> p Z
                """);

        PdaSimulator.Result result = PdaSimulator.simulate(cycling, "a");

        assertFalse(result.accepted());
        assertFalse(result.searchTruncated());
    }
}
