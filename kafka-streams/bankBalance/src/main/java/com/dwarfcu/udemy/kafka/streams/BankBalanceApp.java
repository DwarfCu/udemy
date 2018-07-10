package com.dwarfcu.udemy.kafka.streams;


import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Arrays;
import java.util.Properties;

public class BankBalanceApp {
  public static void main(String[] args) {

    Properties config = new Properties();
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "BankBalanceApp");
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    StreamsBuilder builder = new StreamsBuilder();

    KStream<String, String> wordCountInput = builder.stream("bankBalance-input");

    KTable<String, Long> wordCounts = wordCountInput.mapValues(textline -> textline.toLowerCase())
        .flatMapValues(loweredTextLine -> Arrays.asList((loweredTextLine.split(" "))))
        .selectKey((ignoredKey, word) -> word)
        .groupByKey()
        .count();

    wordCounts.toStream().to(Serdes.String(), Serdes.Long(),"bankBalance-output");

    KafkaStreams streams = new KafkaStreams(builder.build(), config);
    streams.start();

    System.out.println(streams.toString());

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }
}