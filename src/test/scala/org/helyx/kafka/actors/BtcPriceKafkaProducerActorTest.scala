package org.helyx.kafka.actors

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import java.util.Properties
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.PathMatcher
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.landoop.kafka.testing.KCluster
import kafka.cluster.Broker
import kafka.utils.TestUtils
import kafka.utils.TestUtils.createNewConsumer
import org.apache.kafka.clients.consumer.ConsumerConfig.{KEY_DESERIALIZER_CLASS_CONFIG, VALUE_DESERIALIZER_CLASS_CONFIG}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.security.auth.SecurityProtocol.PLAINTEXT
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.helyx.kafka.actors.BtcPriceFetcherActor.FetchPrice
import org.helyx.kafka.models.{BitcoinPriceIndex, Quote, QuoteTime}
import org.helyx.kafka.server.http.RestServer
import org.helyx.kafka.server.http.Routes.Get
import org.helyx.kafka.utils.JsonUtils
import org.helyx.kafka.utils.JsonUtils.fromJson
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}

class BtcPriceKafkaProducerActorTest extends FunSuite with Matchers with BeforeAndAfter {

  val logger: Logger = getLogger(classOf[BtcPriceKafkaProducerActorTest])

  implicit val system: ActorSystem = ActorSystem("btc-price-coordinator")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  var cluster: KCluster = _
  var consumer: KafkaConsumer[Array[Byte], Array[Byte]] = _
  val topic = "btc-price"

  before {
    cluster = new KCluster(schemaRegistryEnabled = false)

    cluster.createTopic("btc-price")

//    val consumerProperties = new Properties()
//    consumerProperties.putAll(
//      Map[String, String](
//        KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringSerializer].getName,
//        VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringSerializer].getName
//      ).asJava
//    )

    consumer = createNewConsumer(cluster.BrokersList, securityProtocol = PLAINTEXT/*, props = Some(consumerProperties)*/)
    consumer.subscribe(Seq(topic).asJava)
  }

  after {
    consumer.unsubscribe()
    consumer.close(5, SECONDS)
    cluster.close()
  }

  test("Get Price Quote") {
    implicit val executionContext: ExecutionContext = system.dispatcher

    val kafkaProducerActor = system.actorOf(Props(classOf[BtcPriceKafkaProducerActor], cluster.BrokersList.split(",").toSeq, "kafka-producer", topic), "kafka-producer")

    val expectedQuote = Quote(
      QuoteTime(
        ZonedDateTime.parse("2018-02-02T16:48:00+00:00[UTC]", ISO_ZONED_DATE_TIME)
      ),
      "This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org",
      "Bitcoin",
      Map(
        "USD" -> BitcoinPriceIndex("USD", "&#36;", "United States Dollar", 9035.6925),
        "GBP" -> BitcoinPriceIndex("GBP", "&pound;", "British Pound Sterling", 6397.6769),
        "EUR" -> BitcoinPriceIndex("EUR", "&euro;", "Euro", 7266.0341)
      )
    )

    kafkaProducerActor ! expectedQuote

    val record = consumer.poll(10000).iterator().next()
    val recordValue = new StringDeserializer().deserialize(topic, record.value())

    val actualQuote = fromJson[Quote](recordValue)

    actualQuote shouldEqual expectedQuote
  }

}
