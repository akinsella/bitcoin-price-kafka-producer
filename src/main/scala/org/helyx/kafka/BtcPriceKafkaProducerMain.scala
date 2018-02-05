package org.helyx.kafka

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.helyx.kafka.actors.BtcPriceCoordinatorActor.Start
import org.helyx.kafka.actors.{BtcPriceCoordinatorActor, BtcPriceFetcherActor, BtcPriceKafkaProducerActor}
import org.slf4j.LoggerFactory.getLogger

case class Arguments(topic: String = "", brokers: Seq[String] = Seq())

class BtcPriceKafkaProducerMain

object BtcPriceKafkaProducerMain extends BtcPriceKafkaProducerMain with App {

  val logger = getLogger(classOf[BtcPriceKafkaProducerMain])

  val config = ConfigFactory.load()

  val parser = new scopt.OptionParser[Arguments]("scopt") {
    head("Bitcoin Price Kafka Producer", "1.x")

    opt[String]('t', "topic").action( (topic, config) =>
      config.copy(topic = topic) ).text("topic to send")

    opt[Seq[String]]('b', "broker-list").valueName("<broker-1>,<broker-2>...").action( (brokers, config) =>
      config.copy(brokers = brokers)
    ).text("kafka brokers")

    help("help").text("prints this usage text")

  }

  // parser.parse returns Option[C]
  parser.parse(args, Arguments()) match {
    case Some(args) =>
      run(args.brokers, args.topic, config)
    case None =>
      System.exit(0)
  }

  def run(brokers: Seq[String], topic: String, config:Config): Unit = {

    logger.info(s"Brokers: $brokers")
    logger.info(s"Topic: $topic")

    implicit val system: ActorSystem = ActorSystem("bitcoin-price-akka-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val priceFetcherActor = system.actorOf(Props(new BtcPriceFetcherActor()), "price-fetcher")
    val kafkaProducerActor = system.actorOf(Props(new BtcPriceKafkaProducerActor(brokers, "BitcoinPriceKafkaProducer", topic)), "kafka-producer")

    val coordinatorActor = system.actorOf(Props(new BtcPriceCoordinatorActor(priceFetcherActor, kafkaProducerActor)), "coordinator")

    coordinatorActor ! Start
  }

}