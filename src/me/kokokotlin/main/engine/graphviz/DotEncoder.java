package me.kokokotlin.main.engine.graphviz;

import me.kokokotlin.main.engine.Automaton;
import me.kokokotlin.main.engine.State;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DotEncoder {

    public static void automatonToDotfile(Automaton automaton, Path outputPath) {
        StringBuilder dotRepr = new StringBuilder();

        dotRepr.append("digraph {\n");
        dotRepr.append("\trankdir=LR\n");
        dotRepr.append("\txxxxxxxxxxxxxxxxxxxxxxxxxx [shape = point];\n");

        for (State s: automaton.getFinalStates()) {
            dotRepr.append("\t");
            dotRepr.append(s.getName());
            dotRepr.append(" [shape=doublecircle]\n");
        }

        dotRepr.append("\tnode [shape = circle];\n");
        dotRepr.append("\txxxxxxxxxxxxxxxxxxxxxxxxxx -> ");
        dotRepr.append(automaton.getInitialState().getName());
        dotRepr.append("\n");

        for (State state: automaton.getStates()) {
            for (var entry: state.getTransition().entrySet()) {
                dotRepr.append("\t");
                dotRepr.append(state.getName());
                dotRepr.append(" -> ");
                dotRepr.append(entry.getValue().getName());
                dotRepr.append(String.format(" [label = \" %c\"]", entry.getKey()));
                dotRepr.append("\n");
            }
        }

        dotRepr.append("}\n");

        try {
            BufferedWriter bWriter = Files.newBufferedWriter(outputPath);
            bWriter.write(dotRepr.toString());
            bWriter.flush();
        } catch (IOException e) {
            System.err.printf("Couldn't create output dot file! %s\n", e.getMessage());
        }
    }
}
