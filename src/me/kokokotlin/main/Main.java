package me.kokokotlin.main;

import me.kokokotlin.main.engine.Automaton;
import me.kokokotlin.main.engine.Loader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


public class Main {
    private static boolean interactive = false;
    private static boolean checkSrc = false;
    private static boolean repr = false;
    private static String word;
    private static Path automatonSrc;

    private static String convertWord(String word) {
        return (word.length() == 0) ? "ε" : word;
    }

    private static void printHelp() {
        System.out.println("""
            Automaton Interpreter by Yannik Höll (2021)

            Command line switches:
                -h: Display help
                -c: Check the source for errors
                -i: Start program in interactive mode
                -p: Path of the source of the automaton [required]
                -r: Print parsed version of automaton
                -w: Input word for the automaton [required when no -i, -c provided]
        """);
    }

    private static String getArgumentOrError(Queue<String> queue, String errorMsg) {
        String s = queue.poll();
        if (s == null) throw new IllegalArgumentException(errorMsg);

        return s;
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
                case "-r" -> {
                    repr = true;
                }
                default -> {
                    throw new IllegalArgumentException(String.format("Command line option %s unknown! See -h for help!", currentOption));
                }
            }
        }
    }

    private static void interactivePrompt(Automaton automaton) {
        final Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.print("(q: quit)> ");
            String userInput = scanner.nextLine();

            if ("q".equals(userInput)) {
                return;
            } else if ("p".equals(userInput)) {
                System.out.println(automaton.getStringRepr());
            } else {
                System.out.printf("Word: %s, Accepted: %s\n", convertWord(userInput), automaton.isAccepted(userInput));
            }
        }
    }


    public static void main(String[] args) {
        handleARGS(args);

        if (automatonSrc == null) {
            System.err.println("No automaton source provided! Exiting...");
            return;
        }

        if (!interactive && !repr && !checkSrc && word == null) {
            System.err.println("No word provided and not in interactive mode! Exiting...");
            return;
        }

        Automaton automaton = Loader.loadFromFile(automatonSrc);
        if (automaton == null) return;

        if (checkSrc) {
            System.out.printf("No syntactical errors found in \"%s\".\n", automatonSrc.toString());
            return;
        }

        if (repr) {
            System.out.println(automaton.getStringRepr());
            return;
        }

        if (interactive) interactivePrompt(automaton);
        else System.out.printf("Word: %s, Accepted: %s\n", convertWord(word), automaton.isAccepted(word));
    }
}
