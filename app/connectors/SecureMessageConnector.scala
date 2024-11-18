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
import model.{ MessageCount, MessageListItem, MessagesCounts, MessagesWithCount, RenderMessageMetadata }
import models.{ Conversation, Count, CustomerMessage, Letter, MessageHeader }
import play.api.Logging
import play.api.i18n.{ Lang, Messages }
import play.api.libs.json.{ Json, Reads, __ }
import play.mvc.Http.Status.CREATED
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.{ URI, URLEncoder }
import java.util.Base64
import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }

class SecureMessageConnector @Inject() (httpClient: HttpClientV2, servicesConfig: ServicesConfig)(implicit
  ec: ExecutionContext
) extends Logging {
  import SecureMessageConnector.*
  private val secureMessageBaseUrl = servicesConfig.baseUrl("secure-message") + "/secure-messaging"

  def getInboxList(
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]],
    lang: Lang
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[List[MessageHeader]] = {
    val queryParams: String =
      queryParamsBuilder(enrolmentKeys, customerEnrolments, tags, List(("lang", lang.language))).getOrElse("")

    httpClient
      .get(new URI(s"$secureMessageBaseUrl/messages" + queryParams).toURL)
      .execute[List[MessageHeader]]
  }

  def getCount(
    enrolmentKeys: Option[List[String]],
    customerEnrolments: Option[List[CustomerEnrolment]],
    tags: Option[List[Tag]]
  )(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Count] = {
    val queryParams: String = queryParamsBuilder(enrolmentKeys, customerEnrolments, tags, Nil).getOrElse("")

    httpClient
      .get(new URI(s"$secureMessageBaseUrl/messages/count" + queryParams).toURL)
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
      .get(new URI(s"$secureMessageBaseUrl/messages/$rawId?lang=${lang.language}").toURL)
      .execute[Letter]

  def getConversationContent(rawId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Conversation] =
    httpClient.get(new URI(s"$secureMessageBaseUrl/messages/$rawId").toURL).execute[Conversation]

  def saveCustomerMessage(id: String, message: CustomerMessage)(implicit
    ec: ExecutionContext,
    hc: HeaderCarrier
  ): Future[Boolean] =
    httpClient
      .post(
        new URI(s"$secureMessageBaseUrl/messages/$id/customer-message").toURL
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

  def messages(taxIdentifiers: List[String], regimes: List[String] = List(), language: String)(implicit
    hc: HeaderCarrier
  ): Future[Seq[MessageListItem]] = {
    val identifiersParams = formatQueryParam(taxIdentifiers = taxIdentifiers, regimes = regimes)
    val paramsWithLanguage = withLangQueryParam(language, identifiersParams)

    httpClient
      .get(URI.create(s"$secureMessageBaseUrl/messages$paramsWithLanguage").toURL)
      .execute[MessagesWithCount]
      .map {
        _.items
      }
  }

  def getMessageMetadata(
    id: String
  )(implicit hc: HeaderCarrier, messagesProvider: Messages): Future[RenderMessageMetadata] = {
    val messageId = Base64.getEncoder.encodeToString(id.getBytes("UTF-8"))
    httpClient
      .get(URI.create(s"$secureMessageBaseUrl/messages/$messageId?lang=${messagesProvider.lang.language}").toURL)
      .execute[RenderMessageMetadata]
  }

  def messageCount(
    transform: MessagesCounts => MessageCount,
    taxIdentifiers: List[String],
    regimes: List[String] = List()
  )(implicit
    hc: HeaderCarrier
  ): Future[MessageCount] = {
    implicit def messagesCountsReads: Reads[MessagesCounts] =
      (__ \ "count").read[MessagesCounts](MessagesCounts.format)

    val identifiersQueryParam = formatQueryParam(taxIdentifiers, regimes = regimes)

    httpClient
      .get(URI.create(s"$secureMessageBaseUrl/messages/count$identifiersQueryParam").toURL)
      .execute[MessagesCounts]
      .map {
        transform
      }
  }
}

object SecureMessageConnector {
  def formatQueryParam(
    taxIdentifiers: List[String],
    alwaysAppend: Boolean = false,
    regimes: List[String] = List()
  ): String =
    (taxIdentifiers.map(("taxIdentifiers", _)) ++ regimes.map(("regimes", _)))
      .foldLeft("") {
        case (accumulator, (name, identifier)) if accumulator.isEmpty && !alwaysAppend => s"?$name=$identifier"
        case (accumulator, (name, identifier)) => s"$accumulator&$name=$identifier"
      }

  def withLangQueryParam(language: String, queryParams: String): String = {
    val langParam = s"lang=$language"
    if (queryParams.startsWith("?")) {
      s"$queryParams&$langParam"
    } else {
      s"?$langParam"
    }
  }
}
