package org.example.automata;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DfaTest {
    private final Dfa partial = new Dfa(
            Set.of("s", "t"), Set.of('a', 'b'),
            Map.of("s", Map.of('a', "t")),
            "s", Set.of("t"));

    @Test
    void completedAddsDeadStateForMissingTransitions() {
        Dfa complete = partial.completed();

        assertFalse(partial.isComplete());
        assertTrue(complete.isComplete());
        assertEquals(3, complete.states().size());
        assertEquals(StateNames.DEAD_STATE, complete.next("s", 'b').orElseThrow());
        assertEquals(StateNames.DEAD_STATE, complete.next(StateNames.DEAD_STATE, 'a').orElseThrow());
    }

    @Test
    void completedReturnsSameInstanceWhenAlreadyComplete() {
        Dfa complete = partial.completed();

        assertSame(complete, complete.completed());
    }

    @Test
    void rejectsTransitionToUndeclaredState() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new Dfa(
                Set.of("s"), Set.of('a'), Map.of("s", Map.of('a', "missing")), "s", Set.of()));

        assertTrue(error.getMessage().contains("missing"));
    }

    @Test
    void rejectsEpsilonInAlphabet() {
        assertThrows(IllegalArgumentException.class, () -> new Dfa(
                Set.of("s"), Set.of(Symbols.EPSILON), Map.of(), "s", Set.of()));
    }

    @Test
    void transitionsAreImmutable() {
        assertThrows(UnsupportedOperationException.class, () -> partial.transitions().put("t", Map.of()));
    }
}
