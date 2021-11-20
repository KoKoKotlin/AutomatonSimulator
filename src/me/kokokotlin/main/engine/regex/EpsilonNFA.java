package me.kokokotlin.main.engine.regex;

import me.kokokotlin.main.utils.Tuple;

import java.util.*;

public class EpsilonNFA {

    // states names are implicitly defined as the indices of the list
    List<Map<String, List<Integer>>> transitions = new ArrayList<>();
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

    public List<Integer> getEpsilonClojure(List<Integer> states) {
        Set<Integer> resultingStates = new HashSet<>();
        for (Integer i: states) resultingStates.addAll(getEpsilonClojure(i));

        return resultingStates.stream().toList();
    }

    public List<Integer> getEpsilonClojure(Integer state) {
        if (state >= transitions.size()) return new ArrayList<>();

        List<Integer> epsilonCl = new ArrayList<>();
        epsilonCl.add(state);  // state is always in its own e-Cl

        // states that still need to be looked at to determine the e-Cl
        Queue<Integer> nextStates = new LinkedList<>();
        nextStates.add(state);

        while (!nextStates.isEmpty()) {
            Integer current = nextStates.poll();

            var currentTransition = transitions.get(current);
            List<Integer> reachableStates = currentTransition.get("");

            for (Integer i: reachableStates) {
                if (!epsilonCl.contains(i)) {
                    epsilonCl.add(i);
                    nextStates.add(i);
                }
            }
        }

        return epsilonCl;
    }

    public List<Integer> makeTransition(List<Integer> states, String symbol) {
        Set<Integer> resultingStates = new HashSet<>();
        for (Integer i: states) resultingStates.addAll(makeTransition(i, symbol));

        return resultingStates.stream().toList();
    }

    public List<Integer> makeTransition(Integer state, String symbol) {
        return transitions.get(state).getOrDefault(symbol, new ArrayList<>());
    }

    private void loadEpsilonNFA(String regex) {
        RegexStack stack = new RegexStack(regex);

        // starting state is implicitly defined as the first state in the list
        // final state is implicitly defined as the first state in the list
        Map<String, List<Integer>> currentNFAState = new HashMap<>();
        int stateCount = 0;

        while (!stack.getStack().isEmpty()) {
            if (currentNFAState == null) currentNFAState = new HashMap<>();
            RegexState s = stack.getStack().pollLast();
            String symbol = s.type.fromSymbol(s.symbol);

            // add empty transition to state itself
            addTransition("", currentNFAState, stateCount);

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
                        addTransition("", currentNFAState, ++stateCount);
                        transitions.add(currentNFAState);

                        currentNFAState = null;
                        hasEpsilons = true;
                    }
                }

                case NONE_OR_ONE -> {
                    // add an epsilon transition and a constant transition to the next state
                    stateCount++;
                    addTransition("", currentNFAState, stateCount);
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
            addTransition("", lastState, stateCount);
            transitions.add(lastState);
        }
    }

}
