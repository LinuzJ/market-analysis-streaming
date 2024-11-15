file://<WORKSPACE>/market_data_analysis/src/main/java/com/linusjern/marketanalysis/SparkConsumerApp.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 1665
uri: file://<WORKSPACE>/market_data_analysis/src/main/java/com/linusjern/marketanalysis/SparkConsumerApp.java
text:
```scala
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

        // Write stream to console
        StreamingQuery query;
        try {
            query = formattedDf.writeStream()
                    .outputMode("append")   // Print only new rows as they arrive
                    .format("console")
                    .trigger(Trigger.ProcessingTime("5 seconds"))  // Set a processing interval
                    .start();
        } catch (Exception e) {
            // TODO Auto-generated c@@atch block
            e.printStackTrace();
        }

        // Await termination of the query
        query.awaitTermination();

        // Close the Spark session
        spark.close();
    }
}

```



#### Error stacktrace:

```
scala.collection.Iterator$$anon$19.next(Iterator.scala:973)
	scala.collection.Iterator$$anon$19.next(Iterator.scala:971)
	scala.collection.mutable.MutationTracker$CheckedIterator.next(MutationTracker.scala:76)
	scala.collection.IterableOps.head(Iterable.scala:222)
	scala.collection.IterableOps.head$(Iterable.scala:222)
	scala.collection.AbstractIterable.head(Iterable.scala:935)
	dotty.tools.dotc.interactive.InteractiveDriver.run(InteractiveDriver.scala:164)
	dotty.tools.pc.MetalsDriver.run(MetalsDriver.scala:45)
	dotty.tools.pc.HoverProvider$.hover(HoverProvider.scala:40)
	dotty.tools.pc.ScalaPresentationCompiler.hover$$anonfun$1(ScalaPresentationCompiler.scala:376)
```
#### Short summary: 

java.util.NoSuchElementException: next on empty iterator