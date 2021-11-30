package me.kokokotlin.main.engine.graphviz;

import me.kokokotlin.main.engine.AutomatonBase;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DotEncoder {
    public static void automatonToDotfile(AutomatonBase automaton, Path outputPath) {
        writeOut(outputPath, automaton.toDotRepr());
    }

    public static void automatonToPng(AutomatonBase automaton, Path pngPath) {
        Path tempDotPath = Paths.get("__automaton__.dot");
        automatonToDotfile(automaton, tempDotPath);

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
