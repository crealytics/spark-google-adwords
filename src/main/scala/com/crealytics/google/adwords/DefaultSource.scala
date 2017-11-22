package com.crealytics.google.adwords

import com.google.api.ads.common.lib.auth.OfflineCredentials
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.sources._

class DefaultSource extends RelationProvider {

  // The default User Agent
  private final val DEFAULT_USER_AGENT = "Spark"
  // Default During Clause: 30 days
  private final val DEFAULT_DURING = "LAST_30_DAYS"

  /**
    * Creates a new relation for retrieving data from Google AdWords
    * Parameters must include clientId, clientSecret, developerToken, refreshToken, reportType, clientCustomerId
    * Optionally you can also specify userAgent and during
    */
  override def createRelation(sqlContext: SQLContext, parameters: Map[String, String]): AdWordsRelation = {

    // gather parameters
    val clientId = checkParameter(parameters, "clientId")
    val clientSecret = checkParameter(parameters, "clientSecret")
    val developerToken = checkParameter(parameters, "developerToken")
    val refreshToken = checkParameter(parameters, "refreshToken")
    val reportType = checkParameter(parameters, "reportType")
    val clientCustomerId = checkParameter(parameters, "clientCustomerId")

    val userAgent =
      parameterOrDefault(parameters, "userAgent", DEFAULT_USER_AGENT)
    val duringStmt = parameterOrDefault(parameters, "during", DEFAULT_DURING)
    // Our OAuth2 Credential
    val credential = new OfflineCredentials.Builder()
      .forApi(OfflineCredentials.Api.ADWORDS)
      .withClientSecrets(clientId, clientSecret)
      .withRefreshToken(refreshToken)
      .build
      .generateCredential
    // create relation
    AdWordsRelation(credential, developerToken, clientCustomerId, userAgent, reportType, duringStmt)(sqlContext)
  }

  // Forces a Parameter to exist, otherwise an exception is thrown.
  private def checkParameter(map: Map[String, String], param: String) = {
    if (!map.contains(param)) {
      throw new IllegalArgumentException(s"Parameter ${'"'}$param${'"'} is missing in options.")
    } else {
      map.apply(param)
    }
  }

  // Gets the Parameter if it exists, otherwise returns the default argument
  private def parameterOrDefault(map: Map[String, String], param: String, default: String) =
    map.getOrElse(param, default)
}
