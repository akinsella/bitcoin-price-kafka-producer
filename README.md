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
docker run <registry-host>:<registry:port>/bitcoin-price-kafka-producer:<version> -t <topic> -b <kafka-server-1>:<port>,<kafka-server-2>:<port>,<kafka-server-3>:<port>
```

