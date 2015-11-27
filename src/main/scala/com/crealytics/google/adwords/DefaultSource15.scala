package com.crealytics.google.adwords

import org.apache.spark.sql.sources.DataSourceRegister

class DefaultSource15 extends DefaultSource with DataSourceRegister {
  override def shortName(): String = "google-adwords"
}
