package me.kokokotlin.main.engine.regex;

public enum Type {
    WILDCARD,
    CONSTANT;

    // \0 as sentinel value for wildcard
    public String fromSymbol(String s) {
        return (this == CONSTANT) ? s : "\0";
    }
}