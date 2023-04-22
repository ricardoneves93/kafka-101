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
The spring boot application explores Kafka Producer, Kafka Consumer, Kafka Streams and also interacts with kafka connect.

```bash
mvn spring-boot:run
```

#### Kafka Producer and Consumer
There is an endpoint `/publish` that will publish a message to the topic `input-topic` using a Kafka Producer.
To demonstrate Consumer Groups concept there are two Kafka Consumers that were created with `@Listener` annotation.

#### Kafka Streams



