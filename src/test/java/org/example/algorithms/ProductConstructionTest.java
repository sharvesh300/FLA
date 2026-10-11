package org.example.algorithms;

import org.example.TestStrings;
import org.example.automata.Dfa;
import org.example.io.AutomatonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductConstructionTest {
    private final Dfa evenAs = AutomatonParser.parseDfa("""
            states: even odd
            alphabet: a b
            start: even
            accept: even
            even a -> odd
            even b -> even
            odd a -> even
            odd b -> odd
            """);

    private final Dfa endsWithB = AutomatonParser.parseDfa("""
            states: p q
            alphabet: a b
            start: p
            accept: q
            p a -> p
            p b -> q
            q a -> p
            q b -> q
            """);

    @Test
    void acceptsExactlyTheIntersection() {
        Dfa product = ProductConstruction.intersect(evenAs, endsWithB);

        assertEquals(4, product.states().size());
        assertEquals("(even,p)", product.start());
        for (String input : TestStrings.allUpTo(product.alphabet(), 7)) {
            assertEquals(evenAs.accepts(input) && endsWithB.accepts(input), product.accepts(input), input);
        }
    }

    @Test
    void handlesPartialDfas() {
        Dfa onlyAs = AutomatonParser.parseDfa("""
                states: s
                alphabet: a b
                start: s
                accept: s
                s a -> s
                """);

        Dfa product = ProductConstruction.intersect(evenAs, onlyAs);

        for (String input : TestStrings.allUpTo(product.alphabet(), 6)) {
            assertEquals(evenAs.accepts(input) && onlyAs.accepts(input), product.accepts(input), input);
        }
    }

    @Test
    void rejectsDifferentAlphabets() {
        Dfa binary = AutomatonParser.parseDfa("""
                states: s
                alphabet: 0 1
                start: s
                accept: s
                """);

        assertThrows(IllegalArgumentException.class, () -> ProductConstruction.intersect(evenAs, binary));
    }
}
