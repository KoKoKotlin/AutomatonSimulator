package me.kokokotlin.main.engine;

import java.util.*;
import java.util.stream.Collectors;

public class State {
    private final String name;
    private final Map<Symbol, List<State>> transition;
    private final List<String> alphabet;

    public State(String name, List<String> alphabet, boolean needsEpsilon) {
        this.name = name;
        transition = new HashMap<>();

        if (needsEpsilon)
            transition.put(Symbol.epsilon(), new ArrayList<>());

        for(int i = 0; i < alphabet.size(); i++) {
            transition.put(new Symbol(alphabet.get(i)), new ArrayList<>());
        }

        this.alphabet = alphabet;
    }

    // TODO: refactoring for symbol 
    public String missingChars() {
        /*
        return alphabet.stream()
                .filter(c -> transition.get(c) == null)
                .collect(Collectors.joining());
        */
        return "";
    }

    public void addTransition(Symbol symbol, State destState) {
        transition.get(symbol).add(destState);
    }

    public boolean hasUniqueTransitions() {
        return transition.entrySet().stream().allMatch(entry -> entry.getValue().size() == 1);
    }

    public List<State> getNextStates(Symbol inputSymbol) { return transition.get(inputSymbol); }

    public String getName() {
        return name;
    }

    public Map<Symbol, List<State>> getTransition() {
        return transition;
    }

    private String transitionToString() {
        return transition.entrySet().stream().map(entry -> {
            Symbol c = entry.getKey();
            List<State> val = entry.getValue();

            return String.format("%s ↦ { %s }", c, val.stream().map(s -> s.name).collect(Collectors.joining(", ")));
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
