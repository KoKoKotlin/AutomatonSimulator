package me.kokokotlin.main.engine.regex;

import me.kokokotlin.main.engine.Automaton;
import me.kokokotlin.main.engine.State;

import java.util.*;
import java.util.stream.Collectors;

// Idea: create an Epsilon-NFA from the regular expression and then
// construct an equivalent NFA and then DFA
// Supported symbols:
// wildcard: .
// Kleene-Star: *
// Kleene-Plus: +
// Optional: ?
public class RegularExpressionLoader {


    // generate the transition table delta'' for the DFA
    // this can introduce new states
    // the initial states are grouped into one state
    // every state that contains at least one final state is a new final state
    // at the end of the conversion, every state needs a transition for every symbol in the alphabet
    // and there can only be one initial state
    // there can also be states that are not reachable. They can be safely removed without changing the behavior of the automaton
    private static Automaton constructDFA(NFA nfa) {
        String assembledAlphabet = String.join("", nfa.getAlphabet());

        List<List<Integer>> states = new ArrayList<>();     // symbolic states as sets to keep track of the state indices
        List<State> dfaStates = new ArrayList<>();          // actual dfa states

        // keeping track of states that can actually be reached
        // this is needed for removing the not reachable states
        Set<Integer> reachableStates = new HashSet<>();

        for (int i = 0; i < nfa.getStateCount(); i++) {
            states.add(List.of(i));
            dfaStates.add(new State(String.valueOf(i), assembledAlphabet, false));
        }
        int currentStateIndex = 0;

        while (currentStateIndex < states.size()) {
            List<Integer> currentState = states.get(currentStateIndex);
            State currentDfaState = dfaStates.get(currentStateIndex);

            for (String symbol: nfa.getAlphabet()) {
                List<State> resultState_ = nfa.makeTransitionIdx(currentState, symbol);
                List<Integer> resultState = resultState_.stream().map(nfa::getStateIndex).collect(Collectors.toList());

                if (!states.contains(resultState)) {
                    states.add(resultState);
                    String stateName;
                    if (resultState.size() == 0) stateName = "∅";
                    else stateName = resultState.stream().map(String::valueOf).collect(Collectors.joining(""));

                    dfaStates.add(new State(stateName, assembledAlphabet, false));
                }

                int index = states.indexOf(resultState);
                reachableStates.add(index);
                currentDfaState.addTransition(symbol.charAt(0), dfaStates.get(index));
            }

            currentStateIndex++;
        }

        // TODO: remove the unreachable states

        // get final states
        List<Integer> finalStates = new ArrayList<>();
        final int finalStateIndex = nfa.getStateCount() - 1;

        for (int i = 0; i < states.size(); i++) {
            List<Integer> state = states.get(i);
            if (state.contains(finalStateIndex)) finalStates.add(i);
        }

        Integer[] finalStateArray = new Integer[finalStates.size()];
        finalStates.toArray(finalStateArray);


        State[] stateArray = new State[dfaStates.size()];
        dfaStates.toArray(stateArray);

        int startingStateIndex = -1;
        for (int i = 0; i < states.size(); i++) {
            var state = states.get(i);
            if (nfa.getInitialStatesIdx().size() == state.size() && nfa.getInitialStatesIdx().containsAll(state)) {
                startingStateIndex = i;
                break;
            }
        }

        return new Automaton(stateArray, startingStateIndex, finalStateArray, assembledAlphabet);
    }

    public static Automaton loadFromRegex(String regex) {
        EpsilonNFA eNFA = new EpsilonNFA(regex);
        NFA nfa = new NFA(eNFA);
        return constructDFA(nfa);
    }
}
