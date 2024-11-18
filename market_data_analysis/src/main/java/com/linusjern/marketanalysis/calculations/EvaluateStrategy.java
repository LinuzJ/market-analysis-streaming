package com.linusjern.marketanalysis.calculations;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import com.linusjern.marketanalysis.types.CrossoverEventResult;
import com.linusjern.marketanalysis.types.MarketDataEvent;
import com.linusjern.marketanalysis.types.SignalType;

public class EvaluateStrategy extends KeyedProcessFunction<String, MarketDataEvent, CrossoverEventResult> {

    private ValueState<Float> previousEMA100;
    private ValueState<Float> previousEMA38;

    @Override
    public void open(Configuration configuration) throws Exception {
        previousEMA100 = getRuntimeContext()
                .getState(new ValueStateDescriptor<>("previousEMA100State", Float.class));
        previousEMA38 = getRuntimeContext()
                .getState(new ValueStateDescriptor<>("previousEMA38State", Float.class));
    }

    @Override
    public void processElement(MarketDataEvent event,
            KeyedProcessFunction<String, MarketDataEvent, CrossoverEventResult>.Context ctx,
            Collector<CrossoverEventResult> out) throws Exception {

        if (previousEMA38.value() == null || previousEMA100.value() == null) {
            previousEMA38.update(0f);
            previousEMA100.update(0f);
        }

        ExponentialMovingAverage ema = new ExponentialMovingAverage();
        Float newEMA38 = ema.calculateEMA(event.value, previousEMA38.value(), 38);
        Float newEMA100 = ema.calculateEMA(event.value, previousEMA100.value(), 100);

        SignalType signal = ema.evaluateBreakoutType(previousEMA38.value(), newEMA38, previousEMA100.value(),
                newEMA100);

        previousEMA38.update(newEMA38);
        previousEMA100.update(newEMA100);

        if (signal == SignalType.None) {
            return;
        }

        out.collect(new CrossoverEventResult(event, signal));
    }

    @Override
    public void close() {
        previousEMA100.clear();
        previousEMA38.clear();
    }
}
