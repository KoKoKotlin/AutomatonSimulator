package me.kokokotlin.main.engine.regex;

import java.util.*;

public class RegexStack {
    private final Deque<RegexState> stack = new ArrayDeque<>();
    private final Set<String> alphabet = new HashSet<>();

    public RegexStack(String regex) {
        buildStack(regex);
    }

    private void buildStack(String regex) {
        final String[] symbols = regex.split("");

        Integer errorIndex = null;

        for (int i = 0; i < symbols.length; i++) {
            String symbol = symbols[i];

            switch (symbol) {
                case "*" -> {
                    RegexState s = stack.poll();
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
                    RegexState s = stack.poll();
                    if (s == null) {
                        errorIndex = i;
                        break;
                    }

                    s.frequency = SymbolFrequency.NONE_OR_ONE;
                    stack.push(s);
                }
                case "." -> {
                    stack.push(RegexState.wildcard());
                    alphabet.add("\0");
                }
                default -> {
                    stack.push(new RegexState(symbol));
                    alphabet.add(symbol);
                }
            }
        }

        if (errorIndex != null) {
            throw new IllegalArgumentException(String.format("Syntax error in Regex at %d!", errorIndex));
        }
    }

    public Deque<RegexState> getStack() {
        return stack;
    }

    public List<String> getAlphabet() {
        return alphabet.stream().toList();
    }
}
