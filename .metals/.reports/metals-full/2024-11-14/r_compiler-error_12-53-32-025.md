file://<WORKSPACE>/market_data_analysis/src/main/java/com/linusjern/marketanalysis/SparkConsumerApp.java
### java.util.NoSuchElementException: next on empty iterator

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 807
uri: file://<WORKSPACE>/market_data_analysis/src/main/java/com/linusjern/marketanalysis/SparkConsumerApp.java
text:
```scala
package com.linusjern.marketanalysis;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;



public class SparkConsumerApp {
    public static void main(String[] args) throws Exception {
        SparkSession spark = SparkSession
            .builder()
            .appName("JavaStructuredNetworkWordCount")
            .config("spark.master", "local")
            .getOrCreate();
        Dataset<Row> df = spark
            .readStream()
            .format("kafka")
            .option("kafka.bootstrap.servers", "localhost:9092")
            .option("subscribe", "market-data")
            .load();
        df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)");


        StreamingQuery query = wordCount@@s.writeStream()
  .outputMode("complete")
  .format("console")
  .start();
        df.
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