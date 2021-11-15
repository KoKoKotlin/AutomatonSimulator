package me.kokokotlin.main.engine;

import java.util.ArrayDeque;
import java.util.Deque;

public class RegularExpressionLoader {

    private enum SymbolFrequency {
        EXACTLY_ONE,
        NONE_OR_ONE,
        NONE_OR_MORE,
    }

    private enum Type {
        WILDCARD,
        CONSTANT,
    }

    private static class RegexState {
        String symbol;
        SymbolFrequency frequency;
        Type type;

        public RegexState(String symbol) {
            this.symbol = symbol;
            type = Type.CONSTANT;
            frequency = SymbolFrequency.EXACTLY_ONE;
        }

        public static RegexState wildcard() {
            RegexState r = new RegexState("");
            r.type = Type.WILDCARD;
            return r;
        }

        @Override
        public String toString() {
            return "RegexState{" +
                    "symbol='" + symbol + '\'' +
                    ", frequency=" + frequency +
                    ", type=" + type +
                    '}';
        }
    }

    // Idea: create a NFA from the regular expression and then
    // construct an equivalent DFA from the NFA

    // Supported symbols:
    // wildcard: .
    // Kleene-Star: *
    // Kleene-Plus: +
    // Optional: ?
    private static Automaton loadNFA(String regex) {
        final Deque<RegexState> stack = new ArrayDeque<>();
        final String[] symbols = regex.split("");

        Integer errorIndex = null;

        for (int i = 0; i < symbols.length; i++) {
            String symbol = symbols[i];

            switch (symbol) {
                case "*" -> {
                    RegexState s = stack.pollLast();
                    if (s == null) {
                        errorIndex = i;
                        break;
                    }

                    s.frequency = SymbolFrequency.NONE_OR_MORE;
                    stack.push(s);
                }
                case "+" -> {
                    RegexState s = stack.pollLast();
                    if (s == null) {
                        errorIndex = i;
                        break;
                    }
                    s.frequency = SymbolFrequency.NONE_OR_MORE;
                    // + == one + none_or_more
                    stack.push(new RegexState(s.symbol));
                    stack.push(s);
                }
                case "?" -> {
                    RegexState s = stack.pollLast();
                    if (s == null) {
                        errorIndex = i;
                        break;
                    }

                    s.frequency = SymbolFrequency.NONE_OR_ONE;
                    stack.push(s);
                }
                case "." -> {
                    stack.push(RegexState.wildcard());
                }
                default -> {
                    stack.push(new RegexState(symbol));
                }
            }
        }

        if (errorIndex != null) {
            throw new IllegalArgumentException(String.format("Syntax error in Regex at %d!", errorIndex));
        }

        for (var f: stack) {
            System.out.println(f);
        }

        return null;
    }

    private static Automaton constructDFA(Automaton NFA) {
        return null;
    }

    public static void loadFromRegex(String regex) {
        Automaton NFA = loadNFA(regex);
    }
}
