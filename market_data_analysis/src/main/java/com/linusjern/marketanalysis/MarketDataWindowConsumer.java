package com.linusjern.marketanalysis;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;

import com.linusjern.marketanalysis.MarketDataEvent.MapToMarketDataEvent;
import com.linusjern.marketanalysis.MarketDataEvent.FilterValidMarketDataEvent;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import java.time.Duration;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.ReduceFunction;

public class MarketDataWindowConsumer {

    final static String inputTopic = "market-data";
    final static String jobTitle = "market-data-analysis";

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // env.setParallelism(10);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics(inputTopic)
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> dataStream = env.fromSource(source, WatermarkStrategy.noWatermarks(),
                "Kafka Source");

        DataStream<MarketDataEvent> parsed = dataStream
                .map(new MapToMarketDataEvent())
                .filter(new FilterValidMarketDataEvent())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<MarketDataEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((event, timestamp) -> event.timestamp))
                .keyBy(event -> event.symbol)
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .reduce(new ReduceFunction<MarketDataEvent>() {
                    @Override
                    public MarketDataEvent reduce(MarketDataEvent event1, MarketDataEvent event2) {
                        return event1.timestamp > event2.timestamp ? event1 : event2;
                    }
                });

        parsed.print();

        env.execute(jobTitle);
    }

}
