# BankBalance Demo

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
$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --topic bankBalance-input --create --replication-factor 1 --partitions 2

$KAFKA_HOME/bin/kafka-topics.sh --zookeeper localhost:2181 --topic bankBalance-output --create --replication-factor 1 --partitions 2
```
Instead of creating the class for the Kafka Producer as requested in the Kafka Streams course, I've chosen to generate a random dataset using the online tool **Mockaroo** and the command ***kafka-producer-perf-test.sh***.
##### [Mockaroo] Generate Dataset.
Mockaroo: The maximum download size for free accounts is 1,000 rows, but you can generate as many 'datasets' as you want. Therefore, you can repeat, or automate ;), these steps to build a bigger dataset.
1) Download a dataset.
```
curl "https://api.mockaroo.com/api/9b0d87e0?count=1000&key=9ae0b0d0" > "Udemy-KafkaStreams-BankBalanceDataset1.json"
```
2) Add the last dataset.
``` 
cat Udemy-KafkaStreams-BankBalanceDataset1.json >> Udemy-KafkaStreams-BankBalanceDataset.json
```
3) Repeat steps 1 and 2.
4) Finally edit the dataset.
```
vim Udemy-KafkaStreams-BankBalanceDataset.json
```
Apply the following expressions to remove '\[' char and replace ']' char:
```
:1,$ s/\[//g
:1,$ s/\]/,\r/g
```
##### [Kafka] Producer
Run *kafka-producer-perf-test.sh* command:
``` 
kafka-producer-perf-test.sh --topic bankBalance-input --num-records 10000 --throughput 100 --payload-file Udemy-KafkaStreams-BankBalanceDataset.json --producer-props bootstrap.servers=localhost:9092
```
Run *kafka-console-consumer.sh* command to check the topic:
```
kafka-console-consumer.sh --zookeeper localhost:2181 --topic bankBalance-input --from-beginning
```