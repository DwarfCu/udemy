# FavouriteColour Demo

### Environment

0) Download and deploy **Apache Kafka 1.1.0**. Remember set/export $KAFKA_HOME environment var.

1) Start a Zookeeper Server (default configuration):
    ```
    $KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties
    ```
2) Start a Kafka Broker (default configuration):
    ```
    $KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties
    ```
3) Create topics (if not created yet):
    ```
    $KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --topic favouriteColour-input --create --replication-factor 1 --partitions 1

    $KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --topic favouriteUserColour-output --create --replication-factor 1 --partitions 1 --config cleanup.policy=compact
    
    $KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --topic favouriteColours-output --create --replication-factor 1 --partitions 1 --config cleanup.policy=compact
    ```

### [Kafka Streams] FavouriteColour instances

1) Download and build this project.
    ```
    cd /path/to/favouriteColour

    mvn clean package
    ```
2) Run as many instances of the FavouriteColourApp class as partitions you setup before, or less.
    ```
    java -jar target/favouriteColour-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```
### [Kafka Producer & Consumer] Tests
1) Run Kafka CONSUMERS.
    ```
    $KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --new-consumer --topic favouriteUserColour-output --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
    
    $KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --new-consumer --topic favouriteColours-output --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
    ```
2) Run a Kafka PRODUCER for pushing messages into *favouriteColour-input*.
     ```
     $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic favouriteColour-input
     
     stephane,blue
     john,green
     Stephane,red
     alice,red
     ```
3) The output of the first consumer would be something similar to:
    ```
    stephane	blue
    john	green
    stephane	red
    alice	red
    ```

4) And the output of the second consumer:
    ```
    blue	1
    green	1
    blue	0
    red	1
    red	2
    ```