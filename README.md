# Kafka 101

### What is this repository for?

It presents some basic concepts of kafka:

* Kafka Producer
* Kafka Consumer
* Kafka Streams
* Kafka Connect
* KSQL

### How do I get set up?

First you need to run the docker-compose file to start the kafka broker, zookeeper, AKHQ and kafka connect.

```bash
docker-compose up -d
```

After having all services up and running you can start the Spring Boot application.
The spring boot application explores Kafka Producer, Kafka Consumer, Kafka Streams and also interacts with kafka
connect.

```bash
mvn spring-boot:run
```

#### Kafka Producer and Consumer

There is an endpoint `/publish` that will publish a message to the topic `input-topic` using a Kafka Producer.
To demonstrate Consumer Groups concept there are two Kafka Consumers that were created with `@Listener` annotation.

#### Kafka Streams

1. There is the `simpleStream` that consumes from the topic `input-topic` and logs the message's keys and values.
2. There is the `decomposeIntoWordsStream` that consumer from the topic `input-topic` and decompose the message's value
   into words and publishes them to the topic `single-word` topic.
3. There is the `wordCountStream` that consumes from the topic `input-topic` and counts the number of occurrences of
   each word by saving them in a state store called `counts`

#### Kafka Connect

There is a source connector plugin that is already installed in alongside with Kafka connect docker-compose service.
It is a IRC connector that will consume from the IRC channel `#kafka` and publish the messages to the topic `irc-kafka`.
We need to create an connector instance as well, there are many ways you can achieve this, in this case, the
kafka-connect REST  API will be used, you can find the configuration in the `irc-source-connector.sh`.
Check the Kafka Connect logs with `docker logs -f kafka-connect` to see the connector instance being created with no
errors.

You can also check ig the connector was created by invoking kafka connect restful api. You can execute
```curl http://localhost:8083/connectors``` to get a list of all connectors, you can execute
```curl http://localhost:8083/connectors/irc-source-connector``` to get the connector instance configuration.
You can also call ```curl "http://localhost:8083/connectors?expand=status"``` to get the connector status.

In order to test the connector, you can go to http://www.dal.net:9090/ and join the channel `#programming` and start 
sending messages. The connector should consume the messages and publish them to the configured topic `irc-messages`.

If you want to see the messages you can open the AKHQ that is running on http://localhost:8082/.


