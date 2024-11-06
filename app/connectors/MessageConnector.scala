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

import model.{ MessageCount, MessageListItem, MessagesCounts, MessagesWithCount, RenderMessageMetadata }
import play.api.http.Status
import play.api.i18n.Messages
import play.api.libs.json.*
import play.api.{ Configuration, Environment }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.{ URI, URL }
import java.util.Base64
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class MessageConnector @Inject() (
  http: HttpClientV2,
  val config: Configuration,
  val environment: Environment,
  servicesConfig: ServicesConfig
)(implicit
  ec: ExecutionContext
) extends Status {
  import MessageConnector.*

  private val secureMessageUrl: String = servicesConfig.baseUrl("secure-message") + "/secure-messaging"

  def messages(taxIdentifiers: List[String], regimes: List[String] = List(), language: String)(implicit
    hc: HeaderCarrier
  ): Future[Seq[MessageListItem]] = {
    val identifiersParams = formatQueryParam(taxIdentifiers = taxIdentifiers, regimes = regimes)
    val paramsWithLanguage = withLangQueryParam(language, identifiersParams)

    http.get(URI.create(s"$secureMessageUrl/messages$paramsWithLanguage").toURL).execute[MessagesWithCount].map {
      _.items
    }
  }

  def getMessageMetadata(
    id: String
  )(implicit hc: HeaderCarrier, messagesProvider: Messages): Future[RenderMessageMetadata] = {
    val messageId = Base64.getEncoder.encodeToString(id.getBytes("UTF-8"))
    http
      .get(URI.create(s"$secureMessageUrl/messages/$messageId?lang=${messagesProvider.lang.language}").toURL)
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

    http.get(URI.create(s"$secureMessageUrl/messages/count$identifiersQueryParam").toURL).execute[MessagesCounts].map {
      transform
    }
  }
}

object MessageConnector {
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
