package me.kokokotlin.main.engine.regex;

import me.kokokotlin.main.engine.DFA;
import me.kokokotlin.main.engine.ENFA;

// Idea: create an Epsilon-NFA from the regular expression and then
// construct an equivalent NFA and then DFA
// Supported symbols:
// wildcard: .
// Kleene-Star: *
// Kleene-Plus: +
// Optional: ?
public class RegularExpressionLoader {
    public static DFA loadFromRegex(String regex) {
        return ENFA.fromRegex(regex).toDFA();
    }
}
