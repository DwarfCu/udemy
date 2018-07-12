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
import sun.util.resources.cldr.gv.LocaleNames_gv;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class WordCountAppTests {

  TopologyTestDriver testDriver;

  StringSerializer stringSerializer = new StringSerializer();

  ConsumerRecordFactory<String, String> recordFactory = new ConsumerRecordFactory<>(stringSerializer, stringSerializer);

  @Before
  public void setUpTopologyTestDriver() {
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordCount-Tests");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "noneeded:9092");
    config.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    config.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

    WordCountApp wordCountApp = new WordCountApp();

    testDriver = new TopologyTestDriver(wordCountApp.createTopology(), config);
  }

  // Always include the following method
  @After
  public void closeTestDriver() {
    testDriver.close();
  }

  private void pushNewInputRecord(String value) {
    testDriver.pipeInput(recordFactory.create("word-count-input", null, value));
  }

  private ProducerRecord<String, Long> readOutput() {
    return testDriver.readOutput("word-count-output", new StringDeserializer(), new LongDeserializer());
  }

  @Test
  public void makeSureCountsAreCorrect() {
    String firstExample = "testing Kafka Streams";
    pushNewInputRecord(firstExample);
    OutputVerifier.compareKeyValue(readOutput(), "testing", 1L);
    OutputVerifier.compareKeyValue(readOutput(), "kafka", 1L);
    OutputVerifier.compareKeyValue(readOutput(), "streams", 1L);
    assertEquals(readOutput(), null);

    String secondExample = "testing Kafka again";
    pushNewInputRecord(secondExample);
    OutputVerifier.compareKeyValue(readOutput(), "testing", 2L);
    OutputVerifier.compareKeyValue(readOutput(), "kafka", 2L);
    OutputVerifier.compareKeyValue(readOutput(), "again", 1L);
    assertEquals(readOutput(), null);
  }

  public void makeSureWordsBecomeLowercase() {
    String upperCaseString = "KAFKA kafka Kafka";
    pushNewInputRecord(upperCaseString);
    OutputVerifier.compareKeyValue(readOutput(), "kafka", 1L);
    OutputVerifier.compareKeyValue(readOutput(), "kafka", 2L);
    OutputVerifier.compareKeyValue(readOutput(), "kafka", 3L);
  }
}
