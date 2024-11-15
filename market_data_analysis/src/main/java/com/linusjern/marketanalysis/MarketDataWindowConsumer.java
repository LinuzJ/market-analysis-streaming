package com.linusjern.marketanalysis;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.linusjern.marketanalysis.MarketDataEvent.MapToMarketDataEvent;
import com.linusjern.marketanalysis.MarketDataEvent.FilterValidMarketDataEvent;

import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;

public class MarketDataWindowConsumer {

    final static String inputTopic = "market-data";
    final static String jobTitle = "WordCount";

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(100);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("localhost:9092")
                .setTopics(inputTopic)
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStream<String> dataStream = env.fromSource(source, WatermarkStrategy.forMonotonousTimestamps(),
                "Kafka Source");

        DataStream<MarketDataEvent> parsed = dataStream.map(new MapToMarketDataEvent())
                .filter(new FilterValidMarketDataEvent());

        parsed.print();

        env.execute(jobTitle);
    }

}
