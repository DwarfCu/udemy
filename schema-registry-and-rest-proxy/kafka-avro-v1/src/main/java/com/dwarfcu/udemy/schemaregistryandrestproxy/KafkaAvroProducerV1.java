package com.dwarfcu.udemy.schemaregistryandrestproxy;

import com.example.Customer;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaAvroProducerV1 {

  public static void main(String[] args) {
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    properties.put(ProducerConfig.ACKS_CONFIG, "1");
    properties.put(ProducerConfig.RETRIES_CONFIG, "10");
    properties.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaAvroProducerV1");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
    properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://127.0.0.1:8081");

    KafkaProducer<String, Customer> kafkaProducer = new KafkaProducer<>(properties);

    String topic = "customer-avro";

    Customer customer = Customer.newBuilder()
        .setFirstName("John")
        .setLastName("Doe")
        .setAge(26)
        .setHeight(185.5f)
        .setWeight(85.6f)
        .setAutomatedEmail(false)
        .build();

    ProducerRecord<String, Customer> producerRecord = new ProducerRecord<>(topic, customer);

    kafkaProducer.send(producerRecord, new Callback() {
      @Override
      public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        if (e == null) {
          System.out.println("Sucess!");
          System.out.println(recordMetadata.toString());
        }
        else {
          e.printStackTrace();
        }
      }
    });

    kafkaProducer.flush();

    kafkaProducer.close();
  }

}
