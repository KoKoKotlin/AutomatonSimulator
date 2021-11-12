package me.kokokotlin.main;

import me.kokokotlin.main.utils.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class Transition {
    private final Map<String, Map<Character, String>> transitions = new HashMap<>();

    public Transition(String[] states) {
        for (String state: states)
            transitions.put(state, new HashMap<>());
    }

    public void addTransition(String startingState, Character symbol, String finalState) {
        Map<Character, String> stateMap = transitions.get(startingState);
        if (stateMap.containsKey(symbol)) {
            System.err.printf("WARNING: Overriding transition from state %s with symbol (%s), current final state: %s, new final state: %s.\n",
                    startingState, symbol, finalState, stateMap.get(symbol));
        }
        stateMap.put(symbol, finalState);
    }

    public Optional<String> getNextState(String startState, char symbol) {
        Map<Character, String> stateMap = transitions.get(startState);
        if (stateMap.containsKey(symbol)) return Optional.of(stateMap.get(symbol));
        else return Optional.empty();
    }

    public String getStringRepr() {
        List<String> reprBuilder = new LinkedList<>();
        reprBuilder.add("");

        for (String key: transitions.keySet()) {
            Map<Character, String> keyMap = transitions.get(key);

            for (Character symbol: keyMap.keySet()) {
                reprBuilder.add(String.format("(%s, %c) -> %s", key, symbol, keyMap.get(symbol)));
            }
        }

        return String.join("\n", reprBuilder);
    }

    public List<Tuple<String, Set<Character>>> getNotSaturatedStates(String alphabet) {
        Set<Character> symbolSet = Arrays.stream(alphabet.split(""))
                .map(s -> s.charAt(0))
                .collect(Collectors.toSet());

        return transitions.keySet().stream()
                .map(key -> new Tuple<>(key, transitions.get(key).keySet()))
                .filter(tuple -> !tuple.getSecond().equals(symbolSet) )
                .collect(Collectors.toList());
    }

    /*
    * Checks if every state has one and only one transition for every symbol in the alphabet.
    */
    public boolean isComplete(String alphabet) {
        return getNotSaturatedStates(alphabet).isEmpty();
    }
}
