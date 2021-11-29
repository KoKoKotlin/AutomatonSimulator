package me.kokokotlin.main.engine;

import me.kokokotlin.main.engine.regex.Type;

public class Symbol {
    // encoding of empty word
    public static final String EPSILON = "\0";

    private Type type;
    private String symbol;
    
    public Symbol(Type type, String symbol) {
        this.type = type;
        this.symbol = symbol;
    }

    public Symbol(String symbol) {
        this.type = (symbol == null) ? Type.WILDCARD : Type.CONSTANT;
        this.symbol = symbol;
    }

    public static Symbol epsilon() {
        return new Symbol(EPSILON);
    }

    public static Symbol wildcard() {
        return new Symbol(null);
    }

    public boolean match(String matchingSymbol) {
        switch(type) {
            case CONSTANT -> {
                return matchingSymbol.equals(this.symbol);
            }
            case WILDCARD -> {
                return true;
            }
            default -> { throw new IllegalStateException("Switch case is not exaustive!"); }
        }
    }

    @Override
    public String toString() {
        return "Symbol [symbol=" + symbol + ", type=" + type + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Symbol other = (Symbol) obj;
        if (symbol == null) {
            if (other.symbol != null)
                return false;
        } else if (!symbol.equals(other.symbol))
            return false;
        if (type != other.type)
            return false;
        return true;
    }

    public String dotRepr() {
        return (symbol == null) ? "." : symbol;
    }
}
