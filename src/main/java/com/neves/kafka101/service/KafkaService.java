package com.neves.kafka101.service;

import com.neves.kafka101.schemas.MessageSchema;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final KafkaTemplate<String, MessageSchema> kafkaTemplateSchema;

    public void sendMessage(String topic, String key, String value) {
        kafkaTemplate.send(topic, key, value);
    }

    public void sendMessageWithSchema(String topic, String key, String content) {
        MessageSchema messageSchema = MessageSchema.newBuilder()
                .setUuid(UUID.randomUUID().toString())
                .setContent(content)
                .setTimestamp(System.currentTimeMillis())
                .build();
        kafkaTemplateSchema.send(topic, key, messageSchema);
    }

    @KafkaListener(topics = "input-topic", groupId = "redis-consumer-group", properties = {"auto.offset.reset = earliest"}, concurrency = "4")
    public void consumerRedis(ConsumerRecord<String, String> record) {
        System.out.println("Received Message to store on Redis! Key:" + record.key() + " Value: " + record.value());
    }

    @KafkaListener(topics = "input-topic", groupId = "db-consumer-group", properties = {"auto.offset.reset = earliest"}, concurrency = "4")
    public void consumerDatabase(ConsumerRecord<String, String> record) {
        System.out.println("Received Message to store on DB! Key:" + record.key() + " Value: " + record.value());
    }

    @KafkaListener(topics = "input-topic-schema", groupId = "consumer-group-schema", properties = {
            "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
            "spring.kafka.consumer.value-deserializer=io.confluent.kafka.serializers.KafkaAvroDeserializer",
            "spring.kafka.consumer.properties.schema.registry.url=http://localhost:8081"
    })
    public void consumerSchema(ConsumerRecord<String, GenericRecord> record) {
        System.out.println("Received Message! Key:" + record.key() + " Content: " + record.value());
    }

}
