package com.linusjern.marketanalysis;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        // Kafka producer properties
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", "localhost:9092");
        kafkaProps.put("key.serializer", StringSerializer.class.getName());
        kafkaProps.put("value.serializer", StringSerializer.class.getName());

        String topic = "market-data";
        String csvFilePath = "src/main/resources/debs.csv";

        // Initialize Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(kafkaProps);

        // Read CSV and send each line to Kafka
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            // Skip the first line (header)
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split(",", -1);

                String id = splitLine[0];
                String secType = splitLine[1];
                String lastPrice = splitLine[21];
                String lastTime = splitLine[23];
                String lastDate = splitLine[26];

                if (lastTime.isBlank()) {
                    continue;
                }

                String toSend = String.join(",", id, secType, lastPrice, lastTime, lastDate);
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, toSend);
                producer.send(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
}
