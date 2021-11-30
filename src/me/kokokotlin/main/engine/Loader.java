package me.kokokotlin.main.engine;

import me.kokokotlin.main.utils.Tuple;

import java.beans.Expression;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import javax.sound.sampled.SourceDataLine;

public class Loader {
    private static boolean error = false;
    private static boolean hasEpsilons = false;

    private static enum AutomatonType {
        DFA,
        NFA,
        ENFA;

        static AutomatonType fromString(String repr) throws IllegalArgumentException {
            switch(repr) {
                case "dfa":  return DFA;
                case "nfa":  return NFA;
                case "enfa": return ENFA;
                default: throw new IllegalArgumentException("Illegal Automaton type: Type has to be one of the following: dfa, nfa, enfa!");
            }
        }
    } 

    static private class Header {
        int stateCount = 0;
        int transitionCount = 0;
        List<Integer> initialStates = new ArrayList<>();
        List<Integer> finalStates = new ArrayList<>();
        String alphabet = "";
        AutomatonType type;
    }

    private static List<Integer> parseStates(String stateRepr) {
        List<Integer> stateIdx = new ArrayList<>();
        
        for (String idx: stateRepr.replace("(", "").replace(")", "").replace(" ", "").split(",")) {
            try {
                stateIdx.add(Integer.valueOf(idx));
            } catch (NumberFormatException e) {
                System.err.printf("Error while parsing state indices in header! %s\n", e.getMessage());
                error = true;
                return null;
            }
        }

        return stateIdx;
    }

    private static Header parseHeader(String line) {
        Header header = new Header();

        String[] data = line.split(" ");
        if (data.length < 6) {
            System.err.printf("Error while parsing header! Not enough arguments. Expected 6 or more got %d!\n", data.length);
            error = true;
            return null;
        }

        try {
            header.type = AutomatonType.fromString(data[0]);
            header.stateCount = Integer.parseInt(data[1]);
            header.transitionCount = Integer.parseInt(data[2]);
            
            // parse the state indices -> first iteration initial states, second iteration: final states
            int current = 3;
            for (int i = 0; i < 2; i++) {
                StringBuilder stateRepr = new StringBuilder();
                while (!data[current].contains(")")) {
                    stateRepr.append(data[current]);
                    current++;
                }
                stateRepr.append(data[current++]);
                
                List<Integer> stateIdx = parseStates(stateRepr.toString());
                if (stateIdx == null) return null;

                if (i == 0) header.initialStates = stateIdx;
                else if (i == 1) header.finalStates = stateIdx;
            }

            header.alphabet = data[current];
        } catch (IllegalArgumentException e) {
            System.err.printf("Error while parsing header: %s!\n", e.getMessage());
            error = true;
            return null;
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Malformed header! Probably something is wrong with your paranthesis.");
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

    private static void parseTransition(String line, int lineCount, List<String> stateNames, List<State> states, Header header) {
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

        String startStateName = transitionData[1];
        String symbol = transitionData[3];
        String finalStateName = transitionData[2];

        if (symbol.length() != 1 && !symbol.equals("\"\"")) {
            System.err.printf("Error while parsing line %d! Second argument has to be character! Got %s.\n", lineCount, transitionData[3]);
            error = true;
            return;
        }
        
        if (!stateNames.contains(startStateName)) {
            System.err.printf("Error while parsing line %d! Starting state %s not defined!", lineCount, startStateName);
            error = true;
            return;
        }

        if (!stateNames.contains(finalStateName)) {
            System.err.printf("Error while parsing line %d! Final state %s not defined!", lineCount, finalStateName);
            error = true;
            return;
        }

        if (!header.alphabet.contains(transitionData[3]) && !symbol.equals("\"\"")) {
            System.err.printf("Error while parsing line %d! Symbol %s not in alphabet!", lineCount, transitionData[3]);
            error = true;
            return;
        }

        Optional<State> startState = states.stream().filter(state -> state.getName().equals(startStateName)).findFirst();
        Optional<State> finalState = states.stream().filter(state -> state.getName().equals(finalStateName)).findFirst();

        if (startState.isEmpty() || finalState.isEmpty()) 
            throw new IllegalStateException("Internal error!");

        if (symbol.equals("\"\"")) {
            if (header.type != AutomatonType.ENFA) 
                throw new IllegalStateException("Only automata of type enfa can contain epsilon trainsitions!");

            symbol = Symbol.EPSILON;
            hasEpsilons = true;
        }
        startState.get().addTransition(new Symbol(String.valueOf(symbol)), finalState.get());
    }

    public static AutomatonBase loadFromFile(Path path) {
        error = false;

        BufferedReader bReader;

        try {
            bReader = Files.newBufferedReader(path);
        } catch (IOException e) {
            System.err.printf("Cant open file %s: %s!\n", path, e.getMessage());
            return null;
        }

        Header header = new Header();
        List<State> states = new ArrayList<>();
        List<String> stateNames = new ArrayList<>();
        try {
            int i = 0;
            for(String line = bReader.readLine(); line != null; line = bReader.readLine()) {

                // parse the header
                if (i == 0) header = parseHeader(line);
                if(error) return null;

                // parse the states
                if (i > 0 && (i - 1) < header.stateCount) {
                    String stateName = parseState(line, i);
                    stateNames.add(stateName);
                    states.add(new State(stateName, Arrays.asList(header.alphabet.split("")), header.type == AutomatonType.ENFA));
                }
                if (error) return null;

                // parse the transitions
                if (i > header.stateCount && (i - 1 - header.stateCount) < header.transitionCount) {
                    parseTransition(line, i, stateNames, states, header);
                }
                if (error) return null;

                i++;
            }

            // if the loaded automaton is a DFA -> check if all needed transitions are present
            List<String> missing = states.stream().map(State::missingChars).collect(Collectors.toList());
            if (header.type == AutomatonType.DFA) {
                if (missing.stream().anyMatch(s -> s.length() != 0)) {
                    var notSaturated = Tuple.zip(states, missing).stream()
                            .filter(t -> t.getSecond().length() != 0)
                            .collect(Collectors.toList());

                    String errorMsg = errorForNonSaturatedStates(notSaturated);
                    throw new IllegalStateException(String.format("Not all states are saturated! \n%s", errorMsg));
                }
            }

            
            List<State> initialStates = new ArrayList<>();
            for (Integer idx: header.initialStates) {
                try {
                    initialStates.add(states.get(idx));
                } catch (IndexOutOfBoundsException e) {
                    System.out.printf("Initial State at index %d is out of bounds of [0, %d]!\n", idx, states.size() - 1);
                }
            }

            List<State> finalStates = new ArrayList<>();
            for (Integer idx: header.finalStates) {
                try {
                    finalStates.add(states.get(idx));
                } catch (IndexOutOfBoundsException e) {
                    System.out.printf("Initial State at index %d is out of bounds of [0, %d]!\n", idx, states.size() - 1);
                }
            }

            switch (header.type) {
                case DFA:  return new DFA(states, initialStates, finalStates, Arrays.asList(header.alphabet.split("")));
                case NFA:  return new NFA(states, initialStates, finalStates, Arrays.asList(header.alphabet.split("")));
                case ENFA: return new ENFA(states, initialStates, finalStates, Arrays.asList(header.alphabet.split("")), hasEpsilons);
            }
        } catch (IOException e) {
            System.err.println("Error while reading!");
        }

        return null;
    }

    private static String errorForNonSaturatedStates(List<Tuple<State, String>> notSaturated) {
        StringBuilder errorMsg = new StringBuilder();
        for (int j = 0; j < notSaturated.size(); j++) {
            var data = notSaturated.get(j);
            String stateName = data.getFirst().getName();
            String missingStates = String.join(", ", data.getSecond().split(""));

            if (data.getSecond().length() == 1)
                errorMsg.append(String.format("-> The state %s is missing the transition for the symbol { %s }.",
                        stateName,
                        missingStates));
            else
                errorMsg.append(String.format("-> The state %s is missing transitions for the symbols { %s }.",
                        stateName,
                        missingStates));

            if (j != notSaturated.size() - 1) errorMsg.append("\n");
        }

        return errorMsg.toString();
    }

}
