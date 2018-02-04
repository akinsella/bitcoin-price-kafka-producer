package org.helyx.kafka.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

import com.fasterxml.jackson.databind.DeserializationFeature.{FAIL_ON_UNKNOWN_PROPERTIES, USE_BIG_DECIMAL_FOR_FLOATS}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer.ZONED_DATE_TIME
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonUtils {

  val mapper = new ObjectMapper() with ScalaObjectMapper

  mapper.registerModule(DefaultScalaModule)
  mapper.registerModule(new Jdk8Module())

  val module = new SimpleModule()
  module.addDeserializer(classOf[ZonedDateTime], ZONED_DATE_TIME)
  module.addSerializer(classOf[ZonedDateTime], new ZonedDateTimeSerializer(ISO_ZONED_DATE_TIME))
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