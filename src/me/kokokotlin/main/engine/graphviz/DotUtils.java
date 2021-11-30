package me.kokokotlin.main.engine.graphviz;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.List;

import me.kokokotlin.main.engine.State;
import me.kokokotlin.main.engine.Symbol;

public class DotUtils {
    public static String getStartStateName(int index) {
        return String.format("__start%d__", index);
    }

    public static void writeTransitions(StringBuilder sink, List<State> states) {
        for (State state: states) {
            // group transitions be destination state
            Map<State, Set<Symbol>> groupedTransitionChars = new HashMap<>();
            for (var entry: state.getTransition().entrySet()) {
                Symbol symbol = entry.getKey();
                List<State> destStates = entry.getValue();

                for (State s: destStates) {
                    Set<Symbol> transitionSymbols = groupedTransitionChars.getOrDefault(s, new HashSet<>());
                    transitionSymbols.add(symbol);
                    groupedTransitionChars.put(s, transitionSymbols);
                }
            }

            // write all characters to the same state as a label on the same arrow to make the image more clean
            for (var entry: groupedTransitionChars.entrySet()) {
                State destState = entry.getKey();
                String symbols = entry.getValue().stream().map(s -> s.dotRepr()).collect(Collectors.joining(","));

                sink.append("\t");
                sink.append(state.getName());
                sink.append(" -> ");
                sink.append(destState.getName());
                sink.append(String.format(" [label = \" %s\"]", symbols));
                sink.append("\n");
            }
        }
    }

    public static void writeInitialAndFinalStates(StringBuilder sink, List<State> initials, List<State> finals) {

        for(int i = 0; i < initials.size(); i++) {
            sink.append("\t");
            sink.append(getStartStateName(i));
            sink.append(" [shape = point];\n");
        }

        for (State s: finals) {
            sink.append("\t");
            sink.append(s.getName());
            sink.append(" [shape=doublecircle]\n");
        }

        for(int i = 0; i < initials.size(); i++) {
            State s = initials.get(i);

            sink.append("\tnode [shape = circle];\n\t");
            sink.append(getStartStateName(i));
            sink.append(" -> ");
            sink.append(s.getName());
            sink.append("\n");
        }
    }
}
