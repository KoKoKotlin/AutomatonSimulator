package me.kokokotlin.main.engine.regex;

import me.kokokotlin.main.engine.Symbol;

public class RegexState {
    public String symbol;
    public SymbolFrequency frequency;
    public Type type;

    public RegexState(String symbol) {
        this.symbol = symbol;
        type = Type.CONSTANT;
        frequency = SymbolFrequency.EXACTLY_ONE;
    }

    public static RegexState wildcard() {
        RegexState r = new RegexState("");
        r.type = Type.WILDCARD;
        return r;
    }

    public Symbol toSymbol() {
        if (type == Type.WILDCARD) return Symbol.wildcard();
        else return new Symbol(this.symbol);
    }

    @Override
    public String toString() {
        return "RegexState{" +
                "symbol='" + symbol + '\'' +
                ", frequency=" + frequency +
                ", type=" + type +
                '}';
    }
}