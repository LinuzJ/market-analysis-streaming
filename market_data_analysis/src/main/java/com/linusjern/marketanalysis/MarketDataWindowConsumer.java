package com.linusjern.marketanalysis;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import com.linusjern.marketanalysis.MarketDataEvent.MapToMarketDataEvent;
import com.linusjern.marketanalysis.MarketDataEvent.FilterValidMarketDataEvent;
import com.linusjern.marketanalysis.MarketDataEvent.MarketDataEventWindowFunction;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.api.common.serialization.SimpleStringSchema;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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

        DataStream<MarketDataEvent> parsedEvents = dataStream
                .map(new MapToMarketDataEvent())
                .filter(new FilterValidMarketDataEvent());

        DataStream<MarketDataEvent> timestampedEvents = parsedEvents
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<MarketDataEvent>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                                .withTimestampAssigner((event, timestamp) -> event.timestamp));

        DataStream<String> reduced = timestampedEvents
                .keyBy(MarketDataEvent::getSymbol)
                .window(TumblingEventTimeWindows.of(Time.seconds(20), Time.seconds(5)))
                .process(new MarketDataEventWindowFunction());

        reduced.print();

        env.execute(jobTitle);
    }

}
