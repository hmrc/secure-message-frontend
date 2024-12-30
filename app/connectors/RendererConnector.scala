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

import model.ServiceUrl
import play.api.mvc.RequestHeader
import play.api.{ Configuration, Environment }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto
import uk.gov.hmrc.play.partials.HtmlPartial.*
import uk.gov.hmrc.play.partials.{ HeaderCarrierForPartialsConverter, HtmlPartial }

import java.net.{ URL, URLEncoder }
import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class RendererConnector @Inject() (
  val sessionCookieCrypto: SessionCookieCrypto,
  http: HttpClientV2,
  val runModeConfiguration: Configuration,
  val environment: Environment,
  headerCarrierForPartialsConverter: HeaderCarrierForPartialsConverter,
  servicesConfig: ServicesConfig
)(implicit executionContext: ExecutionContext) {

  def getRenderedMessage(rendererUrl: ServiceUrl, parameters: Map[String, Seq[String]])(implicit
    request: RequestHeader
  ): Future[HtmlPartial] = {
    implicit val hc: HeaderCarrier = headerCarrierForPartialsConverter.fromRequestWithEncryptedCookie(request)

    val deprecateRenderer = servicesConfig.getBoolean("deprecate.message-renderer")
    val url =
      if (
        (rendererUrl.service == "ats-message-renderer" || rendererUrl.service == "two-way-message") && deprecateRenderer
      ) {
        s"${servicesConfig.baseUrl("secure-message")}/secure-messaging${rendererUrl.url}"
      } else {
        s"${servicesConfig.baseUrl(rendererUrl.service)}${rendererUrl.url}"
      }

    val queryParams = parameters.flatMap { case (k, v) => v.map((k, _)) }.toSeq
    val eventualPartial = http.get(new URL(url + makeQueryString(queryParams))).execute[HtmlPartial]

    eventualPartial recover HtmlPartial.connectionExceptionsAsHtmlPartialFailure
  }

  private def makeQueryString(queryParams: Seq[(String, String)]) = {
    val paramPairs = queryParams.map { case (k, v) => s"$k=${URLEncoder.encode(v, "utf-8")}" }
    if (paramPairs.isEmpty) "" else paramPairs.mkString("?", "&", "")
  }
}
