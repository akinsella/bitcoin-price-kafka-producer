package org.helyx.kafka.actors

import java.util.concurrent.TimeUnit.{MINUTES, SECONDS}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCode
import akka.pattern.{AskTimeoutException, ask}
import org.helyx.kafka.actors.BtcPriceCoordinatorActor.Start
import org.helyx.kafka.actors.BtcPriceFetcherActor.FetchPrice
import org.helyx.kafka.models.Quote

import scala.concurrent.duration._
import scala.util.{Failure, Success}

object BtcPriceCoordinatorActor {
  case object Start
}

class BtcPriceCoordinatorActor(system: ActorSystem, priceFetcherActor: ActorRef, kafkaProducerActor:ActorRef) extends Actor with ActorLogging {

  override def preStart(): Unit = {
    log.info(s"Pre Start actor: ${self.path.name}")
  }

  override def postStop(): Unit = {
    log.info(s"Post Stop actor: ${self.path.name}")
  }

  def receive = {

    case Start =>
      val scheduler = system.scheduler

      implicit val ec = context.dispatcher

      val task = new Runnable {
        def run() {
          priceFetcherActor.ask(FetchPrice)(60 seconds) andThen {
            case Success(quote:Quote) =>
              log.info(s"Received Success with Quote from price fetcher - Quote: $quote")
              kafkaProducerActor ! quote
            case Success(sc:StatusCode) =>
              log.info(s"Received Success with Error Status Code from price fetcher - Status code: $sc")
            case Failure(e) =>
              log.info(s"Received Failure from price fetcher: ${e.getMessage}")
            case _ =>
              log.error("Should not go here")
          }
        }
      }

      scheduler.schedule(
        initialDelay = Duration(0, SECONDS),
        interval = Duration(1, MINUTES),
        runnable = task)

    case quote: Quote =>
      log.info(s"Received Quote from price fetcher: $quote")
      log.info(s"Sending quote to kafka with producer ...")
      kafkaProducerActor ! quote

    case code: StatusCode =>
      // handle the failure
      log.error(s"Error code received: $code")

    case Failure(e: AskTimeoutException) =>
      // handle the failure
      log.error(s"No Quotation received: ${e.getMessage}")

  }

}
