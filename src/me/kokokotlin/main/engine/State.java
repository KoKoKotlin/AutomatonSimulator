package me.kokokotlin.main.engine;

import me.kokokotlin.main.engine.regex.EpsilonNFA;

import java.util.*;
import java.util.stream.Collectors;

public class State {
    private final String name;
    private final Map<Character, List<State>> transition;
    private final String alphabet;

    public State(String name, String alphabet, boolean needsEpsilon) {
        this.name = name;
        transition = new HashMap<>();

        if (needsEpsilon)
            transition.put(EpsilonNFA.EPSILON, new ArrayList<>());

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
        return transition.entrySet().stream().allMatch(entry -> entry.getValue().size() == 1);
    }

    public List<State> getNextStates(Character inputSymbol) { return transition.get(inputSymbol); }

    public String getName() {
        return name;
    }

    public Map<Character, List<State>> getTransition() {
        return transition;
    }

    private String transitionToString() {
        return transition.entrySet().stream().map(entry -> {
            Character c = entry.getKey();
            List<State> val = entry.getValue();

            return String.format("%c ↦ { %s }", c, val.stream().map(s -> s.name).collect(Collectors.joining(", ")));
        }).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "State{" +
                "name='" + name + '\'' +
                ", transition=(" + transitionToString() +
                "), alphabet='" + alphabet + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return Objects.equals(name, state.name) && Objects.equals(transition, state.transition) && Objects.equals(alphabet, state.alphabet);
    }
}
