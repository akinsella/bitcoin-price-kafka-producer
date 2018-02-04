package org.helyx.kafka.actors

import java.util.Properties
import java.util.concurrent.TimeUnit.SECONDS

import akka.actor.{Actor, ActorLogging}
import org.apache.kafka.clients.producer.ProducerConfig.{BOOTSTRAP_SERVERS_CONFIG, CLIENT_ID_CONFIG, KEY_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER_CLASS_CONFIG}
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer
import org.helyx.kafka.models.Quote
import org.helyx.kafka.utils.JsonUtils.toJson;

object BtcPriceKafkaProducerActor {
  case object Stop
}

class BtcPriceKafkaProducerActor(brokers:Seq[String], clientId:String, topic: String) extends Actor with ActorLogging {

  log.info(s"Brokers: $brokers")
  log.info(s"Topic: $topic")

  var producer:KafkaProducer[String, String] = _

  override def preStart():Unit = {
    log.info(s"Pre Start actor: ${self.path.name}")

    val props = new Properties()
    props.put(BOOTSTRAP_SERVERS_CONFIG, brokers.mkString(","))
    props.put(CLIENT_ID_CONFIG, clientId)
    props.put(KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    // props.put("schema.registry.url", classOf[KafkaAvroSerializer].getName)

    producer = new KafkaProducer[String, String](props)
  }

  override def postStop(): Unit = {
    log.info(s"Post Stop actor: ${self.path.name}")
    producer.close(30, SECONDS)
  }

  override def receive: Receive = {
    case quote: Quote =>

      log.info(s"Sending info to kafka on topic '$topic' ...")
      producer.send(new ProducerRecord(topic, quote.time.updatedISO.toString, toJson(quote)), new Callback {
        override def onCompletion(recordMetadata: RecordMetadata, e: Exception): Unit = {
          if (e != null) {
            log.error(s"Failed to send message to kafka on topic '$topic': ${e.getMessage}", e)
          } else {
            println(s"Successfully sent message to kafka on topic '$topic'")
          }
        }
      })

  }

}
