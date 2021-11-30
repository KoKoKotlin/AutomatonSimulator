package me.kokokotlin.main;

import me.kokokotlin.main.engine.AutomatonBase;
import me.kokokotlin.main.engine.DFA;
import me.kokokotlin.main.engine.Loader;
import me.kokokotlin.main.engine.graphviz.DotEncoder;
import me.kokokotlin.main.engine.regex.RegularExpressionLoader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


public class Main {
    private static boolean interactive = false;
    private static boolean checkSrc = false;

    private static boolean dotFile = false;
    private static Path dotPath;

    private static String word;
    private static String regex;

    private static Path automatonSrc;

    private static boolean pngFile;
    private static Path pngPath;

    private static String convertWord(String word) {
        return (word.length() == 0) ? "ε" : word;
    }

    private static void printHelp() {
        System.out.println("""
Automaton Interpreter by Yannik Höll (2021)

Command line switches:
    -h: Display help
    -c: Check the source for errors (only works with provided source files)
    -d <path>: Write graph representation to dot file for graphviz [default: automaton.dot]
    -i: Start program in interactive mode
    -p <path>: Path of the source of the automaton 
    -png <path>: Save a image of the graph at the given path [default: automaton.png]
    -regex <regular expression>: Regular expression from which an automaton is build
    -w: Input word for the automaton [required when no -i, -c, -d, -r provided]

You have to provide a path to a source file or a regular expression such that an automaton can be loaded.
If neither is provided, the program will exit without further action.
If both are provided the program will load from file. The regex will then be not taken into account.
        """);
    }

    private static String getArgumentOrError(Queue<String> queue, String errorMsg) {
        String s = queue.poll();
        if (s == null) throw new IllegalArgumentException(errorMsg);

        return s;
    }

    private static String tryGetArgument(Queue<String> queue) {
        String s = queue.peek();
        if (s == null || s.startsWith("-")) return null;

        return queue.poll();
    }

    private static void handleARGS(String[] args) {
        Queue<String> argQueue = new LinkedList<>(Arrays.asList(args));
        while (!argQueue.isEmpty()) {
            String currentOption = argQueue.poll();

            switch (currentOption) {
                case "-w" -> {
                    word = getArgumentOrError(argQueue, "Command line option -w needs a argument <word>!");
                }
                case "-h" -> {
                    printHelp();
                    System.exit(0);
                }
                case "-p" -> {
                    String argument = getArgumentOrError(argQueue, "Command line option -p needs a argument <file path>!");
                    automatonSrc = Paths.get(argument);
                }
                case "-i" -> {
                    interactive = true;
                }
                case "-c" -> {
                    checkSrc = true;
                }
                case "-d" -> {
                    dotFile = true;
                    String maybePath = tryGetArgument(argQueue);
                    dotPath = Paths.get((maybePath != null) ? maybePath : "automaton.dot");
                }
                case "-regex" -> {
                    regex = getArgumentOrError(argQueue, "Command line option -regex needs a argument <regular expression>!");
                }
                case "-png" -> {
                    pngFile = true;
                    String maybePath = tryGetArgument(argQueue);
                    pngPath = Paths.get((maybePath != null) ? maybePath : "automaton.png");
                }
                default -> {
                    throw new IllegalArgumentException(String.format("Command line option %s unknown! See -h for help!", currentOption));
                }
            }
        }
    }

    private static void interactivePrompt(AutomatonBase automaton) {
        final Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.print("(q: quit)> ");
            String userInput = scanner.nextLine();

            if ("q".equals(userInput)) {
                scanner.close();
                return;
            } else {
                System.out.printf("Word: %s, Accepted: %s\n", convertWord(userInput), automaton.match(userInput));
            }
        }
    }


    public static void main(String[] args) {
        handleARGS(args);

        if (automatonSrc == null && regex == null) {
            System.err.println("No automaton source provided! Exiting...");
            return;
        }

        AutomatonBase automaton;
        if (automatonSrc != null) automaton = Loader.loadFromFile(automatonSrc);
        else automaton = RegularExpressionLoader.loadFromRegex(regex);

        if (automaton == null) return;

        if (dotFile) {
            DotEncoder.automatonToDotfile(automaton, Paths.get("res/automaton.dot"));
            return;
        }

        if (pngFile) {
            DotEncoder.automatonToPng(automaton, pngPath);
            return;
        }

        if (checkSrc && automatonSrc != null) {
            System.out.printf("No syntactical errors found in \"%s\".\n", automatonSrc.toString());
            return;
        }

        if (interactive) {
            interactivePrompt(automaton);
        } else {

            if (word == null) {
                System.err.println("No word provided and not in interactive mode! Exiting...");
                return;
            }

            System.out.printf("Word: %s, Accepted: %s\n", convertWord(word), automaton.match(word));
        }
    }
}
