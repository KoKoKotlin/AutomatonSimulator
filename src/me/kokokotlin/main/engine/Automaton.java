package me.kokokotlin.main.engine;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class Automaton {
    private State currentState;
    private final State[] states;
    private final State initialState;
    private final State[] finalStates;
    private final String alphabet;

    public Automaton(State[] states, int startingState, Integer[] finalStates, String alphabet) {
        this.states = states;
        this.currentState = this.states[startingState];

        this.initialState = this.states[startingState];
        this.finalStates = new State[finalStates.length];
        for (int i = 0; i < finalStates.length; i++) this.finalStates[i] = this.states[finalStates[i]];

        this.alphabet = alphabet;
    }

    private void reset() {
        currentState = initialState;
    }

    public boolean isAccepted(String word) {
        reset();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            currentState = currentState.getNextState(c);
        }

        return Arrays.stream(finalStates).anyMatch((State s) -> s == currentState);
    }

    private String getTransitionRepr() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < states.length; i++) {
            State current = states[i];
            stringBuilder.append(String.format("State (%s): \n", current.getName()));
            stringBuilder.append(current.getTransition().entrySet().stream()
                    .map(entry -> String.format("\t%c -> %s", entry.getKey(), entry.getValue().getName()))
                    .collect(Collectors.joining("\n")));

            if (i != states.length - 1) stringBuilder.append("\n");
        }
        
        return stringBuilder.toString();
    }

    public String getStringRepr() {
        String stateNames = Arrays.stream(states).map(State::getName).collect(Collectors.joining(", "));
        String finalNames = Arrays.stream(finalStates).map(State::getName).collect(Collectors.joining(", "));
        String alphabetRepr = "{ " + String.join(", ", alphabet.split("")) + " }";
        String stateRepr = "{ " + stateNames + " }";
        String finalStateRepr = "{ " + finalNames + " }";
        return String.format("""
               Alphabet: %s,
               States: %s,
               Transitions:
               %s,
               Initial state: %s,
               Final states: %s
               """, alphabetRepr, stateRepr, getTransitionRepr(), initialState.getName(), finalStateRepr);
    }
}
