package me.kokokotlin.main.engine;

import java.util.List;

import static me.kokokotlin.main.engine.graphviz.DotUtils.*;

public abstract class AutomatonBase {
    
    protected final List<State> states;
    protected final List<State> initialStates;
    protected final List<State> finalStates;

    protected final List<String> alphabet;

    public AutomatonBase(List<State> states, List<State> initialStates, List<State> finalStates, List<String> alphabet) {
        this.states = states;
        this.initialStates = initialStates;
        this.finalStates = finalStates;
        this.alphabet = alphabet;
    }

    public abstract boolean match(String word);
    
    public String toDotRepr() {
        StringBuilder repr = new StringBuilder();
        
        repr.append("digraph {\n\trankdir=LR\n");        
        writeInitialAndFinalStates(repr, getInitialStates(), getFinalStates());
        writeTransitions(repr, this.getStates());
        repr.append("}\n");

        return repr.toString();
    }
    
    public abstract DFA toDFA();
    public abstract NFA toNFA();
    public abstract ENFA toENFA();

    public List<State> getStates() {
        return states;
    }

    public List<State> getInitialStates() {
        return initialStates;
    }

    public List<State> getFinalStates() {
        return finalStates;
    }

    public List<String> getAlphabet() {
        return alphabet;
    }
}
