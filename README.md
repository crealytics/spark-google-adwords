# Spark Google AdWords Library

[![Join the chat at https://gitter.im/spark-google-adwords/Lobby](https://badges.gitter.im/spark-google-adwords/Lobby.svg)](https://gitter.im/spark-google-adwords/Lobby?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A library for querying Google AdWords data with Apache Spark, for Spark SQL and DataFrames.

[![Build Status](https://travis-ci.org/crealytics/spark-google-adwords.svg?branch=master)](https://travis-ci.org/crealytics/spark-google-adwords)

## Requirements

This library requires Spark 1.4+

## Linking
You can link against this library in your program at the following coordinates:

### Scala 2.10
```
groupId: com.crealytics
artifactId: spark-google-adwords_2.10
version: 0.8.2
```
### Scala 2.11
```
groupId: com.crealytics
artifactId: spark-google-adwords_2.11
version: 0.8.2
```

## Using with Spark shell
This package can be added to  Spark using the `--packages` command line option.  For example, to include it when starting the spark shell:

### Spark compiled with Scala 2.11
```
$SPARK_HOME/bin/spark-shell --packages com.crealytics:spark-google-adwords_2.11:0.8.2
```

### Spark compiled with Scala 2.10
```
$SPARK_HOME/bin/spark-shell --packages com.crealytics:spark-google-adwords_2.10:0.8.2
```

## Features
This package allows querying Google AdWords reports as [Spark DataFrames](https://spark.apache.org/docs/latest/sql-programming-guide.html).
The API accepts several options (see the [Google AdWords developer docs](https://developers.google.com/adwords/api/docs/guides/start) for details):
* `clientId`, `clientSecret`: a client identifier and secret that you can
  [generate like this](https://developers.google.com/adwords/api/docs/guides/authentication#create_a_client_identifier_and_client_secret).
* `developerToken`: a token that [identifies your API activity](https://developers.google.com/adwords/api/faq#15113)
* `refreshToken`: a token that you can generate using the AdWordsAuthHelper as shown below.
  This token represents the user consent to grant access to a certain set of APIs
  and will be used to generate further, more short-lived access tokens which are actually used to authenticate calls
  to the AdWords API.
  For more information also see the [official documentation](https://developers.google.com/adwords/api/docs/guides/authentication#configure_and_use_a_client_library).
* `reportType`: The [report type](https://developers.google.com/adwords/api/docs/appendix/reports#report-types) you want to query.
  Use the same `CAPITALS_WITH_UNDERSCORE` spelling as in the listing.
* `clientCustomerId`: id of the account for which you want to query data.
* `userAgent` (optional, default = `Spark`): An arbitrary user-agent that will be used when querying the API.
* `during` (optional, default = `LAST_30_DAYS`): The time range for which you want to query data.
  Check the [official documentation](https://developers.google.com/adwords/api/docs/guides/reporting#date-ranges) for allowed values
  or use `StartDate,EndDate` for a custom date range.

### Scala API
__Spark 1.4+:__

Generate a refresh token (if you don't have one yet):
```scala
import com.crealytics.google.adwords._
val clientId = "123456789123-yourclientid.apps.googleusercontent.com"
val clientSecret = "yourclientsecret-1"
val authHelper = new AdWordsAuthHelper(clientId, clientSecret)

// The next line prints a URL that you have to open in the browser and copy the displayed authentication code
println(authHelper.authorizationUrl)

// Paste the authentication code from the browser window here to get the refresh token
println(authHelper.getRefreshToken("TheAuthenticationTokenFromTheBrowser"))
```

Create a DataFrame from an AdWords report:
```scala
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
val df = sqlContext.read
    .format("com.crealytics.google.adwords")
    .option("clientId", clientId)
    .option("clientSecret", clientSecret)
    .option("developerToken", "YourDeveloperToken")
    .option("refreshToken", "1/YourRefreshToken")
    .option("reportType", "SHOPPING_PERFORMANCE_REPORT")
    .option("clientCustomerId", "1234567890")
    .option("userAgent", "Spark")
    .option("during", "LAST_30_DAYS")
    .load()
```

## Building From Source
This library is built with [SBT](http://www.scala-sbt.org/0.13/docs/Command-Line-Reference.html). To build a JAR file simply run `sbt assembly` from the project root. The build configuration includes support for both Scala 2.10 and 2.11.
