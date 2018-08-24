package com.crealytics.google.adwords

import java.util.zip.GZIPInputStream
import javax.xml.parsers.DocumentBuilderFactory

import com.google.api.ads.adwords.axis.factory.AdWordsServices
import com.google.api.ads.adwords.axis.v201806.cm.{ReportDefinitionField, ReportDefinitionServiceInterface}
import com.google.api.ads.adwords.axis.v201806.cm.{
  ReportDefinitionField,
  ReportDefinitionReportType,
  ReportDefinitionServiceInterface
}
import com.google.api.ads.adwords.lib.client.AdWordsSession
import com.google.api.ads.adwords.lib.jaxb.v201806.{DownloadFormat}
import com.google.api.ads.adwords.lib.jaxb.v201806.DownloadFormat
import com.google.api.ads.adwords.lib.utils.v201806.ReportDownloader
import com.google.api.ads.adwords.lib.utils.v201806.ReportDownloader
import com.google.api.ads.common.lib.auth.OfflineCredentials
import com.google.api.client.auth.oauth2.Credential

class AdWordsClient(credential: Credential, developerToken: String, userAgent: String, clientCustomerId: String) {

  // The Adwords API Session
  private lazy val session =
    new AdWordsSession.Builder()
      .withDeveloperToken(developerToken)
      .withUserAgent(userAgent)
      .withOAuth2Credential(credential)
      .withClientCustomerId(clientCustomerId)
      .build

  // Factory for all AdWords Services
  private lazy val services = new AdWordsServices()

  // Report Definition Service: Provides Metadata about Reports (Column Names, Data Types...)
  private lazy val reportDefinitionService =
    services.get(session, classOf[ReportDefinitionServiceInterface])

  // Returns Field Descriptors for all possible Fields of this Report
  def getFieldsForReportType(report: String): Array[ReportDefinitionField] =
    reportDefinitionService.getReportFields(ReportDefinitionReportType.fromValue(report))

  // Executes the Query, downloads the Report and parses the XML into a Sequence of Rows
  def downloadReport(query: String): Seq[Map[String, String]] = {
    // download the report
    val reportResponse = new ReportDownloader(session)
      .downloadReport(query, DownloadFormat.GZIPPED_XML)
    val inputStream = reportResponse.getInputStream
    val zipStream = new GZIPInputStream(inputStream)
    val xmlDocument =
      DocumentBuilderFactory.newInstance.newDocumentBuilder().parse(zipStream)
    val nodes = xmlDocument.getElementsByTagName("row")
    // Loop over the nodes
    (0 until nodes.getLength)
      .map(i => {
        val item = nodes.item(i)
        // All data is stored in attributes, so put the attributes into a map
        val attrs = item.getAttributes
        (0 until attrs.getLength)
          .map(j => (attrs.item(j).getNodeName, attrs.item(j).getNodeValue))
          .toMap
      })
      .toSeq
  }

}
