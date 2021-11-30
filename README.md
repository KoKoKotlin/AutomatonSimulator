# Automaton Interpreter in Java

## Features:

- Loading Automata from file with specific representation (look /res)
- Syntax checking
- Printing the program internal representation for debugging
- Checking if words are accepted

## aut file format:
```
(dfa|nfa|enfa) no_of_states no_of_transitions (initial_state0, initial_state1, ...) (final_state0, final_state1, ...) alphabet
(s state_name)*
(t start_state_name dest_state_name symbol)*
```

initial_stateN and final_stateN have to be the indices of the states beginning at 0. The order is the order defined in the file below.
You can leave the paranthesis empty, if you don't want inital or final states.
But be aware of the fact that a dfa always needs exactly one initial state or you will get an error.

If you want to create a enfa with an epsilon transition put `""` as the symbol. 

The number of states and transitions given has to match the number provided in the header. The program outputs clear errors if some rule is violated (e.g if a dfa/nfa has epsilon transition)

### TODO:

- implement a better language (regex) ☑
- refactor states into an own class ☑
- visualization of the automaton ☑
- step by step debugging ☐
- class for DFA and NDA ☑