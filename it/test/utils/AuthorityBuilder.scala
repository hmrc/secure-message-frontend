/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.utils

import config.AppConfig
import model.{ EPaye, HmrcPptOrg }

import javax.inject.{ Inject, Singleton }
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc.{ Cookie, CookieHeaderEncoding, SessionCookieBaker }
import play.api.{ Configuration, Environment, Logger }
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.domain._
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import play.api.libs.json.Reads._
import test.models.{ HmrcPodsOrg, HmrcPodsPpOrg }

import java.time.Instant
import play.api.libs.ws.writeableOf_String

case class AuthorityBuilder(json: JsObject, authProvider: TestAuthorisationProvider) {

  val logger = Logger(getClass)

  def sessionCookie(bearerToken: String): (String, String) = authProvider.cookieFor(bearerToken)

  private def jsonTransformer(jsonEnrolment: JsValue) =
    (__ \ "enrolments").json.update(of[JsArray].map { case JsArray(arr) => JsArray(arr :+ jsonEnrolment) })

  def taxIdToEnrolment(t: TaxIdentifier): AuthorityBuilder = t match {
    case SaUtr(utr)        => genericEnrolemnt("IR-SA", "UTR", utr)
    case CtUtr(utr)        => genericEnrolemnt("IR-CT", "UTR", utr)
    case Vrn(vrn)          => genericEnrolemnt("HMCE-VATDEC-ORG", "VATRegNo", vrn)
    case HmrcObtdsOrg(ern) => genericEnrolemnt("HMRC-OBTDS-ORG", "EtmpRegistrationNumber", ern)
    case HmrcPptOrg(ern)   => genericEnrolemnt("HMRC-PPT-ORG", "ETMPREGISTRATIONNUMBER", ern)
    case EPaye(epaye)      => genericEnrolemnt("IR-PAYE", "EMPREF", epaye)
    case HmrcMtdVat(vrn)   => genericEnrolemnt("HMRC-MTD-VAT", "VRN", vrn)
  }

  def genericEnrolemnt(key1: String, key2: String, value: String): AuthorityBuilder = {
    val entitlement =
      s"""
         |    {
         |      "key": "$key1",
         |      "identifiers": [
         |        {
         |          "key": "$key2",
         |          "value": "$value"
         |        }
         |      ],
         |      "state": "Activated"
         |    }
    """.stripMargin
    val jsonEnrolment = Json.parse(entitlement)
    val newJson = json.transform(jsonTransformer(jsonEnrolment)).asOpt.get
    AuthorityBuilder(newJson, authProvider)
  }

  def withSaUtr(saUtr: SaUtr): AuthorityBuilder = genericEnrolemnt("IR-SA", "UTR", saUtr.utr)

  def withCtUtr(ctUtr: CtUtr): AuthorityBuilder = genericEnrolemnt("IR-CT", "UTR", ctUtr.utr)

  def withVrn(vrn: Vrn): AuthorityBuilder = genericEnrolemnt("HMCE-VATDEC-ORG", "VATRegNo", vrn.vrn)

  def withFhdds(identifier: HmrcObtdsOrg): AuthorityBuilder =
    genericEnrolemnt("HMRC-OBTDS-ORG", "EtmpRegistrationNumber", identifier.value)

  def withPpt(identifier: HmrcPptOrg): AuthorityBuilder =
    genericEnrolemnt("HMRC-PPT-ORG", "ETMPREGISTRATIONNUMBER", identifier.value)

  def withPods(identifier: HmrcPodsOrg): AuthorityBuilder = genericEnrolemnt("HMRC-PODS-ORG", "PSAID", identifier.value)

  def withPodsPp(identifier: HmrcPodsPpOrg): AuthorityBuilder =
    genericEnrolemnt("HMRC-PODSPP-ORG", "PSPID", identifier.value)

  def withMtdVat(identifier: HmrcMtdVat): AuthorityBuilder = genericEnrolemnt("HMRC-MTD-VAT", "VRN", identifier.value)

  def withEPaye(identifier: EPaye): AuthorityBuilder = genericEnrolemnt("IR-PAYE", "EMPREF", identifier.value)

  def withNino(nino: Nino): AuthorityBuilder = {
    val newJson = json ++ Json.obj("nino" -> nino.value, "affinityGroup" -> "Individual")
    AuthorityBuilder(newJson, authProvider)
  }

  def asyncBearerTokenHeader(): Future[Option[(String, String)]] =
    authProvider.createBearerToken(json).map(x => x.map(h => (HeaderNames.AUTHORIZATION, h)))

  def bearerToken(): Future[Option[String]] = authProvider.createBearerToken(json)

  private def await[A](future: Future[A])(implicit timeout: Duration) = Await.result(future, timeout)

  def bearerTokenHeader()(implicit timeout: Duration): (String, String) =
    await(asyncBearerTokenHeader()) match {
      case Some(h) => h
      case None =>
        logger.error(
          "No BearerToken Header returned from Auth-Login-Api. Have you correctly configured your test data to sent to the auth?"
        )
        ("", "")
    }
}

@Singleton
class TestAuthorisationProvider @Inject() (
  val appConfig: AppConfig,
  val authConnector: AuthConnector,
  httpClient: WSClient,
  val runModeConfiguration: Configuration,
  val sessionCookieCrypto: SessionCookieCrypto,
  val sessionCookieBaker: SessionCookieBaker,
  val environment: Environment,
  cookieHeaderEncoding: CookieHeaderEncoding
) {

  lazy val authPort = 8500
  lazy val ggAuthPort = 8585

  def governmentGatewayAuthority(): AuthorityBuilder = AuthorityBuilder(authRequestJson, this)
  val authRequest =
    s"""
       |{
       |  "credId": "a-cred-id",
       |  "confidenceLevel": 200,
       |  "credentialStrength": "strong",
       |  "enrolments": [
       |  ],
       |  "usersName": "Lisa Nicole Brennan",
       |  "email": "lisa.brennan@some.domain.com",
       |  "description": "SJ Junior",
       |  "agentFriendlyName": "Johhny English",
       |  "agentCode": "Oh Oh Seven",
       |  "agentId": "007",
       |  "affinityGroup": "Individual",
       |  "itmpData": {
       |    "givenName": "Lisa",
       |    "middleName": "Nicole",
       |    "familyName": "Brennan",
       |    "birthdate": "1988-01-04",
       |    "address": {
       |      "line1": "10 The Causeway",
       |      "line2": "Little London",
       |      "line3": "Blackheath",
       |      "line4": "Surrey",
       |      "postCode": "GU99 ZBH",
       |      "countryName": "United Kingdom",
       |      "countryCode": "GB"
       |    }
       |  }
       |}
       """.stripMargin

  val authRequestJson: JsObject = Json.parse(authRequest).validate[JsObject].asOpt.get

  def createBearerToken(json: JsObject): Future[Option[String]] = buildUserToken(Json.stringify(json))

  private def keyValues(bearerToken: String, authProvider: String): Map[String, String] =
    Map(
      "authToken" -> bearerToken,
      "token"     -> "system-assumes-valid-token",
      "ap"        -> authProvider,
      "ts"        -> Instant.now().toEpochMilli.toString
    )

  def cookieFor(bearerToken: String): (String, String) = {
    val encrypted = sessionCookieCrypto.crypto
      .encrypt(PlainText(sessionCookieBaker.encode(keyValues(bearerToken, "GGW"))))
      .value
    HeaderNames.COOKIE -> cookieHeaderEncoding.encodeCookieHeader(Seq(Cookie("mdtp", encrypted)))
  }
  private def buildUserToken(payload: String): Future[Option[String]] =
    httpClient
      .url(s"http://localhost:$ggAuthPort/government-gateway/session/login")
      .withHttpHeaders(("Content-Type", "application/json"))
      .post(payload)
      .map(response => response.header(HeaderNames.AUTHORIZATION))

}
