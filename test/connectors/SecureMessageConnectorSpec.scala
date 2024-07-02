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
import models.*

import java.time.{ Instant, LocalDate }
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Lang
import play.api.libs.json.JsValue
import play.api.test.Helpers.*
import uk.gov.hmrc.http.client.{ HttpClientV2, RequestBuilder }
import uk.gov.hmrc.http.{ HeaderCarrier, HttpReads, HttpResponse }
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import java.net.URL
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

class SecureMessageConnectorSpec extends PlaySpec with MockitoSugar {

  "SecureMessageConnector.getConversationList" must {

    "return a list with one conversation" in new TestCase {
      val expectedQueryParams = Seq(
        ("enrolmentKey", "HMRC-CUS-ORG"),
        ("enrolment", "HMRC-CUS-ORG~EORIName~GB7777777777"),
        ("tag", "notificationType~CDS Exports"),
        ("lang", "en")
      )

      when(requestBuilder.execute[List[MessageHeader]](any[HttpReads[List[MessageHeader]]], any[ExecutionContext]))
        .thenReturn(
          Future.successful(
            List(
              MessageHeader(
                MessageType.Conversation,
                "123",
                "ABC",
                Instant.now(),
                None,
                unreadMessages = true,
                1,
                Some("123"),
                Some("CDCM")
              )
            )
          )
        )
      private val result = await(
        connector.getInboxList(
          Some(List("HMRC-CUS-ORG")),
          Some(List(CustomerEnrolment("HMRC-CUS-ORG", "EORIName", "GB7777777777"))),
          Some(List(Tag("notificationType", "CDS Exports"))),
          Lang.defaultLang
        )
      )

      result.size mustBe 1
    }

    "return a count of messages" in new TestCase {
      val totalMessagesCount: Long = 5
      val unreadMessagesCount: Long = 2
      val expectedQueryParams = Seq(
        ("enrolmentKey", "HMRC-CUS-ORG"),
        ("enrolment", "HMRC-CUS-ORG~EORIName~GB7777777777"),
        ("tag", "notificationType~CDS Exports")
      )

      when(requestBuilder.execute[Count](any[HttpReads[Count]], any[ExecutionContext]))
        .thenReturn(Future(Count(total = totalMessagesCount, unread = unreadMessagesCount)))
      private val result = await(
        connector.getCount(
          Some(List("HMRC-CUS-ORG")),
          Some(List(CustomerEnrolment("HMRC-CUS-ORG", "EORIName", "GB7777777777"))),
          Some(List(Tag("notificationType", "CDS Exports")))
        )
      )
      result mustBe Count(total = totalMessagesCount, unread = unreadMessagesCount)
    }
  }

  "SecureMessgaeConnector.getConversation" must {
    "return a conversation" in new TestCase {
      private val testDate = Instant.now()
      when(requestBuilder.execute[Conversation](any[HttpReads[Conversation]], any[ExecutionContext]))
        .thenReturn(
          Future.successful(
            Conversation(
              "client",
              "conversationId",
              "status",
              None,
              "subject",
              "en",
              List(Message(SenderInformation(Some("name"), testDate, self = false), None, "content"))
            )
          )
        )
      private val result = await(connector.getConversationContent("conversationId"))
      result mustBe Conversation(
        "client",
        "conversationId",
        "status",
        None,
        "subject",
        "en",
        List(Message(SenderInformation(Some("name"), testDate, self = false), None, "content"))
      )
    }
  }

  "SecureMessageConnector.getConversationContent" must {
    "return a conversation" in new TestCase {
      private val testDate = Instant.now()
      when(requestBuilder.execute[Conversation](any[HttpReads[Conversation]], any[ExecutionContext]))
        .thenReturn(
          Future.successful(
            Conversation(
              "client",
              "conversationId",
              "status",
              None,
              "subject",
              "en",
              List(Message(SenderInformation(Some("name"), testDate, self = false), None, "content"))
            )
          )
        )
      private val result = await(connector.getConversationContent("someID"))
      result mustBe Conversation(
        "client",
        "conversationId",
        "status",
        None,
        "subject",
        "en",
        List(Message(SenderInformation(Some("name"), testDate, self = false), None, "content"))
      )
    }
  }

  "SecureMessgaeConnector.getLetterContent" must {
    "return a conversation" in new TestCase {

      private val localDate = LocalDate.now()
      private val letter = Letter("MRN 123", "CDS message", None, Sender("HMRC", localDate), None)
      when(requestBuilder.execute[Letter](any[HttpReads[Letter]], any[ExecutionContext]))
        .thenReturn(Future.successful(letter))
      private val result = await(connector.getLetterContent("someId", Lang("cy")))
      result mustBe letter
    }
  }

  "SecureMessageConnector.postCustomerMessage" must {

    "return true when message sent successfully" in new TestCase {
      when(requestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(CREATED, "")))
      private val result = await(connector.saveCustomerMessage(id, CustomerMessage("test")))
      result mustEqual true
    }

    "return false when message fails to send" in new TestCase {
      when(requestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))
      private val result = await(connector.saveCustomerMessage(id, CustomerMessage("test")))
      result mustEqual false
    }

  }

  "SecureMessageConnector.saveCustomerMessage" must {

    "return true when message sent successfully" in new TestCase {
      when(requestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(CREATED, "")))
      private val result = await(connector.saveCustomerMessage(id, CustomerMessage("test")))
      result mustEqual true
    }

    "return false when message fails to send" in new TestCase {
      when(requestBuilder.execute[HttpResponse](any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))
      private val result = await(connector.saveCustomerMessage(id, CustomerMessage("test")))
      result mustEqual false
    }
  }

  trait TestCase {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val aClient: String = "cdcm"
    val aConversationId: String = "D-80542-20210308"
    val id = "L2NvbnZlcnNhdGlvbi8xMjMxNTQ2NDU2"
    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
    when(mockServicesConfig.baseUrl(ArgumentMatchers.eq("secure-message"))).thenReturn("http://localhost:9051")
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
    when(
      mockHttpClient
        .get(any[URL])(
          any[HeaderCarrier]
        )
    ).thenReturn(requestBuilder)
    when(
      mockHttpClient.post(any[URL])(
        any[HeaderCarrier]
      )
    ).thenReturn(requestBuilder)
    when(requestBuilder.withBody(any[JsValue])(any(), any(), any())).thenReturn(requestBuilder)
    val connector = new SecureMessageConnector(mockHttpClient, mockServicesConfig)
  }
}
