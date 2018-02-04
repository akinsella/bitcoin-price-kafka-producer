package org.helyx.kafka.utils

import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.Locale

import com.fasterxml.jackson.databind.DeserializationFeature.{FAIL_ON_UNKNOWN_PROPERTIES, USE_BIG_DECIMAL_FOR_FLOATS}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.deser.{InstantDeserializer}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonUtils {

  val mapper = new ObjectMapper() with ScalaObjectMapper

  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new Jdk8Module())

  val module = new SimpleModule()
  //  (DateTimeFormatter.ofPattern(""MMM d, yyyy HH:mm:ss z"")
  module.addDeserializer(classOf[ZonedDateTime], InstantDeserializer.ZONED_DATE_TIME)
  mapper.registerModule(module)

  mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(USE_BIG_DECIMAL_FOR_FLOATS, true)

  def toJson(value: Map[Symbol, Any]): String = {
    toJson(value map { case (k,v) => k.name -> v})
  }

  def toJson(value: Any): String = {
    mapper.writeValueAsString(value)
  }

  def toMap[V](json:String)(implicit m: Manifest[V]) = fromJson[Map[String,V]](json)

  def fromJson[T](json: String)(implicit m : Manifest[T]): T = {
    mapper.readValue[T](json)
  }

}