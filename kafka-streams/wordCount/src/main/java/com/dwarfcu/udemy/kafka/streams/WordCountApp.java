package com.dwarfcu.udemy.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class WordCountApp {

  public Topology createTopology() {
    StreamsBuilder builder = new StreamsBuilder();

    KStream<String, String> wordCountInput = builder.stream("word-count-input");

    KTable<String, Long> wordCounts = wordCountInput
        .mapValues(textline -> textline.toLowerCase())
        .flatMapValues(loweredTextLine -> Arrays.asList((loweredTextLine.split(" "))))
        .selectKey((ignoredKey, word) -> word)
        .groupByKey()
        .count();

    wordCounts.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

    return builder.build();
  }

  public static void main(String[] args) {

    Properties config = new Properties();
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "wordCountApp");
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    WordCountApp wordCountApp = new WordCountApp();

    KafkaStreams streams = new KafkaStreams(wordCountApp.createTopology(), config);
    streams.start();

    System.out.println(streams.toString());

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

}
