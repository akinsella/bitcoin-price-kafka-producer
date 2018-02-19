[![Build Status](https://travis-ci.org/akinsella/bitcoin-price-kafka-producer)](https://travis-ci.org/akinsella/bitcoin-price-kafka-producer)

== Build status

### Description
Program sample using akka and pushing to kafka: Fetch current bitcoin price and store it to kafka.
It uses coindesk API to get bitcoin price informations.

### Run the program
``` sh
java -jar bitcoin-price-kafka-producer-<version>-shaded.jar -t <topic> -b <kafka-server-1>:<port>,<kafka-server-2>:<port>,<kafka-server-3>:<port>
```

### Package the program
``` sh
mvn clean package
```

### Build Docker Image
``` sh
mvn clean package dockerfile:build
```

### Push to docker registry
``` sh
mvn dockerfile:push -Ddockerfile.username=<useranme> -Ddockerfile.password=<password> 
```

### Run in docker
``` sh
docker run <registry-host>:<registry-port>/bitcoin-price-kafka-producer:<version> -t <topic> -b <kafka-server-1>:<port>,<kafka-server-2>:<port>,<kafka-server-3>:<port>
```

### Create Topic
``` sh
bin/kafka-topics.sh --zookeeper <zookeeper_server>:2181 --create --topic btc-price --replication-factor 3 --partitions 6
```

### Delete Topic (Needs to set: delete.topic.enable: "true")
``` sh
bin/kafka-topics.sh --zookeeper <zookeeper_server>:2181 --delete --topic btc-price
```

### List Topics
``` sh
bin/kafka-topics.sh --zookeeper <zookeeper_server>:2181 --list
```

### Wurstmeister docker-compose.yml:
```
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
  kafka:
    build: .
    ports:
      - "9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: <EXPECTED_IP>
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_DELETE_TOPIC_ENABLE: "true"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock

```

### Consume Topic
``` sh
bin/kafka-console-consumer.sh --from-beginning --bootstrap-server <kafka-server-1>:<port> --topic btc-price
```
