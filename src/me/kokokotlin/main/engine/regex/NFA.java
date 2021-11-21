package me.kokokotlin.main.engine.regex;

import java.util.*;

public class NFA {

    // states names are implicitly defined as the indices of the list
    // the last state is the final state
    private final List<Integer> initialStates;
    private final List<Map<String, List<Integer>>> transitions = new ArrayList<>();
    private final List<String> alphabet;

    public NFA(EpsilonNFA eNFA) {
        initialStates = eNFA.getEpsilonClojure(0);
        constructFromENFA(eNFA);
        alphabet = eNFA.getAlphabet();
    }

    // Converting epsilon-NFA to NFA Formula: delta'(q, sigma) = e-Cl(delta(e-Cl(q), sigma))
    // e-Cl(q): epsilon closure of q, delta: transition relation of epsilon-NFA, delta': transition relation of NFA
    // q: state, sigma: input symbol from the alphabet
    private void constructFromENFA(EpsilonNFA eNFA) {
        // construct a new transition table without epsilon transitions
        // this can introduce and initial states
        // the states could be a set of integers, so we need to keep track of them in a separate list
        for (int i = 0; i < eNFA.transitions.size(); i++) {
            List<Integer> currentStateSet = List.of(i);
            Map<String, List<Integer>> currentTransition = new HashMap<>();
            for (String symbol: eNFA.getAlphabet()) {
                // Formula from above
                List<Integer> resultingStates = eNFA.getEpsilonClojure(eNFA.makeTransition(eNFA.getEpsilonClojure(currentStateSet), symbol));
                currentTransition.put(symbol, resultingStates);
            }

            transitions.add(currentTransition);
        }
    }

    public List<Integer> makeTransition(List<Integer> states, String symbol) {
        Set<Integer> resultingStates = new HashSet<>();
        for (Integer i: states) resultingStates.addAll(makeTransition(i, symbol));

        return resultingStates.stream().toList();
    }

    public List<Integer> makeTransition(Integer state, String symbol) {
        return transitions.get(state).getOrDefault(symbol, new ArrayList<>());
    }

    public List<Integer> getInitialStates() {
        return initialStates;
    }

    public int getStateCount() {
        return transitions.size();
    }

    public List<String> getAlphabet() {
        return alphabet;
    }
}
