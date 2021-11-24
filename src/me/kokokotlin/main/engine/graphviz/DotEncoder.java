package me.kokokotlin.main.engine.graphviz;

import me.kokokotlin.main.engine.Automaton;
import me.kokokotlin.main.engine.State;
import me.kokokotlin.main.engine.regex.EpsilonNFA;
import me.kokokotlin.main.engine.regex.NFA;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class DotEncoder {

    private static String getStartStateName(int index) {
        return String.format("__start%d__", index);
    }

    private static void writeTransitions(StringBuilder sink, List<State> states) {
        for (State state: states) {
            for (var entry: state.getTransition().entrySet()) {
                sink.append("\t");
                sink.append(state.getName());
                sink.append(" -> ");
                sink.append(entry.getValue().get(0).getName());
                sink.append(String.format(" [label = \" %c\"]", entry.getKey()));
                sink.append("\n");
            }
        }
    }

    private static void writeInitialAndFinalStates(StringBuilder sink, List<State> initials, List<State> finals) {

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

    private static String dfaToDot(Automaton automaton) {
        StringBuilder dfaDot = new StringBuilder();

        writeInitialAndFinalStates(dfaDot, List.of(automaton.getInitialState()), Arrays.asList(automaton.getFinalStates()));
        writeTransitions(dfaDot, Arrays.asList(automaton.getStates()));

        return dfaDot.toString();
    }

    private static String toNfaDot(NFA nfa) {
        StringBuilder nfaDot = new StringBuilder();

        writeInitialAndFinalStates(nfaDot, nfa.getInitialStates(), nfa.getFinalStates());
        writeTransitions(nfaDot, nfa.getStates());

        return nfaDot.toString();
    }

    private static String toENfaDot(EpsilonNFA eNFA) {
        StringBuilder eNfaDot = new StringBuilder();

        writeInitialAndFinalStates(eNfaDot, eNFA.getInitialStates(), eNFA.getFinalStates());
        writeTransitions(eNfaDot, eNFA.getStates());

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

    public static void automatonToPng(Automaton dfa, Path pngPath) {
        Path tempDotPath = Paths.get("__automaton__.dot");
        automatonToDotfile(dfa, tempDotPath);

        try {
            Runtime.getRuntime().exec(String.format("dot -Tpng %s -o %s", tempDotPath.toAbsolutePath(), pngPath.toAbsolutePath()));
            Files.delete(tempDotPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
