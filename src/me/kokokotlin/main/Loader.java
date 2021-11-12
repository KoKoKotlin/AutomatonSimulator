package me.kokokotlin.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Loader {
    private static boolean error = false;

    static private class Header {
        int stateCount = 0;
        int transitionCount = 0;
        int startingState = 0;
        List<Integer> finalStates = new ArrayList<>();
        String alphabet = "";
    }

    private static Header parseHeader(String line) {
        Header header = new Header();

        String[] data = line.split(" ");
        if (data.length < 5) {
            System.err.printf("Error while parsing header! Not enough arguments. Expected 5 or more got %d!\n", data.length);
            error = true;
            return null;
        }

        try {
            header.stateCount = Integer.parseInt(data[0]);
            header.transitionCount = Integer.parseInt(data[1]);
            header.startingState = Integer.parseInt(data[2]);
            header.alphabet = data[3];
            for (int i = 4; i < data.length; i++) header.finalStates.add(Integer.parseInt(data[i]));
        } catch (NumberFormatException e) {
            System.err.printf("Error while parsing header: %s!\n", e.getMessage());
            error = true;
            return null;
        }

        return header;
    }

    private static String parseState(String line, int lineCount) {
        String[] lineData = line.split(" ");

        if (lineData.length != 2) {
            System.err.printf("Error while parsing line %d! Not enough arguments. Expected 2 got %d!\n", lineCount, lineData.length);
            error = true;
            return null;
        }

        if (!lineData[0].equals("s")) {
            System.err.printf("Error while parsing line %d! Expected state.\n", lineCount);
            error = true;
            return null;
        }

        return lineData[1];
    }

    private static void parseTransition(String line, int lineCount, Transition transition, List<String> states, String alphabet) {
        String[] transitionData = line.split(" ");

        if (transitionData.length != 4) {
            System.err.printf("Error while parsing line %d! Not enough arguments. Expected 4 got %d!\n", lineCount, transitionData.length);
            error = true;
            return;
        }

        if (!transitionData[0].equals("t")) {
            System.err.printf("Error while parsing line %d! Expected transition.\n", lineCount);
            error = true;
            return;
        }

        String startState = transitionData[1];
        if (transitionData[3].length() != 1) {
            System.err.printf("Error while parsing line %d! Second argument has to be character! Got %s.\n", lineCount, transitionData[3]);
            error = true;
            return;
        }
        Character symbol = transitionData[3].charAt(0);
        String finalState = transitionData[2];


        if (!states.contains(startState)) {
            System.err.printf("Error while parsing line %d! Starting state %s not defined!", lineCount, startState);
            error = true;
            return;
        }

        if (!states.contains(finalState)) {
            System.err.printf("Error while parsing line %d! Final state %s not defined!", lineCount, finalState);
            error = true;
            return;
        }

        if (!alphabet.contains(transitionData[3])) {
            System.err.printf("Error while parsing line %d! Symbol %s not in alphabet!", lineCount, transitionData[3]);
            error = true;
            return;
        }

        transition.addTransition(startState, symbol, finalState);
    }

    public static Automaton loadFromFile(Path path) {
        error = false;

        BufferedReader bReader;

        try {
            bReader = Files.newBufferedReader(path);
        } catch (IOException e) {
            System.err.printf("Cant open file %s: %s!\n", path, e.getMessage());
            return null;
        }

        Header header = new Header();
        List<String> states = new ArrayList<>();
        Transition transition = null;
        try {
            int i = 0;
            for(String line = bReader.readLine(); line != null; line = bReader.readLine()) {

                // parse the header
                if (i == 0) header = parseHeader(line);
                if(error) return null;

                // parse the states
                if (i > 0 && (i - 1) < header.stateCount) {
                    String state = parseState(line, i);
                    states.add(state);
                }
                if (error) return null;

                // parse the transitions
                if (i > header.stateCount && (i - 1 - header.stateCount) < header.transitionCount) {
                    if (transition == null) {
                        String[] states_ = new String[states.size()];
                        states.toArray(states_);
                        transition = new Transition(states_);
                    }

                    parseTransition(line, i, transition, states, header.alphabet);
                }
                if (error) return null;

                i++;
            }

            String[] states_ = new String[states.size()];
            states.toArray(states_);

            Integer[] finalStates = new Integer[header.finalStates.size()];
            header.finalStates.toArray(finalStates);

            return new Automaton(states_, header.startingState, finalStates, transition, header.alphabet);
        } catch (IOException e) {
            System.err.println("Error while reading!");
        }

        return null;
    }

}
