package com.dwarfcu.udemy.kafka.streams;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.apache.kafka.streams.test.OutputVerifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class FavouriteColourAppTests {

  private TopologyTestDriver topologyTestDriver;

  private StringSerializer stringSerializer = new StringSerializer();

  private ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(stringSerializer, stringSerializer);

  @Before
  public void setUpTopologyTestDriver() {
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "favouriteColour-Tests");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "noneeded:9092");
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

    FavouriteColourApp favouriteColourApp = new FavouriteColourApp();

    topologyTestDriver = new TopologyTestDriver(favouriteColourApp.createTopology(), config);
  }

  // Always include the following method
  @After
  public void closeTestDriver() {
    topologyTestDriver.close();
  }

  private void pushNewInputRecord(String value) {
    topologyTestDriver.pipeInput(recordFactory.create("favouriteColour-input", null, value));
  }

  private ProducerRecord<String, Long> readOutput() {
    return topologyTestDriver.readOutput("favouriteColours-output", new StringDeserializer(), new LongDeserializer());
  }

  @Test
  public void containsSeparatorChar() {
    String inputWithComma = "jamie,green";
    pushNewInputRecord(inputWithComma);
    OutputVerifier.compareKeyValue(readOutput(), "green", 1L);
  }

  @Test
  public void noContainsSeparatorChar() {
    String inputWithoutComma = "jamieblack";
    pushNewInputRecord(inputWithoutComma);
    assertEquals(readOutput(), null);
  }

  @Test
  public void makeSureFavouriteColoursAreCorrect() {
    String firstExample = "stephane,blue";
    pushNewInputRecord(firstExample);
    OutputVerifier.compareKeyValue(readOutput(), "blue", 1L);

    String secondExample = "john,green";
    pushNewInputRecord(secondExample);
    OutputVerifier.compareKeyValue(readOutput(), "green", 1L);

    String thirdExample = "Stephane,red";
    pushNewInputRecord(thirdExample);
    OutputVerifier.compareKeyValue(readOutput(), "blue", 0L);
    OutputVerifier.compareKeyValue(readOutput(), "red", 1L);
  }
}