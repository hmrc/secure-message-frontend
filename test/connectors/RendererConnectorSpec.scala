/*
 * Copyright 2024 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.*
import play.api.test.FakeRequest
import play.api.{ Configuration, Environment }
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.{ HttpClientV2, RequestBuilder }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.SessionCookieCrypto
import uk.gov.hmrc.play.partials.{ HeaderCarrierForPartialsConverter, HtmlPartial }

import java.net.{ URI, URL }
import scala.concurrent.{ ExecutionContext, Future }

class RendererConnectorSpec extends PlaySpec with ScalaFutures with MockitoSugar {

  class TestCase {
    val mockCrypto: SessionCookieCrypto = mock[SessionCookieCrypto]
    val mockHttp: HttpClientV2 = mock[HttpClientV2]
    val mockConfiguration: Configuration = mock[Configuration]
    val mockEnvironment: Environment = mock[Environment]
    val mockPartialsConverter: HeaderCarrierForPartialsConverter = mock[HeaderCarrierForPartialsConverter]
    val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
    val mockRequestBuilder: RequestBuilder = mock[RequestBuilder]
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val renderer = new RendererConnector(
      mockCrypto,
      mockHttp,
      mockConfiguration,
      mockEnvironment,
      mockPartialsConverter,
      mockServicesConfig
    )
  }

  "RendererConnector for V3 messages" should {
    "render messages from respective services as per renderUrl" in new TestCase {
      val expectedUrl: URL = URI("http://localhost:9051/message/messageId/content").toURL
      when(mockServicesConfig.baseUrl(ArgumentMatchers.eq("secure-message"))).thenReturn("http://localhost:9051")
      when(mockHttp.get(ArgumentMatchers.eq(expectedUrl))(any[HeaderCarrier])).thenReturn(mockRequestBuilder)
      when(mockRequestBuilder.execute(any(), any()))
        .thenReturn(Future.successful(HtmlPartial.Success(Some("SA"), Html("<p>Self Assessment</p>"))))

      val serviceUrl: ServiceUrl = ServiceUrl("secure-message", "/message/messageId/content")
      val response: Future[HtmlPartial] = renderer.getRenderedMessage(serviceUrl, Map.empty)(FakeRequest())

      response.futureValue.toString must include("<p>Self Assessment</p>")
    }
  }
}
