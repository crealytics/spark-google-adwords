name := "spark-google-adwords"

version := "0.8.2"

organization := "com.crealytics"

scalaVersion := "2.11.7"

spName := "crealytics/spark-google-adwords"

crossScalaVersions := Seq("2.10.5", "2.11.7")

sparkVersion := "1.5.2"

val testSparkVersion = settingKey[String]("The version of Spark to test against.")

testSparkVersion := sys.props.get("spark.testVersion").getOrElse(sparkVersion.value)

sparkComponents := Seq("core", "sql")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5" % "provided",
  "com.databricks" %% "spark-csv" % "1.3.0",
  "com.google.api-ads" % "ads-lib" % "2.8.0" exclude("commons-beanutils", "commons-beanutils"),
  "com.google.api-ads" % "adwords-axis" % "2.8.0" exclude("commons-beanutils", "commons-beanutils"),
  "commons-beanutils" % "commons-beanutils" % "1.9.2",
  "com.google.http-client" % "google-http-client-gson" % "1.21.0",
  "com.google.inject" % "guice" % "4.0",
  "com.google.inject.extensions" % "guice-assistedinject" % "4.0",
  "com.google.inject.extensions" % "guice-multibindings" % "4.0"
)

publishMavenStyle := true

spAppendScalaVersion := true

spIncludeMaven := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomExtra :=
  <url>https://github.com/crealytics/spark-google-adwords</url>
    <licenses>
      <license>
        <name>Apache License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:crealytics/spark-google-adwords.git</url>
      <connection>scm:git:git@github.com:crealytics/spark-google-adwords.git</connection>
    </scm>
    <developers>
      <developer>
        <id>nightscape</id>
        <name>Martin Mauch</name>
        <url>http://www.crealytics.com</url>
      </developer>
    </developers>

// Skip tests during assembly
test in assembly := {}

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("com.google.inject.**" -> "shaded.@0").inAll,
  ShadeRule.rename("org.apache.commons.**" -> "shaded.@0").inAll
)

addArtifact(artifact in(Compile, assembly), assembly)

// -- MiMa binary compatibility checks ------------------------------------------------------------

import com.typesafe.tools.mima.plugin.MimaKeys.{binaryIssueFilters, previousArtifact}
import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings

mimaDefaultSettings ++ Seq(
  previousArtifact := Some("com.crealytics" %% "spark-google-adwords" % "0.8.0"),
  binaryIssueFilters ++= Seq(
  )
)

// ------------------------------------------------------------------------------------------------
