package me.kokokotlin.main.engine.graphviz;

import me.kokokotlin.main.engine.Automaton;
import me.kokokotlin.main.engine.State;
import me.kokokotlin.main.engine.regex.EpsilonNFA;
import me.kokokotlin.main.engine.regex.NFA;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DotEncoder {

    private static String getStartStateName(int index) {
        return String.format("__start%d__", index);
    }

    private static String dfaToDot(Automaton automaton) {
        StringBuilder dfaDot = new StringBuilder();

        dfaDot.append("\t");
        dfaDot.append(getStartStateName(0));
        dfaDot.append(" [shape = point];\n");

        for (State s: automaton.getFinalStates()) {
            dfaDot.append("\t");
            dfaDot.append(s.getName());
            dfaDot.append(" [shape=doublecircle]\n");
        }

        dfaDot.append("\tnode [shape = circle];\n\t");
        dfaDot.append(getStartStateName(0));
        dfaDot.append(" -> ");
        dfaDot.append(automaton.getInitialState().getName());
        dfaDot.append("\n");

        for (State state: automaton.getStates()) {
            for (var entry: state.getTransition().entrySet()) {
                dfaDot.append("\t");
                dfaDot.append(state.getName());
                dfaDot.append(" -> ");
                dfaDot.append(entry.getValue().get(0).getName());
                dfaDot.append(String.format(" [label = \" %c\"]", entry.getKey()));
                dfaDot.append("\n");
            }
        }

        return dfaDot.toString();
    }

    private static String toNfaDot(NFA nfa) {
        StringBuilder nfaDot = new StringBuilder();

        nfa.getInitialStates();


        return nfaDot.toString();
    }

    private static String toENfaDot(EpsilonNFA eNFA) {
        StringBuilder eNfaDot = new StringBuilder();



        return eNfaDot.toString();
    }

    public static void automatonToDotfile(Automaton automaton, Path outputPath) {
        String dotRepr = "digraph {\n" +
                "\trankdir=LR\n" +
                dfaToDot(automaton) +
                "}\n";

        writeOut(outputPath, dotRepr);
    }

    public static void automatonToDotfile(NFA nfa, Path outputPath) {
        String dotRepr = "digraph {\n" +
                "\trankdir=LR\n\t" +
                toNfaDot(nfa) +
                "}\n";

        writeOut(outputPath, dotRepr);
    }

    public static void automatonToDotfile(EpsilonNFA eNFA, Path outputPath) {
        String dotRepr = "digraph {\n" +
                "\trankdir=LR\n\t" +
                toENfaDot(eNFA) +
                "}\n";

        writeOut(outputPath, dotRepr);
    }

    private static void writeOut(Path outputPath, String stringRepr) {
        try {
            BufferedWriter bWriter = Files.newBufferedWriter(outputPath);
            bWriter.write(stringRepr);
            bWriter.flush();
        } catch (IOException e) {
            System.err.printf("Couldn't create output dot file! %s\n", e.getMessage());
        }
    }
}
