package me.kokokotlin.main.engine.regex;

import me.kokokotlin.main.engine.Automaton;
import me.kokokotlin.main.utils.Tuple;

import java.util.*;

public class RegularExpressionLoader {

    private static void addTransition(String symbol, Map<String, List<Integer>> nfaState, Integer destStateIndex) {
        List<Integer> transition = nfaState.getOrDefault(symbol, null);
        if (transition == null) transition = new ArrayList<>();

        transition.add(destStateIndex);
        nfaState.put(symbol, transition);
    }

    // Idea: create an Epsilon-NFA from the regular expression and then
    // construct an equivalent NFA and then DFA
    // Supported symbols:
    // wildcard: .
    // Kleene-Star: *
    // Kleene-Plus: +
    // Optional: ?
    private static Tuple<List<Map<String, List<Integer>>>, Boolean> loadEpsilonNFA(String regex) {
        RegexStack stack = new RegexStack(regex);
        RegexState[] states = new RegexState[stack.getStack().size()];
        stack.getStack().toArray(states);

        // starting state is implicitly defined as the first state in the list
        // final state is implicitly defined as the first state in the list
        List<Map<String, List<Integer>>> nfaStates = new ArrayList<>();
        Map<String, List<Integer>> currentNFAState = new HashMap<>();
        int stateCount = 0;
        boolean epsilonTransitionsExist = false;

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
                    nfaStates.add(currentNFAState);
                    currentNFAState = null;
                }

                case NONE_OR_MORE -> {
                    // add transition to state itself
                    addTransition(symbol, currentNFAState, stateCount);

                    RegexState nextState = stack.getStack().peekLast();
                    if (nextState != null && nextState.frequency == SymbolFrequency.NONE_OR_MORE) {
                        // add epsilon transition to the next state
                        addTransition("", currentNFAState, ++stateCount);
                        nfaStates.add(currentNFAState);

                        currentNFAState = null;
                        epsilonTransitionsExist = true;
                    }
                }

                case NONE_OR_ONE -> {
                    // add an epsilon transition and a constant transition to the next state
                    stateCount++;
                    addTransition("", currentNFAState, stateCount);
                    addTransition(symbol, currentNFAState, stateCount);
                    nfaStates.add(currentNFAState);

                    currentNFAState = null;
                    epsilonTransitionsExist = true;
                }

                default -> {
                    throw new IllegalStateException("Not implemented yet! (" + s.frequency + ", " + s.type + ")");
                }
            }
        }

        // add last state if it hasn't happened yet
        if (currentNFAState != null) nfaStates.add(currentNFAState);

        // add a missing last state with no transitions if necessary
        if (nfaStates.size() == stateCount) {
            Map<String, List<Integer>> lastState = new HashMap<>();
            addTransition("", lastState, stateCount);
            nfaStates.add(lastState);
        }

        return new Tuple<>(nfaStates, epsilonTransitionsExist);
    }

    private static Automaton constructNFA(String regex) { return null; }

    private static Automaton constructDFA(Automaton NFA) {
        return null;
    }

    public static void loadFromRegex(String regex) {
        loadEpsilonNFA(regex);
    }
}
