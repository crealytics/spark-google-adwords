package com.crealytics.google.adwords

import java.math.BigDecimal
import java.sql.{Date, Timestamp}
import java.text.NumberFormat
import java.util.Locale

import com.google.api.client.auth.oauth2.Credential
import org.apache.spark.rdd.RDD
import org.apache.spark.sql._
import org.apache.spark.sql.sources._
import org.apache.spark.sql.types._

import scala.util.Try

case class AdWordsRelation(
  credential: Credential, developerToken: String, clientCustomerId: String,
  userAgent: String, reportType: String, duringStmt: String)
  (@transient val sqlContext: SQLContext)
extends BaseRelation with TableScan with PrunedScan with PrunedFilteredScan {
  private val client =
    new AdWordsClient(credential, developerToken, userAgent, clientCustomerId)

  val googleSchema = client.getFieldsForReportType(reportType)
  override val schema: StructType = googleSchema.foldLeft(new StructType) {
    case (struct, column) =>
      struct.add(column.getFieldName, sparkDataTypeForGoogleDataType(column.getFieldType))
  }
  val fieldNameXMLNameLookupMap = googleSchema.map(col => (col.getFieldName, col.getXmlAttributeName)).toMap

  // These two just forward to the buildScan() defined below
  override def buildScan: RDD[Row] = buildScan(schema.map(_.name).toArray, Array())

  override def buildScan(requiredColumns: Array[String]): RDD[Row] = buildScan(requiredColumns, Array())

  // Creates an AWQL Query from the selected Columns and Filters
  def createQuery(columns: Array[String], filters: Array[Filter]): String = {
    // Make sure at least one column is selected
    val cols = if (columns.isEmpty) Array[String](schema.apply(0).name) else columns
    // create the query
    val conditionStmt = if (filters.nonEmpty) s"WHERE ${combineFilters(filters)}" else ""
    s"""
    SELECT ${cols.mkString(", ")}
    FROM $reportType
    $conditionStmt
    DURING $duringStmt
    """
  }

  // Extracts a subset of Columns of the Table Schema
  def getSchemaForColumns(columns: Array[String]): StructType = {
    val cols = schema.filter(row => columns.contains(row.name))
    columns.foldLeft(new StructType) {
      case (struct, col) => struct.add(cols.filter(_.name == col).head)
    }
  }

  // Executes the Query and fetches Results
  override def buildScan(requiredColumns: Array[String], filters: Array[Filter]): RDD[Row] = {
    val query = createQuery(requiredColumns, filters)
    val result = executeQuery(query, getSchemaForColumns(requiredColumns))
    sqlContext.sparkContext.parallelize(result.map(Row.fromSeq))
  }

  // Combines the Filters in AWQL Style
  def combineFilters(filters: Array[Filter]): String = {
    def convertFilter(filter: Filter): String = filter match {
      case EqualTo(attribute, value) => s"$attribute = $value"
      case Not(EqualTo(attribute, value)) => s"$attribute != $value"
      case EqualNullSafe(attribute, value) => s"$attribute = $value"
      case Not(EqualNullSafe(attribute, value)) => s"$attribute != $value"
      case GreaterThan(attribute, value) => s"$attribute > $value"
      case GreaterThanOrEqual(attribute, value) => s"$attribute >= $value"
      case LessThan(attribute, value) => s"$attribute < $value"
      case LessThanOrEqual(attribute, value) => s"$attribute <= $value"
      case In(attribute, values) => s"$attribute IN [${values.mkString(",")}]"
      case Not(In(attribute, values)) => s"$attribute NOT_IN [${values.mkString(",")}]"
      case And(lhs, rhs) => Seq(lhs, rhs).map(convertFilter).mkString(" AND ")
      case StringStartsWith(attribute, value) => s"$attribute STARTS_WITH $value"
      case StringEndsWith(attribute, value) => ???
      case StringContains(attribute, value) => s"$attribute CONTAINS $value"
      case Not(StringContains(attribute, value)) => s"$attribute DOES_NOT_CONTAIN $value"
      case Or(lhs, rhs) => ???
      case IsNull(attribute) => ???
      case IsNotNull(attribute) => ???
      case Not(filt) => ???
    }
    filters.map(convertFilter).mkString(" AND ")
  }

  // Convert from Google Data Types to Spark Data Types
  private def sparkDataTypeForGoogleDataType(dataType: String) = dataType match {
    case "String" => "STRING"
    case "Money" => "DOUBLE"
    case "Double" => "DOUBLE"
    case "Long" => "LONG"
    case "Date" => "TIMESTAMP"
    case "Enum" => "STRING"
    case "DayOfWeek" => "INTEGER"
    case "Integer" => "INTEGER"
    case "MonthOfYear" => "INTEGER"
    case "Byte" => "BYTE"
    case "Boolean" => "BOOLEAN"
    case "boolean" => "BOOLEAN"
    case "Bid" => "DOUBLE"
    case "long" => "LONG"
    case "DateTime" => "TIMESTAMP"
    case "int" => "INTEGER"
    case _ => "STRING"
  }

  // Cast a String to a Spark Data Type
  private def castTo(datum: String, castType: DataType): Any = {
    castType match {
      case _: ByteType => datum.toByte
      case _: ShortType => datum.toShort
      case _: IntegerType => datum.toInt
      case _: LongType => datum.toLong
      case _: FloatType => Try(datum.toFloat)
        .getOrElse(NumberFormat.getInstance(Locale.getDefault).parse(datum).floatValue())
      case _: DoubleType => Try(datum.toDouble)
        .getOrElse(NumberFormat.getInstance(Locale.getDefault).parse(datum).doubleValue())
      case _: BooleanType => datum.toBoolean
      case _: DecimalType => new BigDecimal(datum.replaceAll(",", ""))
      case _: TimestampType => Timestamp.valueOf(datum)
      case _: DateType => Date.valueOf(datum)
      case _: StringType => datum
      case _ => throw new RuntimeException(s"Unsupported type: ${castType.typeName}")
    }
  }

  @annotation.tailrec
  private final def retry[T](n: Int)(fn: => T): util.Try[T] = {
    util.Try(fn) match {
      case x: util.Success[T] => x
      case _ if n > 1 => retry(n - 1)(fn)
      case f => f
    }
  }

  // Execute the Query and align the Results according to the Schema
  private def executeQuery(query: String, schema: StructType): Seq[Seq[Any]] = {
    val response = retry(3)(client.downloadReport(query)).get
    // Transform the Strings to their data types
    response.map(row => {
      schema.map(col => {
        val name = fieldNameXMLNameLookupMap(col.name)
        val value = row(name)
        castTo(value, col.dataType)
      }).seq
    })
  }
}
