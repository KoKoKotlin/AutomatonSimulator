package me.kokokotlin.main;

import java.util.Arrays;
import java.util.Optional;

public class Automaton {
    private String currentState;
    private final String[] states;
    private final String initialState;
    private final String[] finalStates;
    private final String alphabet;

    private final Transition transition;

    public Automaton(String[] states, int startingState, Integer[] finalStates, Transition transition, String alphabet) {
        this.states = states;
        this.currentState = this.states[startingState];

        this.initialState = this.states[startingState];
        this.finalStates = new String[finalStates.length];
        for (int i = 0; i < finalStates.length; i++) this.finalStates[i] = this.states[finalStates[i]];

        this.alphabet = alphabet;
        this.transition = transition;
    }

    private void reset() {
        currentState = initialState;
    }

    public boolean isAccepted(String word) {
        reset();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            Optional<String> state = transition.getNextState(currentState, c);
            if (state.isEmpty()) return false;
            else currentState = state.get();
        }

        return Arrays.stream(finalStates).anyMatch((String s) -> s.equals(currentState));
    }

    public String getStringRepr() {
        String alphabetRepr = "{ " + String.join(", ", alphabet.split("")) + " }";
        String stateRepr = "{ " + String.join(", ", states) + " }";
        String transitionRepr = transition.getStringRepr();
        String finalStateRepr = "{ " + String.join(", ", finalStates) + " }";
        return String.format("""
               Alphabet: %s,
               States: %s,
               Transitions: %s,
               Initial state: %s,
               Final states: %s
               """, alphabetRepr, stateRepr, transitionRepr, initialState, finalStateRepr);
    }
}
