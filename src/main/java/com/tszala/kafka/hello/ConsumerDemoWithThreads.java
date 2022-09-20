package com.tszala.kafka.hello;

import ch.qos.logback.core.net.server.ConcurrentServerRunner;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThreads {
    public static void main(String[] args) {
        ConsumerDemoWithThreads consumerDemoWithThreads = new ConsumerDemoWithThreads();
        consumerDemoWithThreads.run();
    }

    private void run() {
        Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThreads.class);
        String groupId = "my-forth-app-1";
        String topic = "first_topic";

        CountDownLatch latch = new CountDownLatch(1);
        String bootstrapServers = "localhost:9092";
        logger.info("Creating consumer runnable");
        Runnable consumerRunnable = new ConsumerRunnable(latch, bootstrapServers,groupId,topic);
        Thread consumerThread = new Thread(consumerRunnable);
        consumerThread.start();

        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            logger.info("Caught shutdown hook");
            ((ConsumerRunnable) consumerRunnable).shutdown();
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted", e);
        } finally {
            logger.info("Application is closing");
        }
    }

    static class ConsumerRunnable implements Runnable {

        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;
        private Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class);

        ConsumerRunnable(CountDownLatch latch, String bootstrapServers, String groupId, String topic) {
            this.latch = latch;
            Properties properties = new Properties();
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            this.consumer = new KafkaConsumer<String, String>(properties);
            // subscribe for a topic
            consumer.subscribe(List.of(topic));
        }

        @Override
        public void run() {
            try {
                while (true) {
                    ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                    for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                        logger.info(String.format("Read record with key: %s and value: %s", consumerRecord.key(), consumerRecord.value()));
                    }
                }
            } catch (WakeupException e) {
                logger.info("Received shutdown signal");
            } finally {
                consumer.close();
                // tell the main code we are done with the consumer
                latch.countDown();
            }
        }

        public void shutdown() {
            consumer.wakeup();
        }
    }

}
