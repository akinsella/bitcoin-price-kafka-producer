package org.helyx.kafka.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME

import org.helyx.kafka.models.{BitcoinPriceIndex, Quote, QuoteTime}
import org.helyx.kafka.utils.JsonUtils.fromJson
import org.scalatest.{FunSuite, Matchers}

class JsonUtilsTest extends FunSuite with Matchers {

  test("JSON body is correctly deserialized") {

    val jsonBody = """
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
    """.stripMargin

    val actualQuote = fromJson[Quote](jsonBody)

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

    actualQuote shouldEqual expectedQuote

  }

}