/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{ Conversation, ConversationHeader, Message, ReadTime, SenderInformation }
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{ any, anyString }
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Writes
import play.api.test.Helpers._
import uk.gov.hmrc.http.{ HeaderCarrier, HttpClient, HttpReads, HttpResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class SecureMessageConnectorSpec extends PlaySpec with MockitoSugar {

  "SecureMessgaeConnector.getConversationList" must {
    "return a list with one conversation" in new TestCase {
      when(
        mockHttpClient
          .GET[List[ConversationHeader]](any[String], any[Seq[(String, String)]], any[Seq[(String, String)]])(
            any[HttpReads[List[ConversationHeader]]],
            any[HeaderCarrier],
            any[ExecutionContext]))
        .thenReturn(Future(List(ConversationHeader("cdcm", "123", "ABC", new DateTime(), None, true, 1))))
      private val result = await(connector.getConversationList())
      result.size mustBe 1
    }
  }

  "SecureMessgaeConnector.getConversation" must {
    "return a conversation" in new TestCase {
      val testDate = DateTime.now()
      when(
        mockHttpClient
          .GET[Conversation](any[String], any[Seq[(String, String)]], any[Seq[(String, String)]])(
            any[HttpReads[Conversation]],
            any[HeaderCarrier],
            any[ExecutionContext]))
        .thenReturn(
          Future(
            Conversation(
              "client",
              "conversationId",
              "status",
              Map.empty[String, String],
              "subject",
              "en",
              List(Message(SenderInformation("name", testDate), None, "content")))))
      private val result = await(connector.getConversation("client", "conversationId"))
      result mustBe (
        Conversation(
          "client",
          "conversationId",
          "status",
          Map.empty[String, String],
          "subject",
          "en",
          List(Message(SenderInformation("name", testDate), None, "content"))
        )
      )
    }
  }

  "SecureMessgaeConnector.recordReadTime" must {
    "return " in new TestCase {
      when(
        mockHttpClient.POST[ReadTime, HttpResponse](anyString, any[ReadTime], any[Seq[(String, String)]])(
          any[Writes[ReadTime]],
          any[HttpReads[HttpResponse]],
          any[HeaderCarrier],
          any[ExecutionContext])).thenReturn(Future.successful(HttpResponse(OK, "")))

      private val result = await(connector.recordReadTime("client", "conversationId"))
      result.status must be(OK)
    }
  }

  trait TestCase {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val mockHttpClient: HttpClient = mock[HttpClient]
    val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
    val connector = new SecureMessageConnector(mockHttpClient, mockServicesConfig)
  }

}
