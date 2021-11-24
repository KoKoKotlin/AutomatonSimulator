package me.kokokotlin.main.engine;

import java.util.*;
import java.util.stream.Collectors;

public class State {
    private final String name;
    private final Map<Character, List<State>> transition;
    private final String alphabet;

    public State(String name, String alphabet) {
        this.name = name;
        transition = new HashMap<>();

        for(int i = 0; i < alphabet.length(); i++) {
            transition.put(alphabet.charAt(i), new ArrayList<>());
        }

        this.alphabet = alphabet;
    }

    public String missingChars() {
        return Arrays.stream(alphabet.split(""))
                .filter(c -> transition.get(c.charAt(0)) == null)
                .collect(Collectors.joining());
    }

    public void addTransition(Character symbol, State destState) {
        transition.get(symbol).add(destState);
    }

    public boolean hasUniqueTransitions() {
        return true;
    }

    public List<State> getNextStates(Character inputSymbol) { return transition.get(inputSymbol); }

    public String getName() {
        return name;
    }

    public Map<Character, List<State>> getTransition() {
        return transition;
    }
}
