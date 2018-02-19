package org.helyx.kafka.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

import com.fasterxml.jackson.databind.DeserializationFeature.{FAIL_ON_UNKNOWN_PROPERTIES, USE_BIG_DECIMAL_FOR_FLOATS}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.{INDENT_OUTPUT, WRITE_DATES_AS_TIMESTAMPS}
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer.ZONED_DATE_TIME
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonUtils {

  lazy val indentMapper = createMapper(true)
  lazy val defaultMapper = createMapper()

  def mapper(indent: Boolean = false): ObjectMapper with ScalaObjectMapper = {
    if (indent) indentMapper else defaultMapper
  }

  def createMapper(indent: Boolean = false): ObjectMapper with ScalaObjectMapper = {
    val mapper = new ObjectMapper() with ScalaObjectMapper

    mapper.registerModule(DefaultScalaModule)
    mapper.registerModule(new Jdk8Module())

    val module = new SimpleModule()
    module.addDeserializer(classOf[ZonedDateTime], ZONED_DATE_TIME)
    module.addSerializer(classOf[ZonedDateTime], new ZonedDateTimeSerializer(ISO_OFFSET_DATE_TIME))
    mapper.registerModule(module)

    mapper.configure(INDENT_OUTPUT, indent)
    mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false)
    mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.configure(USE_BIG_DECIMAL_FOR_FLOATS, true)

    mapper
  }

  def toJson(value: Any, indent: Boolean = false): String = {
    mapper(indent).writeValueAsString(value)
  }

  def fromJson[T](json: String)(implicit m : Manifest[T]): T = {
    mapper().readValue[T](json)
  }

}