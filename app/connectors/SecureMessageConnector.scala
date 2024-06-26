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

package connectors

import controllers.generic.models.{ CustomerEnrolment, Tag }
import models.{ Conversation, Count, CustomerMessage, Letter, MessageHeader }
import play.api.Logging
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.mvc.Http.Status.CREATED
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.{ URL, URLEncoder }
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class SecureMessageConnector @Inject() (httpClient: HttpClientV2, servicesConfig: ServicesConfig) extends Logging {

  private val secureMessageBaseUrl = servicesConfig.baseUrl("secure-message")

  def getInboxList(
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]],
    lang: Lang
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[List[MessageHeader]] = {
    val queryParams: String =
      queryParamsBuilder(enrolmentKeys, customerEnrolments, tags, List(("lang", lang.language))).getOrElse("")
    val urlString: URL = new URL(s"$secureMessageBaseUrl/secure-messaging/messages" + queryParams)
    httpClient
      .get(urlString)
      .execute[List[MessageHeader]]
  }

  def getCount(
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Count] = {
    val queryParams: String = queryParamsBuilder(enrolmentKeys, customerEnrolments, tags, Nil).getOrElse("")
    val urlString: String = s"$secureMessageBaseUrl/secure-messaging/messages/count" + queryParams
    httpClient
      .get(new URL(urlString))
      .execute[Count]
  }

  private def queryParamsBuilder(
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]],
    languageParams: List[(String, String)]
  ) = {
    val queryParams = for {
      keysQueryParams: List[(String, String)] <- enrolmentKeys.map(keys => keys.map(ek => ("enrolmentKey", ek)))
      enrolmentsQueryParams: List[(String, String)] <-
        customerEnrolments
          .map(enrols => enrols.map(ce => ("enrolment", s"${ce.key}~${ce.name}~${ce.value}")))
      tagsQueryParams: List[(String, String)] <- tags.map(t => t.map(tag => ("tag", s"${tag.key}~${tag.value}")))
    } yield keysQueryParams concat enrolmentsQueryParams concat tagsQueryParams concat languageParams

    def makeQueryString(queryParams: Seq[(String, String)]) = {
      val paramPairs = queryParams.map { case (k, v) => s"$k=${URLEncoder.encode(v, "utf-8")}" }
      if (paramPairs.isEmpty) "" else paramPairs.mkString("?", "&", "")
    }

    queryParams.map(makeQueryString)
  }

  def getLetterContent(rawId: String, lang: Lang)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Letter] =
    httpClient
      .get(new URL(s"$secureMessageBaseUrl/secure-messaging/messages/$rawId?lang=${lang.language}"))
      .execute[Letter]

  def getConversationContent(rawId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Conversation] =
    httpClient.get(new URL(s"$secureMessageBaseUrl/secure-messaging/messages/$rawId")).execute[Conversation]

  def saveCustomerMessage(id: String, message: CustomerMessage)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Boolean] =
    httpClient
      .post(
        new URL(s"$secureMessageBaseUrl/secure-messaging/messages/$id/customer-message")
      )
      .withBody(Json.toJson(message))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case CREATED => true
          case status =>
            logger.error(s"POST of customer message failed. Got response status $status with message ${response.body}")
            false
        }
      }
}
