package me.kokokotlin.main.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class State {
    private final String name;
    private final Map<Character, State> transition;
    private final String alphabet;

    public State(String name, String alphabet) {
        this.name = name;
        transition = new HashMap<>();

        for(int i = 0; i < alphabet.length(); i++) {
            transition.put(alphabet.charAt(i), null);
        }

        this.alphabet = alphabet;
    }

    public String missingChars() {
        return Arrays.stream(alphabet.split(""))
                .filter(c -> transition.get(c.charAt(0)) == null)
                .collect(Collectors.joining());
    }

    public boolean addTransition(Character symbol, State destState) {
        boolean override = transition.get(symbol) != null;

        transition.put(symbol, destState);
        return override;
    }

    public State getNextState(Character inputSymbol) {
        return transition.get(inputSymbol);
    }

    public String getName() {
        return name;
    }

    public Map<Character, State> getTransition() {
        return transition;
    }
}
