package com.neves.kafka101.controller;

import com.neves.kafka101.model.Message;
import com.neves.kafka101.model.WordCountResponse;
import com.neves.kafka101.service.KafkaService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class KafkaController {

    private final KafkaService kafkaService;
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @PostMapping("/publish")
    public ResponseEntity<Void> publishMessage(@RequestBody Message message) {
        kafkaService.sendMessage("input-topic", message.key(), message.value());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/count")
    public ResponseEntity<List<WordCountResponse>> getWordCountList() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType("counts", QueryableStoreTypes.keyValueStore())
        );
        List<WordCountResponse> words = new ArrayList<>();

        counts.all()
                .forEachRemaining(keyValue -> words.add(new WordCountResponse(keyValue.key, keyValue.value)));

        words.sort((w1, w2) -> w2.count().compareTo(w1.count()));

        return ResponseEntity.ok(words.subList(0, Math.min(words.size(), 10)));
    }

    @GetMapping("/count/{word}")
    public ResponseEntity<WordCountResponse> getWordCount(@PathVariable String word) {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType("counts", QueryableStoreTypes.keyValueStore())
        );

        Long wordCount = counts.get(word);
        return ResponseEntity.ok(new WordCountResponse(word, Objects.isNull(wordCount) ? 0L : wordCount));
    }
}