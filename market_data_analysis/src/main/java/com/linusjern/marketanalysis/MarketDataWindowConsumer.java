package com.linusjern.marketanalysis;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import com.linusjern.marketanalysis.calculations.EvaluateStrategy;
import com.linusjern.marketanalysis.types.CrossoverEventResult;
import com.linusjern.marketanalysis.types.MarketDataEvent;
import com.linusjern.marketanalysis.types.MarketDataEvent.FilterValidMarketDataEvent;
import com.linusjern.marketanalysis.types.MarketDataEvent.GetLastEventPerWindowFunction;
import com.linusjern.marketanalysis.types.MarketDataEvent.MapToMarketDataEvent;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import java.time.Duration;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;

public class MarketDataWindowConsumer {

    final static String inputTopic = "market-data";
    final static String jobTitle = "market-data-analysis";

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStateBackend(new HashMapStateBackend());
        env.setParallelism(1);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics(inputTopic)
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> dataStream = env.fromSource(source, WatermarkStrategy.noWatermarks(),
                "Kafka Source");

        DataStream<MarketDataEvent> batchedPerWindowAndSymbol = dataStream
                .map(new MapToMarketDataEvent())
                .filter(new FilterValidMarketDataEvent())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<MarketDataEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((event, timestamp) -> event.timestamp))
                .keyBy(MarketDataEvent::getSymbol)
                .window(TumblingEventTimeWindows.of(Time.minutes(5), Time.minutes(1)))
                .process(new GetLastEventPerWindowFunction());

        DataStream<CrossoverEventResult> crossoverEvents = batchedPerWindowAndSymbol.keyBy(MarketDataEvent::getSymbol)
                .process(new EvaluateStrategy());

        crossoverEvents.print();

        env.execute(jobTitle);
    }
}
