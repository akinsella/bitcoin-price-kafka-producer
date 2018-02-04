package org.helyx.kafka.models

import java.time.ZonedDateTime


/**
  *
  * val jsonBody = """
  *|{
      *|  "time": {
      *|    "updated": "Feb 2, 2018 16:48:00 UTC",
      *|    "updatedISO": "2018-02-02T16:48:00+00:00",
      *|    "updateduk": "Feb 2, 2018 at 16:48 GMT"
      *|  },
      *|  "disclaimer": "This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org",
      *|  "chartName": "Bitcoin",
      *|  "bpi": {
      *|    "USD": {
      *|      "code": "USD",
      *|      "symbol": "&#36;",
      *|      "rate": "9,035.6925",
      *|      "description": "United States Dollar",
      *|      "rate_float": 9035.6925
      *|    },
      *|    "GBP": {
      *|      "code": "GBP",
      *|      "symbol": "&pound;",
      *|      "rate": "6,397.6769",
      *|      "description": "British Pound Sterling",
      *|      "rate_float": 6397.6769
      *|    },
      *|    "EUR": {
      *|      "code": "EUR",
      *|      "symbol": "&euro;",
      *|      "rate": "7,266.0341",
      *|      "description": "Euro",
      *|      "rate_float": 7266.0341
      *|    }
      *|  }
      *|}
    *""".stripMargin
 *
  */

case class BitcoinPriceIndex(code: String, symbol: String, description: String, rate_float: Double)

case class QuoteTime(updatedISO:ZonedDateTime)

case class Quote(time:QuoteTime, disclaimer: String, chartName: String, bpi: Map[String, BitcoinPriceIndex])

