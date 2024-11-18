package com.linusjern.marketanalysis.types;

public class CrossoverEventResult {
    public String symbol;
    public SignalType signal;

    public CrossoverEventResult(String symbol, SignalType signal) {
        this.symbol = symbol;
        this.signal = signal;
    }

    public String toString() {
        return "\nSYMBOL: " + this.symbol +
                " HAS SIGNAL " +
                this.signal;
    }
}
