# WordCount Demo

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
    $KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --topic word-count-input --create --replication-factor 1 --partitions 2

    $KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --topic word-count-output --create --replication-factor 1 --partitions 2
```

### [Kafka Streams] WordCount instances

4) Download and build this project.
```
    cd /path/to/wordCount

    mvn clean package
```
5) Run as many instances of the WordCountApp class as partitions you setup before, or less.
```
    (Terminal 1)# java -jar target/wordCount-1.0-SNAPSHOT-jar-with-dependencies.jar
```
The output must be like to:
```
    ...
    stream-thread [wordCountAapp-b5d2d2d7-4a2e-479f-9910-043a7b73e126-StreamThread-1] partition assignment took 11 ms.
    current active tasks: [0_0, 0_1, 1_0, 1_1]
	current standby tasks: []
	previous active tasks: []
	...
```
```
    (Terminal 2)# java -jar target/wordCount-1.0-SNAPSHOT-jar-with-dependencies.jar
    ...
    stream-thread [wordCountAapp-aea68c8a-85fc-441c-b48b-dec7387e14be-StreamThread-1] partition assignment took 15 ms.
	current active tasks: [1_0, 1_1]
	current standby tasks: []
	previous active tasks: []
	...
```
Then, the active tasks from the output of the first instance must have changed:
```
    ...
    stream-thread [wordCountAapp-b5d2d2d7-4a2e-479f-9910-043a7b73e126-StreamThread-1] partition assignment took 15 ms.
	current active tasks: [0_0, 0_1]
	current standby tasks: []
	previous active tasks: [0_0, 0_1, 1_0, 1_1]
	...
```

### [Kafka Producer & Consumer] Tests

6) Run a Kafka PRODUCER for pushing messages into word-count-input and a Kafka CONSUMER from word-count-output topic
```
    $KAFKA_HOME/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic word-count-input
    hello world
    curso kafka streams
    kafka streams processing
```
```
    $KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --new-consumer --topic word-count-output --from-beginning --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
    hola	1
    mundo	1
    kafka	1
    streams	1
    curso	1
    kafka	2
    streams	2
    processing	1
```