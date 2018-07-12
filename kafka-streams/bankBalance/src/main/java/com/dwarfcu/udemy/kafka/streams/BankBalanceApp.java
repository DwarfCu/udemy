package com.dwarfcu.udemy.kafka.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.apache.kafka.connect.json.JsonSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import java.time.Instant;
import java.util.Properties;

public class BankBalanceApp {
  public static void main(String[] args) {

    // json Serde
    final Serializer<JsonNode> jsonSerializer = new JsonSerializer();
    final Deserializer<JsonNode> jsonDeserializer = new JsonDeserializer();
    final Serde<JsonNode> jsonSerde = Serdes.serdeFrom(jsonSerializer, jsonDeserializer);

    Properties config = new Properties();
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "BankBalanceApp");
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    //config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    //config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, jsonSerde.getClass().getName());

    // NOT recommended in production environment
    config.setProperty(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

    // Exactly once processing!!!
    config.setProperty(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

    StreamsBuilder builder = new StreamsBuilder();

    KStream<String, JsonNode> transactions = builder.stream("bankBalance-input");

    // Create initial JSON
    // Option 1)
    //ObjectMapper mapper = new ObjectMapper();
    //JsonNode initialBalance = mapper.createObjectNode();
    //((ObjectNode) initialBalance).put("count", 0);
    //((ObjectNode) initialBalance).put("balance", 0);
    //((ObjectNode) initialBalance).put("time", Instant.ofEpochMilli(0L).toString());

    // Option 2)
    ObjectNode initialBalance = JsonNodeFactory.instance.objectNode();
    initialBalance.put("count", 0);
    initialBalance.put("balance", 0);
    initialBalance.put("time", Instant.ofEpochMilli(0L).toString());


    KTable<String, JsonNode> bankBalance = transactions
        .selectKey((ignoredKey, value) -> value.get("name").asText())
        .groupByKey(Serdes.String(), jsonSerde)
        .aggregate(
            () -> initialBalance,
            (key, transaction, balance) -> newBalance(transaction, balance),
            jsonSerde,
            "bankBalance-agg"
        );

    bankBalance.to(Serdes.String(), jsonSerde,"bankBalance-output");

    KafkaStreams streams = new KafkaStreams(builder.build(), config);
    streams.start();

    System.out.println(streams.toString());

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

  private static JsonNode newBalance(JsonNode transaction, JsonNode balance) {
    ObjectNode newBalance = JsonNodeFactory.instance.objectNode();
    newBalance.put("count", balance.get("count").asInt() + 1);
    newBalance.put("balance", balance.get("balance").asInt() + transaction.get("amount").asInt());

    Long balanceEpoch = Instant.parse(balance.get("time").asText()).toEpochMilli();
    Long transactionEpoch = Instant.parse(transaction.get("time").asText()).toEpochMilli();
    Instant newBalanceInstant = Instant.ofEpochMilli(Math.max(balanceEpoch, transactionEpoch));
    newBalance.put("time", newBalanceInstant.toString());
    return newBalance;
  }
}