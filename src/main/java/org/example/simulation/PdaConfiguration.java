package org.example.simulation;

import org.example.automata.Pda;

// The stack is written top-first: "AZ" means A is on top of Z.
public record PdaConfiguration(String state, int position, String stack) {

    public boolean canApply(Pda.Transition transition, String input) {
        boolean inputMatches = !transition.readsInput()
                || (position < input.length() && input.charAt(position) == transition.input());
        boolean stackMatches = !transition.popsStack()
                || (!stack.isEmpty() && stack.charAt(0) == transition.pop());
        return transition.from().equals(state) && inputMatches && stackMatches;
    }

    public PdaConfiguration apply(Pda.Transition transition) {
        String remainingStack = transition.popsStack() ? stack.substring(1) : stack;
        int nextPosition = transition.readsInput() ? position + 1 : position;
        return new PdaConfiguration(transition.to(), nextPosition, transition.push() + remainingStack);
    }

    public String remainingInput(String input) {
        return input.substring(position);
    }
}
