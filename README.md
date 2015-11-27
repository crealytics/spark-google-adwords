# Spark Google AdWords Library

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
version: 0.8.0
```
### Scala 2.11
```
groupId: com.crealytics
artifactId: spark-google-adwords_2.11
version: 0.8.0
```

## Using with Spark shell
This package can be added to  Spark using the `--packages` command line option.  For example, to include it when starting the spark shell:

### Spark compiled with Scala 2.11
```
$SPARK_HOME/bin/spark-shell --packages com.crealytics:spark-google-adwords_2.11:0.8.0
```

### Spark compiled with Scala 2.10
```
$SPARK_HOME/bin/spark-shell --packages com.crealytics:spark-google-adwords_2.10:0.8.0
```

## Features
This package allows querying Google AdWords reports as [Spark DataFrames](https://spark.apache.org/docs/latest/sql-programming-guide.html).
The API accepts several options (see the [Google AdWords developer docs](https://developers.google.com/adwords/api/docs/guides/start) for details):
* `clientId`, `clientSecret`: a client identifier and secret that you can
  [generate like this](https://developers.google.com/adwords/api/docs/guides/authentication#create_a_client_identifier_and_client_secret).
* `developerToken`: 
* `refreshToken`: a token that you can
  [generate like this](https://developers.google.com/adwords/api/docs/guides/authentication#configure_and_use_a_client_library).
  It is a time-based token and has to be refreshed after a certain time.
* `reportType`: the service from which you want to query data.
* `clientCustomerId`: id of the account for which you want to query data.
* `userAgent` (optional, default = `Spark`): An arbitrary user-agent that will be used when querying the API.
* `during` (optional, default = `LAST_30_DAYS`): The time range for which you want to query data.

### Scala API
__Spark 1.4+:__

Automatically infer schema (data types), otherwise everything is assumed string:
```scala
import org.apache.spark.sql.SQLContext

val sqlContext = new SQLContext(sc)
val df = sqlContext.read
    .format("com.crealytics.google.adwords")
    .option("clientId", "")
    .option("clientSecret", "")
    .option("developerToken", "")
    .option("refreshToken", "")
    .option("reportType", "")
    .option("clientCustomerId", "")
    .option("userAgent", "")
    .option("during", "")
    .load()
```

## Building From Source
This library is built with [SBT](http://www.scala-sbt.org/0.13/docs/Command-Line-Reference.html), which is automatically downloaded by the included shell script. To build a JAR file simply run `sbt/sbt package` from the project root. The build configuration includes support for both Scala 2.10 and 2.11.
