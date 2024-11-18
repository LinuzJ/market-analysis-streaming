package com.linusjern.marketanalysis.types;

import java.time.Instant;

public class CrossoverEventResult {
    public MarketDataEvent event;
    public SignalType signal;
    public long processingTime;

    public CrossoverEventResult(MarketDataEvent event, SignalType signal) {
        this.event = event;
        this.signal = signal;
        this.processingTime = Instant.now().toEpochMilli() - this.event.systemTimestampStart;
    }

    public String toString() {
        return "\nSYMBOL: " + this.event.symbol +
                " HAS SIGNAL " +
                this.signal +
                " AT PRICE " +
                Float.toString(this.event.value) +
                " TOOK " +
                this.processingTime +
                " MILLISECONDS.";
    }
}
