FROM openjdk:8-jre
MAINTAINER Alexis Kinsella <alexis.kinsella@gmail.com>

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/app/bitcoin-price-kafka-producer-0.1.2-SNAPSHOT-shaded.jar"]

# Add the service itself
ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/app/bitcoin-price-kafka-producer-0.1.2-SNAPSHOT-shaded.jar