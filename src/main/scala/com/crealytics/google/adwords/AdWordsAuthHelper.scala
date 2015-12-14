package com.crealytics.google.adwords

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.common.collect.Lists

class AdWordsAuthHelper(clientId: String, clientSecret: String) {

  val ADWORDS_API_SCOPE = "https://www.googleapis.com/auth/adwords"

  private val SCOPES = Lists.newArrayList(ADWORDS_API_SCOPE)

  private val CALLBACK_URL = "urn:ietf:wg:oauth:2.0:oob"
  val authorizationFlow: GoogleAuthorizationCodeFlow =
    new GoogleAuthorizationCodeFlow.Builder(new NetHttpTransport(), new JacksonFactory(),
      clientId, clientSecret, SCOPES)
      .setAccessType("offline")
      .build()
  val authorizationUrl: String =
    authorizationFlow.newAuthorizationUrl().setRedirectUri(CALLBACK_URL).build()

  def getRefreshToken(authorizationCode: String): String = {
    val tokenRequest = authorizationFlow.newTokenRequest(authorizationCode)
    tokenRequest.setRedirectUri(CALLBACK_URL)
    val tokenResponse = tokenRequest.execute()
    val credential = new GoogleCredential.Builder().setTransport(new NetHttpTransport())
      .setJsonFactory(new JacksonFactory())
      .setClientSecrets(clientId, clientSecret)
      .build()
    credential.setFromTokenResponse(tokenResponse)
    credential.getRefreshToken
  }
}
