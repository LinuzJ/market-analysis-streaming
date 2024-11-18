package com.linusjern.marketanalysis.types;

public class CrossoverEventResult {
    public String symbol;
    public SignalType signal;
    public Integer count;

    public CrossoverEventResult(String symbol, SignalType signal, Integer c) {
        this.symbol = symbol;
        this.signal = signal;
        this.count = c;
    }

    public void incCount() {
        this.count += 1;
    }

    public String toString() {
        return "\nSYMBOL: " + this.symbol +
                " HAS SIGNAL " +
                this.signal +
                " AND COUNT: " +
                this.count;
    }
}
