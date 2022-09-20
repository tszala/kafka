package com.tszala.kafka.hello;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;

public class ConsumerDemoWithGroups {
    public static void main(String[] args) {
        var logger = LoggerFactory.getLogger(ConsumerDemoWithGroups.class);
        var groupId = "my-forth-app-1";
        var topic = "first_topic";
        var properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // create a consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // subscribe for a topic
        consumer.subscribe(List.of(topic));

        while(true) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
            for(ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                logger.info(String.format("Read record with key: %s and value: %s", consumerRecord.key(),consumerRecord.value()));
            }
        }

    }
}
