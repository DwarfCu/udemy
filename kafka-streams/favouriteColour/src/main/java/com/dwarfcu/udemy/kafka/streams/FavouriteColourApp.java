package com.dwarfcu.udemy.kafka.streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;

public class FavouriteColourApp {

  public Topology createTopology() {
    StreamsBuilder builder = new StreamsBuilder();

    KStream<String, String> input = builder.stream("favouriteColour-input");

    KStream<String, String> favouriteUserColour = input.mapValues(textline -> textline.toLowerCase())
        .filter((key,value) -> value.contains(","))
        .selectKey((ignoredKey,value) -> value.split(",")[0])
        .mapValues(value -> value.split(",")[1])
        .filter((user,colour) -> Arrays.asList("green","blue","red").contains(colour));

    favouriteUserColour.to("favouriteUserColour-output", Produced.with(Serdes.String(), Serdes.String()));

    KTable<String, Long> favouriteColours = builder.table("favouriteUserColour-output")
        .groupBy((user,colour) -> new KeyValue<>(colour.toString(),colour.toString()))
        .count("CountsByColours");

    favouriteColours.toStream().to("favouriteColours-output", Produced.with(Serdes.String(), Serdes.Long()));

    return builder.build();
  }

  public static void main(String[] args) {

    Properties config = new Properties();
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "favouriteColourApp");
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // NOT recommended in production environment
    config.setProperty(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");

    FavouriteColourApp favouriteColourApp = new FavouriteColourApp();

    KafkaStreams streams = new KafkaStreams(favouriteColourApp.createTopology(), config);

    // NOT in production, only for development
    streams.cleanUp();

    streams.start();

    streams.localThreadsMetadata().forEach(data -> System.out.println(data));

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }

}