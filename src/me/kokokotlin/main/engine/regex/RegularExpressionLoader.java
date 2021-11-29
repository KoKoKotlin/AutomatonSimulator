package me.kokokotlin.main.engine.regex;

import me.kokokotlin.main.engine.DFA;
import me.kokokotlin.main.engine.ENFA;
import me.kokokotlin.main.engine.NFA;
import me.kokokotlin.main.engine.State;
import me.kokokotlin.main.engine.Symbol;

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


    // remove not reachable states
    // that has to be done multiple times, because after the every deletion round there could be new unreachable states
    // that were reachable only be states, that were unreachable and are now deleted
    private static List<State> removeUnreachableStates(List<State> states, State initialState) {
        int lastStateCount = 0;
        int currentStateCount = states.size();

        while (lastStateCount != currentStateCount) {
            final Set<State> reachableStates = new HashSet<>();

            for (State s: states) {
                for(List<State> sReachable: s.getTransition().values()) {
                    List<State> copy = new ArrayList<> (sReachable);
                    copy.remove(s);
                    reachableStates.addAll(copy);
                }
            }

            states = states.stream().filter(state -> reachableStates.contains(state) || state.equals(initialState)).collect(Collectors.toList());
            lastStateCount = currentStateCount;
            currentStateCount = states.size();
        }

        return states;
    }

    // generate the transition table delta'' for the DFA
    // this can introduce new states
    // the initial states are grouped into one state
    // every state that contains at least one final state is a new final state
    // at the end of the conversion, every state needs a transition for every symbol in the alphabet
    // and there can only be one initial state
    // there can also be states that are not reachable. They can be safely removed without changing the behavior of the automaton
    private static DFA constructDFA(NFA nfa) {
        List<List<Integer>> states = new ArrayList<>();     // symbolic states as sets to keep track of the state indices
        List<State> dfaStates = new ArrayList<>();          // actual dfa states

        for (int i = 0; i < nfa.getStateCount(); i++) {
            states.add(List.of(i));
            dfaStates.add(new State(String.valueOf(i), nfa.getAlphabet(), false));
        }
        int currentStateIndex = 0;

        while (currentStateIndex < states.size()) {
            List<Integer> currentState = states.get(currentStateIndex);
            State currentDfaState = dfaStates.get(currentStateIndex);

            for (String symbol: nfa.getAlphabet()) {
                Symbol currentSymbol = new Symbol(symbol);

                List<State> resultState_ = nfa.makeTransitionIdx(currentState, currentSymbol);
                List<Integer> resultState = resultState_.stream().map(nfa::getStateIndex).collect(Collectors.toList());

                if (!states.contains(resultState)) {
                    states.add(resultState);
                    String stateName;
                    if (resultState.size() == 0) stateName = "∅";
                    else stateName = resultState.stream().map(String::valueOf).collect(Collectors.joining(""));

                    dfaStates.add(new State(stateName, nfa.getAlphabet(), false));
                }

                int index = states.indexOf(resultState);
                currentDfaState.addTransition(currentSymbol, dfaStates.get(index));
            }

            currentStateIndex++;
        }

        // get final states
        List<State> finalStates = new ArrayList<>();
        final int finalStateIndex = nfa.getStateCount() - 1;

        for (int i = 0; i < states.size(); i++) {
            List<Integer> state = states.get(i);
            State currentState = dfaStates.get(i);
            if (state.contains(finalStateIndex)) finalStates.add(currentState);
        }

        // find initial state
        int startingStateIndex = -1;
        List<Integer> nfaInitialState = nfa.getInitialStatesIdx();
        for (int i = 0; i < states.size(); i++) {
            var state = states.get(i);
            if (nfaInitialState.size() == state.size() && nfaInitialState.containsAll(state)) {
                startingStateIndex = i;
                break;
            }
        }

        State initialState = dfaStates.get(startingStateIndex);
        dfaStates = removeUnreachableStates(dfaStates, initialState);

        // remove unreachable states from final states
        finalStates = finalStates.stream().filter(dfaStates::contains).collect(Collectors.toList());

        return new DFA(dfaStates, List.of(initialState), finalStates, nfa.getAlphabet());
    }

    public static DFA loadFromRegex(String regex) {
        ENFA eNFA = ENFA.fromRegex(regex);
        NFA nfa = NFA.constructFromENFA(eNFA);
        return constructDFA(nfa);
    }
}
