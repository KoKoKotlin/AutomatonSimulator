package me.kokokotlin.main.engine.regex;

import me.kokokotlin.main.engine.State;

import java.util.*;
import java.util.stream.Collectors;

public class NFA {

    // states names are implicitly defined as the indices of the list
    // the last state is the final state

    private final List<State> initialStates = new ArrayList<>();
    private final List<String> alphabet;
    private final List<State> states = new ArrayList<>();

    public NFA(EpsilonNFA eNFA) {
        alphabet = eNFA.getAlphabet();
        for (int i = 0; i < eNFA.getStates().size(); i++) states.add(new State(String.valueOf(i), String.join("", getAlphabet())));
        constructFromENFA(eNFA);
        // initialStates = eNFA.getEpsilonClojure();
    }

    // Converting epsilon-NFA to NFA Formula: delta'(q, sigma) = e-Cl(delta(e-Cl(q), sigma))
    // e-Cl(q): epsilon closure of q, delta: transition relation of epsilon-NFA, delta': transition relation of NFA
    // q: state, sigma: input symbol from the alphabet
    private void constructFromENFA(EpsilonNFA eNFA) {
        // construct a new transition table without epsilon transitions
        // this can introduce and initial states
        // the states could be a set of integers, so we need to keep track of them in a separate list
        for (int i = 0; i < eNFA.getStates().size(); i++) {
            State currentState = states.get(i);
            List<State> currentStateSet = List.of(states.get(i));

            for (String symbol: eNFA.getAlphabet()) {
                // Formula from above
                List<State> resultingStates = eNFA.getEpsilonClojure(eNFA.makeTransition(eNFA.getEpsilonClojure(currentStateSet), symbol));
                for (State s: resultingStates) {
                    currentState.addTransition(symbol.charAt(0), s);
                }
            }
        }
    }

    public List<State> makeTransitionIdx(List<Integer> statesIndices, String symbol) {
        List<State> states_ = statesIndices.stream().map(states::get).collect(Collectors.toList());
        return makeTransition(states_, symbol);
    }

    public List<State> makeTransition(List<State> states, String symbol) {
        Set<State> resultingStates = new HashSet<>();
        for (State s: states) resultingStates.addAll(makeTransition(s, symbol));

        return resultingStates.stream().toList();
    }

    public List<State> makeTransition(State state, String symbol) {
        return state.getNextStates(symbol.charAt(0));
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
}
