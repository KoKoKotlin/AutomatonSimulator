package me.kokokotlin.main.engine.regex;

import me.kokokotlin.main.engine.State;

import java.util.*;

public class EpsilonNFA {
    // encoding of empty word
    public static final Character epsilon = 0xFF;

    // states names are implicitly defined as the indices of the list
    // first state is the initial state and the last state is the final state
    private final List<State> states = new ArrayList<>();
    private List<String> alphabet;

    boolean hasEpsilons = false;


    public EpsilonNFA(String regex) {
        loadEpsilonNFA(regex);
    }

    private void addTransition(String symbol, Map<String, List<Integer>> nfaState, Integer destStateIndex) {
        List<Integer> transition = nfaState.getOrDefault(symbol, null);
        if (transition == null) transition = new ArrayList<>();

        transition.add(destStateIndex);
        nfaState.put(symbol, transition);
    }

    public List<State> getEpsilonClojure(List<State> states) {
        Set<State> resultingStates = new HashSet<>();
        for (State s: states) resultingStates.addAll(getEpsilonClojure(s));

        return resultingStates.stream().toList();
    }

    public List<State> getEpsilonClojure(State state) {
        List<State> epsilonCl = new ArrayList<>();
        epsilonCl.add(state);  // state is always in its own e-Cl

        // states that still need to be looked at to determine the e-Cl
        Queue<State> nextStates = new LinkedList<>();
        nextStates.add(state);

        while (!nextStates.isEmpty()) {
            State current = nextStates.poll();

            List<State> reachableStates = current.getNextStates(epsilon);

            for (State s: reachableStates) {
                if (!epsilonCl.contains(s)) {
                    epsilonCl.add(s);
                    nextStates.add(s);
                }
            }
        }

        return epsilonCl;
    }

    public List<State> makeTransition(List<State> states, String symbol) {
        Set<State> resultingStates = new HashSet<>();
        for (State s: states) resultingStates.addAll(makeTransition(s, symbol));

        return resultingStates.stream().toList();
    }

    public List<State> makeTransition(State state, String symbol) {
        return state.getNextStates(symbol.charAt(0));
    }

    private void loadEpsilonNFA(String regex) {
        RegexStack stack = new RegexStack(regex);
        alphabet = stack.getAlphabet();

        // starting state is implicitly defined as the first state in the list
        // final state is implicitly defined as the first state in the list

        List<Map<String, List<Integer>>> transitions = new ArrayList<>();
        Map<String, List<Integer>> currentNFAState = new HashMap<>();
        int stateCount = 0;

        while (!stack.getStack().isEmpty()) {
            if (currentNFAState == null) currentNFAState = new HashMap<>();
            RegexState s = stack.getStack().pollLast();
            String symbol = s.type.fromSymbol(s.symbol);

            // add empty transition to state itself
            addTransition(String.valueOf(epsilon), currentNFAState, stateCount);

            switch (s.frequency) {
                case EXACTLY_ONE -> {
                    // add a transition to the next state
                    addTransition(symbol, currentNFAState, ++stateCount);
                    transitions.add(currentNFAState);
                    currentNFAState = null;
                }

                case NONE_OR_MORE -> {
                    // add transition to state itself
                    addTransition(symbol, currentNFAState, stateCount);

                    RegexState nextState = stack.getStack().peekLast();
                    if (nextState != null && nextState.frequency == SymbolFrequency.NONE_OR_MORE) {
                        // add epsilon transition to the next state
                        addTransition(String.valueOf(epsilon), currentNFAState, ++stateCount);
                        transitions.add(currentNFAState);

                        currentNFAState = null;
                        hasEpsilons = true;
                    }
                }

                case NONE_OR_ONE -> {
                    // add an epsilon transition and a constant transition to the next state
                    stateCount++;
                    addTransition(String.valueOf(epsilon), currentNFAState, stateCount);
                    addTransition(symbol, currentNFAState, stateCount);
                    transitions.add(currentNFAState);

                    currentNFAState = null;
                    hasEpsilons = true;
                }

                default -> {
                    throw new IllegalStateException("Not implemented yet! (" + s.frequency + ", " + s.type + ")");
                }
            }
        }

        // add last state if it hasn't happened yet
        if (currentNFAState != null) transitions.add(currentNFAState);

        // add a missing last state with no transitions if necessary
        if (transitions.size() == stateCount) {
            Map<String, List<Integer>> lastState = new HashMap<>();
            addTransition(String.valueOf(epsilon), lastState, stateCount);
            transitions.add(lastState);
        }

        // convert the transition table to a list of states
        for (int i = 0; i < transitions.size(); i++) {
            states.add(new State(String.valueOf(i), String.join("", alphabet)));
        }

        for (int i = 0; i < transitions.size(); i++) {
            var transition = transitions.get(i);
            State currentState = states.get(i);
            for (String symbol: transition.keySet()) {
                List<Integer> stateIndices = transition.get(symbol);
                for (int idx: stateIndices) currentState.addTransition(symbol.charAt(0), states.get(idx));
            }
        }
    }

    public List<String> getAlphabet() {
        return alphabet;
    }

    public List<State> getStates() {
        return states;
    }

    public List<State> getInitialStates() {
        return List.of(states.get(0));
    }

    public List<State> getFinalStates() {
        return List.of(states.get(states.size() - 1));
    }
}
