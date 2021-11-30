package me.kokokotlin.main.engine;

import java.util.*;
import java.util.stream.Collectors;

public class NFA extends AutomatonBase {

    /* public NFA(ENFA eNFA) {
        alphabet = eNFA.getAlphabet();
        for (int i = 0; i < eNFA.getStates().size(); i++) states.add(new State(String.valueOf(i), String.join("", getAlphabet()), false));
        constructFromENFA(eNFA);
    } */ 

    public NFA(List<State> states, List<State> initialStates, List<State> finalStates, List<String> alphabet) {
        super(states, initialStates, finalStates, alphabet);
    }

    // Converting epsilon-NFA to NFA Formula: delta'(q, sigma) = e-Cl(delta(e-Cl(q), sigma))
    // e-Cl(q): epsilon closure of q, delta: transition relation of epsilon-NFA, delta': transition relation of NFA
    // q: state, sigma: input symbol from the alphabet
    public static NFA constructFromENFA(ENFA eNFA) {

        List<State> states = new ArrayList<>();

        // construct a new transition table without epsilon transitions
        // this can introduce and initial states
        // the states could be a set of integers, so we need to keep track of them in a separate list
        for (int i = 0; i < eNFA.getStates().size(); i++) {
            State currentState = states.get(i);

            for (String symbol: eNFA.getAlphabet()) {
                Symbol currentSymbol = new Symbol(symbol);
                // Formula from above
                List<Integer> resultingStates = eNFA.getEpsilonClojureIdx(eNFA.makeTransitionIdx(eNFA.getEpsilonClojureIdx(List.of(i)), currentSymbol));
                for (Integer idx: resultingStates) {
                    currentState.addTransition(currentSymbol, states.get(idx));
                }
            }
        }

        return new NFA(states, eNFA.getEpsilonClojureIdx(List.of(0)).stream().map(states::get).collect(Collectors.toList()), List.of(), eNFA.alphabet);
    }

    public List<State> makeTransitionIdx(List<Integer> statesIndices, Symbol symbol) {
        List<State> states_ = statesIndices.stream().map(states::get).collect(Collectors.toList());
        return makeTransition(states_, symbol);
    }

    public List<State> makeTransition(List<State> states, Symbol symbol) {
        Set<State> resultingStates = new HashSet<>();
        for (State s: states) resultingStates.addAll(makeTransition(s, symbol));

        return resultingStates.stream().toList();
    }

    public List<State> makeTransition(State state, Symbol symbol) {
        return state.getNextStates(symbol);
    }

    @Override
    public boolean match(String word) {
        return false;
    }

    @Override
    public DFA toDFA() {
        return DFA.constructDFA(this);
    }

    @Override
    public NFA toNFA() {
        return this;
    }

    @Override
    public ENFA toENFA() {
        return new ENFA(states, initialStates, finalStates, alphabet, false);
    }

    public List<State> getInitialStates() {
        return initialStates;
    }

    public int getStateCount() {
        return states.size();
    }

    public List<String> getAlphabet() {
        return alphabet;
    }

    public int getStateIndex(State state) {
        return states.indexOf(state);
    }

    public List<State> getFinalStates() {
        return List.of(states.get(states.size() - 1));
    }

    public List<State> getStates() {
        return states;
    }

    public List<Integer> getInitialStatesIdx() {
        return getInitialStates().stream().map(states::indexOf).collect(Collectors.toList());
    }
}
