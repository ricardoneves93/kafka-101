package com.neves.kafka101.configuration;

import com.neves.kafka101.schemas.MessageSchema;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;


@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfiguration {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${spring.kafka.schema-registry-url}")
    private String schemaRegistryUrl;


    // Producer configuration

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, MessageSchema> producerFactoryWithSchema() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);

        // Schema Registry configuration
        configProps.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaTemplate<String, MessageSchema> kafkaTemplateWithSchema() {
        return new KafkaTemplate<>(producerFactoryWithSchema());
    }

    // Streams configuration
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, "streams-app");
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name().toLowerCase());
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0); // Disable KTable caching
        props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        return new KafkaStreamsConfiguration(props);
    }

    /**
     * Simple stream that prints the key and value of the incoming messages
     */
//    @Bean
    public KStream<String, String> simpleStream(StreamsBuilder streamsBuilder) {
        return streamsBuilder
                .stream("input-topic", Consumed.with(Serdes.String(), Serdes.String()))
                .peek((key, value) -> System.out.println("Stream received message Key: " + key + " Value: " + value));
    }

    /**
     * Stream that decomposes the incoming messages into individual words and writes them to single-word topic
     */
//    @Bean
    public KStream<String, String> decomposeIntoWordsStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> wordsStream = streamsBuilder
                .stream("input-topic", Consumed.with(Serdes.String(), Serdes.String()))
                .map((key, value) -> KeyValue.pair(key, value.toLowerCase()))
                .flatMapValues(value -> Arrays.asList(value.split("\\W+")));

        wordsStream.to("single-word", Produced.with(Serdes.String(), Serdes.String()));

        return wordsStream;
    }

    /**
     * Stream that counts the number of occurrences of each word and saves in a KTable called counts
     */
    @Bean
    public KStream<String, String> wordCountStream(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder
                .stream("input-topic", Consumed.with(Serdes.String(), Serdes.String()))
                .map((key, value) -> KeyValue.pair(key, value.toLowerCase()))
                .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
                .selectKey((ignoredKey, individualWord) -> individualWord)
                .selectKey((key, value) -> value);

        stream.groupByKey()
                .count(Materialized.as("counts"));

        return stream;
    }
}
