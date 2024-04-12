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
import play.mvc.Http.Status.CREATED
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient, HttpResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class SecureMessageConnector @Inject() (httpClient: HttpClient, servicesConfig: ServicesConfig) extends Logging {

  private val secureMessageBaseUrl = servicesConfig.baseUrl("secure-message")

  def getInboxList(
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]],
    lang: Lang
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[List[MessageHeader]] = {
    val withLanguageParam = ("lang", lang.language)
    val queryParams =
      queryParamsBuilder(enrolmentKeys, customerEnrolments, tags).getOrElse(List()).++(List(withLanguageParam))
    httpClient
      .GET[List[MessageHeader]](s"$secureMessageBaseUrl/secure-messaging/messages", queryParams)
  }

  def getCount(
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Count] = {
    val queryParams = queryParamsBuilder(enrolmentKeys, customerEnrolments, tags)
    httpClient
      .GET[Count](s"$secureMessageBaseUrl/secure-messaging/messages/count", queryParams.getOrElse(List()))
  }

  private def queryParamsBuilder(
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]]
  ): Option[Seq[(String, String)]] =
    for {
      keysQueryParams: List[(String, String)] <- enrolmentKeys.map(keys => keys.map(ek => ("enrolmentKey", ek)))
      enrolmentsQueryParams: List[(String, String)] <-
        customerEnrolments
          .map(enrols => enrols.map(ce => ("enrolment", s"${ce.key}~${ce.name}~${ce.value}")))
      tagsQueryParams: List[(String, String)] <- tags.map(t => t.map(tag => ("tag", s"${tag.key}~${tag.value}")))
    } yield keysQueryParams concat enrolmentsQueryParams concat tagsQueryParams

  def getLetterContent(rawId: String, lang: Lang)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Letter] =
    httpClient.GET[Letter](s"$secureMessageBaseUrl/secure-messaging/messages/$rawId?lang=${lang.language}")

  def getConversationContent(rawId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Conversation] =
    httpClient.GET[Conversation](s"$secureMessageBaseUrl/secure-messaging/messages/$rawId")

  def saveCustomerMessage(id: String, message: CustomerMessage)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Boolean] =
    httpClient
      .POST[CustomerMessage, HttpResponse](
        s"$secureMessageBaseUrl/secure-messaging/messages/$id/customer-message",
        message
      )
      .map { response =>
        response.status match {
          case CREATED => true
          case status =>
            logger.error(s"POST of customer message failed. Got response status $status with message ${response.body}")
            false
        }
      }
}
