package com.linusjern.marketanalysis;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;

public class SparkConsumerApp {
    public static void main(String[] args) throws StreamingQueryException {
        // Initialize Spark session
        SparkSession spark = SparkSession
                .builder()
                .appName("MarketDataKafkaConsumer")
                .config("spark.master", "local[*]")  // Use all available cores
                .getOrCreate();

        // Read data from Kafka topic
        Dataset<Row> df = spark
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "market-data")
                .option("startingOffsets", "latest") // Start reading new messages only
                .load();

        // Select key and value as strings and print to console
        Dataset<Row> formattedDf = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)");

        try {
            // Write stream to console
            StreamingQuery query = formattedDf.writeStream()
                    .outputMode("append")   // Print only new rows as they arrive
                    .format("console")
                    .trigger(Trigger.ProcessingTime("5 seconds"))  // Set a processing interval
                    .start();

            // Await termination of the query
            query.awaitTermination();
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Close the Spark session
        spark.close();
    }
}
