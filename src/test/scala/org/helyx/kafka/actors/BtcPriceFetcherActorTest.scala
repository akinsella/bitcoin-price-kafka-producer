package org.helyx.kafka.actors

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.PathMatcher
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import org.helyx.kafka.actors.BtcPriceFetcherActor.FetchPrice
import org.helyx.kafka.models.{BitcoinPriceIndex, Quote, QuoteTime}
import org.helyx.kafka.server.http.RestServer
import org.helyx.kafka.server.http.Routes.Get
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}
import org.slf4j.Logger
import org.slf4j.LoggerFactory.getLogger

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}

class BtcPriceFetcherActorTest extends FunSuite with Matchers with BeforeAndAfter {

  val logger: Logger = getLogger(classOf[BtcPriceFetcherActorTest])

  implicit val system: ActorSystem = ActorSystem("btc-price-coordinator")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  var server: RestServer = _


  before {
    val route = Get(PathMatcher("v1") / "bpi" / "currentprice.json",
      """
        |{
        |  "time": {
        |    "updated": "Feb 2, 2018 16:48:00 UTC",
        |    "updatedISO": "2018-02-02T16:48:00+00:00",
        |    "updateduk": "Feb 2, 2018 at 16:48 GMT"
        |  },
        |  "disclaimer": "This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org",
        |  "chartName": "Bitcoin",
        |  "bpi": {
        |    "USD": {
        |      "code": "USD",
        |      "symbol": "&#36;",
        |      "rate": "9,035.6925",
        |      "description": "United States Dollar",
        |      "rate_float": 9035.6925
        |    },
        |    "GBP": {
        |      "code": "GBP",
        |      "symbol": "&pound;",
        |      "rate": "6,397.6769",
        |      "description": "British Pound Sterling",
        |      "rate_float": 6397.6769
        |    },
        |    "EUR": {
        |      "code": "EUR",
        |      "symbol": "&euro;",
        |      "rate": "7,266.0341",
        |      "description": "Euro",
        |      "rate_float": 7266.0341
        |    }
        |  }
        |}
      """.stripMargin)
    server = new RestServer(route).start()
  }

  after {
    server.shutdown()
  }

  test("Get Price Quote") {
    implicit val executionContext: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout = Timeout(5 seconds)

    val priceFetcherActor = system.actorOf(Props(new BtcPriceFetcherActor("http://localhost:8080/v1/bpi/currentprice.json")), "price-fetcher")

    val quote = Await.result[Any](priceFetcherActor ? FetchPrice, timeout.duration).asInstanceOf[Quote]
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

    quote shouldEqual expectedQuote
  }

}
