package org.helyx.kafka.actors

import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Status}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.ByteString
import org.helyx.kafka.actors.BtcPriceFetcherActor.FetchPrice
import org.helyx.kafka.models.Quote
import org.helyx.kafka.utils.JsonUtils.fromJson


object BtcPriceFetcherActor {
  case object FetchPrice
}

case class UnexpectedHttpStatusCodeException(code: StatusCode, private val message: String = "", private val cause: Throwable = None.orNull)
  extends Exception(message, cause)


class BtcPriceFetcherActor(uri: String = "https://api.coindesk.com/v1/bpi/currentprice.json")(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  val http = Http(context.system)

  override def preStart(): Unit = {
    log.info(s"Pre Start actor: ${self.path.name}")
  }

  override def postStop(): Unit = {
    log.info(s"Post Stop actor: ${self.path.name}")
  }

  override def receive: Receive = {
    case FetchPrice =>
      log.info(s"Checking bitcoin price ( uri: $uri ) ...")
      http.singleRequest(HttpRequest(GET, uri)) pipeTo self
      context become waitingForHttpResponse(sender())
  }

  private def waitingForHttpResponse(origin: ActorRef): Receive = {
    case HttpResponse(OK, _, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        val jsonBody = body.utf8String
        log.info(s"Received response with body: $jsonBody")
        log.info(s"Deserializing received response body ...")
        val quote = fromJson[Quote](jsonBody)
        log.info(s"Response body deserialized as Quote: $quote")
        log.info(s"Returning Quote to sender ...")
        origin ! quote
      }

    case resp@HttpResponse(code, _, _, _) =>
      log.warning(s"Request failed, response code: $code")
      resp.discardEntityBytes()
      origin ! Failure(UnexpectedHttpStatusCodeException(code))
  }

}
