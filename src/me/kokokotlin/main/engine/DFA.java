package me.kokokotlin.main.engine;

import java.util.List;
import java.util.stream.Collectors;

public class DFA extends AutomatonBase {
    private State currentState;

    public DFA(List<State> states, List<State> initialStates, List<State> finalStates, List<String> alphabet) {
        super(states, initialStates, finalStates, alphabet);

        if (!isValidDFA())
            throw new IllegalArgumentException("DFA needs states with unique transitions and only one initial state!");
    }

    private void reset() {
        currentState = initialStates.get(0);
    }

    private boolean isValidDFA() {
        return states.stream().allMatch(State::hasUniqueTransitions) && initialStates.size() == 1;
    }

    @Override
    public boolean match(String word) {
        reset();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            currentState = currentState.getNextStates(new Symbol(String.valueOf(c))).get(0);
        }

        return finalStates.stream().anyMatch((State s) -> s == currentState);
    }

    @Override
    public DFA toDFA() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toDotRepr() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ENFA toENFA() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NFA toNFA() {
        // TODO Auto-generated method stub
        return null;
    }

    private String getTransitionRepr() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < states.size(); i++) {
            State current = states.get(i);
            stringBuilder.append(String.format("State (%s): \n", current.getName()));
            stringBuilder.append(current.getTransition().entrySet().stream()
                    .map(entry -> String.format("\t%c -> %s", entry.getKey(), entry.getValue().get(0).getName()))
                    .collect(Collectors.joining("\n")));

            if (i != states.size() - 1) stringBuilder.append("\n");
        }
        
        return stringBuilder.toString();
    }

    public String getStringRepr() {
        String stateNames = states.stream().map(State::getName).collect(Collectors.joining(", "));
        String finalNames = finalStates.stream().map(State::getName).collect(Collectors.joining(", "));
        String alphabetRepr = "{ " + String.join(", ", alphabet) + " }";
        String stateRepr = "{ " + stateNames + " }";
        String finalStateRepr = "{ " + finalNames + " }";
        return String.format("""
               Alphabet: %s,
               States: %s,
               Transitions:
               %s,
               Initial state: %s,
               Final states: %s
               """, alphabetRepr, stateRepr, getTransitionRepr(), initialStates.get(0).getName(), finalStateRepr);
    }
}
