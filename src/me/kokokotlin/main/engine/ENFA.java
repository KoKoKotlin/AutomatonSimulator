package me.kokokotlin.main.engine;

import java.util.*;
import java.util.stream.Collectors;

import me.kokokotlin.main.engine.regex.RegexStack;
import me.kokokotlin.main.engine.regex.RegexState;
import me.kokokotlin.main.engine.regex.SymbolFrequency;

public class ENFA extends AutomatonBase {

    private boolean hasEpsilons = false;

    public ENFA(List<State> states, List<State> initialStates, List<State> finalStates, List<String> alphabet, boolean hasEpsilons) {
        super(states, initialStates, finalStates, alphabet);
        this.hasEpsilons = hasEpsilons;
    }

    public List<Integer> getEpsilonClojureIdx(List<Integer> stateIndices) {
        List<State> epCl = getEpsilonClojure(stateIndices.stream().map(states::get).collect(Collectors.toList()));
        return epCl.stream().map(states::indexOf).collect(Collectors.toList());
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

            List<State> reachableStates = current.getNextStates(Symbol.epsilon());

            for (State s: reachableStates) {
                if (!epsilonCl.contains(s)) {
                    epsilonCl.add(s);
                    nextStates.add(s);
                }
            }
        }

        return epsilonCl;
    }

    public List<Integer> makeTransitionIdx(List<Integer> stateIndices, Symbol symbol) {
        List<State> transition = makeTransition(stateIndices.stream().map(states::get).collect(Collectors.toList()), symbol);
        return transition.stream().map(states::indexOf).collect(Collectors.toList());
    }

    public List<State> makeTransition(List<State> states, Symbol symbol) {
        Set<State> resultingStates = new HashSet<>();
        for (State s: states) resultingStates.addAll(makeTransition(s, symbol));

        return resultingStates.stream().toList();
    }

    public List<State> makeTransition(State state, Symbol symbol) {
        return state.getNextStates(symbol);
    }


    private static void addTransition(Symbol symbol, Map<Symbol, List<Integer>> nfaState, Integer destStateIndex) {
        List<Integer> transition = nfaState.getOrDefault(symbol, null);
        if (transition == null) transition = new ArrayList<>();

        transition.add(destStateIndex);
        nfaState.put(symbol, transition);
    }

    public static ENFA fromRegex(String regex) {
        RegexStack stack = new RegexStack(regex);
        List<String> alphabet = stack.getAlphabet();

        // starting state is implicitly defined as the first state in the list
        // final state is implicitly defined as the first state in the list
        List<State> states = new ArrayList<>();
        List<Map<Symbol, List<Integer>>> transitions = new ArrayList<>();
        Map<Symbol, List<Integer>> currentNFAState = new HashMap<>();
        int stateCount = 0;

        boolean hasEpsilons = false;

        while (!stack.getStack().isEmpty()) {
            if (currentNFAState == null) currentNFAState = new HashMap<>();
            RegexState s = stack.getStack().pollLast();
            Symbol symbol = s.toSymbol();

            // add empty transition to state itself
            addTransition(Symbol.epsilon(), currentNFAState, stateCount);

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
                        addTransition(Symbol.epsilon(), currentNFAState, ++stateCount);
                        transitions.add(currentNFAState);

                        currentNFAState = null;
                        hasEpsilons = true;
                    }
                }

                case NONE_OR_ONE -> {
                    // add an epsilon transition and a constant transition to the next state
                    stateCount++;
                    addTransition(Symbol.epsilon(), currentNFAState, stateCount);
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
            Map<Symbol, List<Integer>> lastState = new HashMap<>();
            addTransition(Symbol.epsilon(), lastState, stateCount);
            transitions.add(lastState);
        }

        // convert the transition table to a list of states
        for (int i = 0; i < transitions.size(); i++) {
            states.add(new State(String.valueOf(i), alphabet, true));
        }

        for (int i = 0; i < transitions.size(); i++) {
            var transition = transitions.get(i);
            State currentState = states.get(i);
            for (Symbol symbol: transition.keySet()) {
                List<Integer> stateIndices = transition.get(symbol);
                for (int idx: stateIndices) currentState.addTransition(symbol, states.get(idx));
            }
        }

        return new ENFA(states, List.of(states.get(0)), List.of(states.get(states.size() - 1)), alphabet, hasEpsilons);
    }

    // idea: like nfa only that after the transition the next states will be the epsilon clojure of the current states
    // and also you have to check that after the last iteration the state is contained in the epsilon clojure of final states
    // and you start the algorithm with the epsilon clojure of the initial states
    @Override
    public boolean match(String word) {
        List<State> currentStates = getEpsilonClojure(getInitialStates());
        
        for (String symbol: word.split("")) {
            currentStates = getEpsilonClojure(makeTransition(currentStates, new Symbol(symbol)));
        }

        final List<State> finalEpsilonClojure = getEpsilonClojure(getFinalStates());
        return currentStates.stream().anyMatch(s -> finalEpsilonClojure.contains(s));
    }

    @Override
    public DFA toDFA() {
        return NFA.constructFromENFA(this).toDFA();
    }

    @Override
    public NFA toNFA() {
        return NFA.constructFromENFA(this);
    }

    @Override
    public ENFA toENFA() {
        return this;
    }

    public boolean hasEpsilons() {
        return hasEpsilons;
    }
    
}
