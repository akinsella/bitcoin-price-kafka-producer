package org.helyx.kafka.server.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer

import scala.concurrent.Future

object Routes {
  def Get(pathMatcher: PathMatcher0, payload:String) = {
    path(pathMatcher) {
      get {
        complete { payload }
      }
    }
  }

}

class RestServer(val route: Route)(implicit val system: ActorSystem, val materializer: ActorMaterializer) {

  implicit val executionContext = system.dispatcher

  var binding: Future[Http.ServerBinding] = _

  def start(address: String = "localhost", port: Int = 8080):RestServer = {
    binding = Http().bindAndHandle(route, address, port)
    this
  }

  def shutdown():Future[Unit] = {
    binding.flatMap(_.unbind())
  }

}
