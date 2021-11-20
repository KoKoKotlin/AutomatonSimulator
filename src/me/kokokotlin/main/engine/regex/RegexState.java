package me.kokokotlin.main.engine.regex;

class RegexState {
    String symbol;
    SymbolFrequency frequency;
    Type type;

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

    @Override
    public String toString() {
        return "RegexState{" +
                "symbol='" + symbol + '\'' +
                ", frequency=" + frequency +
                ", type=" + type +
                '}';
    }
}