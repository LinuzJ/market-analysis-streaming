package com.linusjern.marketanalysis;

import java.time.format.DateTimeParseException;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream.Collector;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

public class MarketDataEvent {
    public String symbol;
    public String exchange;
    public String securityType;
    public Float value;
    public String time;
    public String date;
    public long timestamp;

    public MarketDataEvent() {
    }

    public MarketDataEvent(String rawString) {
        String[] splitString = rawString.split(",", -1);

        this.symbol = splitString[0];
        this.securityType = splitString[1];

        if (!splitString[2].isEmpty()) {
            this.value = Float.parseFloat(splitString[2]);
        }

        // Time is guaranteed
        this.time = splitString[3].replace(":", "-").replace(".", "-");
        this.date = splitString[4];

        if (!this.time.isBlank() && !this.date.isBlank()) {
            try {
                this.timestamp = new TimestampConverter().convertToTimestamp(this.date + "-" + this.time);
            } catch (DateTimeParseException e) {
                this.timestamp = -1;
            }
        } else {
            this.timestamp = -1;
        }

        this.checkForExchange();
    }

    public boolean isValidEvent() {
        return this.timestamp > 0;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String toString() {
        if (!this.value.isNaN()) {
            return "Parsed values: " + this.symbol +
                    " has value " +
                    Float.toString(this.value) +
                    " and timestamp: " +
                    this.timestamp;
        }
        return "No value.";
    }

    public static class MapToMarketDataEvent implements MapFunction<String, MarketDataEvent> {

        @Override
        public MarketDataEvent map(String rawString) throws Exception {
            return new MarketDataEvent(rawString);
        }
    }

    public static class FilterValidMarketDataEvent implements FilterFunction<MarketDataEvent> {

        @Override
        public boolean filter(MarketDataEvent event) throws Exception {
            return event.isValidEvent();
        }
    }

    public static class MarketDataEventWindowFunction
            extends ProcessWindowFunction<MarketDataEvent, String, String, TimeWindow> {

        @Override
        public void process(String key,
                ProcessWindowFunction<MarketDataEvent, String, String, TimeWindow>.Context context,
                Iterable<MarketDataEvent> input, org.apache.flink.util.Collector<String> out) {
            MarketDataEvent lastEventInThisWindow = new MarketDataEvent();
            for (MarketDataEvent in : input) {
                if (lastEventInThisWindow.isValidEvent()) {
                    lastEventInThisWindow = in;
                    continue;
                }

                if (in.timestamp > lastEventInThisWindow.timestamp) {
                    lastEventInThisWindow = in;
                }
            }
            out.collect("Window: " + context.window() + "Last Event: " + lastEventInThisWindow);
        }
    }

    private void checkForExchange() {
        if (this.symbol.indexOf(".") == -1) {
            return;
        }

        String[] symbolAndExchange = this.symbol.split(".");

        if (symbolAndExchange.length > 1) {
            this.exchange = symbolAndExchange[1];
        } else {
            this.exchange = "UNKNOWN";
        }
    }

}
