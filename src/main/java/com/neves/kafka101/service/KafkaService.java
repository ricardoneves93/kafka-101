package com.neves.kafka101.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String key, String value) {
        kafkaTemplate.send(topic, key, value);
    }

    @KafkaListener(topics = "input-topic", groupId = "redis-consumer-group", properties = {"auto.offset.reset = earliest"}, concurrency = "4")
    public void consumerRedis(ConsumerRecord<String, String> record) {
        System.out.println("Received Message to store on Redis! Key:" + record.key() + " Value: " + record.value());
    }

    @KafkaListener(topics = "input-topic", groupId = "db-consumer-group", properties = {"auto.offset.reset = earliest"}, concurrency = "4")
    public void consumerDatabase(ConsumerRecord<String, String> record) {
        System.out.println("Received Message to store on DB! Key:" + record.key() + " Value: " + record.value());
    }

}
