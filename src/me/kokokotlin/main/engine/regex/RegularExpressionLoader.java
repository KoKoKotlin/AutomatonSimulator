package me.kokokotlin.main.engine.regex;

import me.kokokotlin.main.engine.Automaton;
import me.kokokotlin.main.utils.Tuple;

import java.util.*;

// Idea: create an Epsilon-NFA from the regular expression and then
// construct an equivalent NFA and then DFA
// Supported symbols:
// wildcard: .
// Kleene-Star: *
// Kleene-Plus: +
// Optional: ?
public class RegularExpressionLoader {

    // Converting epsilon-NFA to NFA Formula: delta'(q, sigma) = e-Cl(delta(e-Cl(q), sigma))
    // e-Cl(q): epsilon closure of q, delta: transition relation of epsilon-NFA, delta': transition relation of NFA
    // q: state, sigma: input symbol from the alphabet
    private static Automaton constructNFA(List<Map<String, List<Integer>>> epsilonNFAStates, boolean containsEpsilons) {

        return null;
    }

    private static Automaton constructDFA(Automaton NFA) {
        return null;
    }

    public static void loadFromRegex(String regex) {
        EpsilonNFA eNFA = new EpsilonNFA(regex);
    }
}
