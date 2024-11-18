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

    private ValueState<Integer> state;

    @Override
    public void open(Configuration configuration) throws Exception {
        state = getRuntimeContext().getState(new ValueStateDescriptor<>("myState", Integer.class));
    }

    @Override
    public void processElement(MarketDataEvent value,
            KeyedProcessFunction<String, MarketDataEvent, CrossoverEventResult>.Context ctx,
            Collector<CrossoverEventResult> out) throws Exception {

        if (state.value() == null) {
            state.update(0);
        }

        state.update(state.value() + 1);

        out.collect(new CrossoverEventResult(value.symbol, SignalType.Long, state.value()));
    }

    @Override
    public void close() {
        state.clear();
    }
}
