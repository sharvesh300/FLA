package org.example.automata;

import java.util.List;
import java.util.Objects;
import java.util.Set;

// Pushdown automaton: a finite automaton plus a stack.
public record Pda(
        Set<String> states,
        Set<Character> inputAlphabet,
        Set<Character> stackAlphabet,
        List<Transition> transitions,
        String start,
        char initialStackSymbol,
        Set<String> accepting,
        AcceptanceMode acceptanceMode) {

    public enum AcceptanceMode {
        FINAL_STATE,
        EMPTY_STACK
    }

    // Read 'input', pop 'pop', go to 'to', push 'push' (its first char ends up on top). ε skips the read/pop.
    public record Transition(String from, char input, char pop, String to, String push) {
        public Transition {
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
            Objects.requireNonNull(push, "push");
        }

        public boolean readsInput() {
            return input != Symbols.EPSILON;
        }

        public boolean popsStack() {
            return pop != Symbols.EPSILON;
        }

        @Override
        public String toString() {
            String pushed = push.isEmpty() ? String.valueOf(Symbols.EPSILON) : push;
            return "δ(%s, %c, %c) = (%s, %s)".formatted(from, input, pop, to, pushed);
        }
    }

    public Pda {
        states = Immutables.set(states);
        inputAlphabet = Immutables.set(inputAlphabet);
        stackAlphabet = Immutables.set(stackAlphabet);
        transitions = List.copyOf(transitions);
        accepting = Immutables.set(accepting);
        Objects.requireNonNull(acceptanceMode, "acceptanceMode");
        validate(states, inputAlphabet, stackAlphabet, transitions, start, initialStackSymbol, accepting);
    }

    private static void validate(
            Set<String> states,
            Set<Character> inputAlphabet,
            Set<Character> stackAlphabet,
            List<Transition> transitions,
            String start,
            char initialStackSymbol,
            Set<String> accepting) {
        Requirements.requireNoEpsilon(inputAlphabet);
        Requirements.requireNoEpsilon(stackAlphabet);
        Requirements.requireState(states, start, "start state");
        Requirements.requireStates(states, accepting, "accepting state");
        Requirements.requireSymbol(stackAlphabet, initialStackSymbol, "initial stack symbol");
        for (Transition transition : transitions) {
            Requirements.requireState(states, transition.from(), "transition source");
            Requirements.requireState(states, transition.to(), "transition target");
            if (transition.readsInput()) {
                Requirements.requireSymbol(inputAlphabet, transition.input(), "input symbol");
            }
            if (transition.popsStack()) {
                Requirements.requireSymbol(stackAlphabet, transition.pop(), "popped stack symbol");
            }
            for (char pushed : transition.push().toCharArray()) {
                Requirements.requireSymbol(stackAlphabet, pushed, "pushed stack symbol");
            }
        }
    }
}
