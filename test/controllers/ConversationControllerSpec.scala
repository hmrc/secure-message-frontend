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

package controllers

import akka.util.Timeout
import config.AppConfig
import connectors.SecureMessageConnector
import models.{ Conversation, ConversationView, FirstReader, Message, SenderInformation }
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.{ contentAsString, status, stubMessagesControllerComponents }
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.{ ExecutionContext, Future }
import views.html.partials.{ conversation, messageContent }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

@SuppressWarnings(Array("org.wartremover.warts.All"))
class ConversationControllerSpec extends PlaySpec with GuiceOneAppPerSuite with MockAuthConnector {

  "Conversation Controller" must {

    "return message partial with 1 message" in new TestCase {
      val messages = List(
        Message(
          SenderInformation("senderName", DateTime.parse("2021-02-19T10:29:47.275Z")),
          Some(FirstReader("firstReadername", DateTime.parse("2021-03-01T10:29:47.275Z"))),
          "messageBody"
        ))

      val messagesContent = controller.messagePartial(messages).toString()
      messagesContent must include("this message on 19 Feb 2021 at 10:29 AM")
      messagesContent must include("on 1 Mar 2021 at 10:29 AM")
      messagesContent must include("You read")
      messagesContent must include("messageBody")
    }

    "return message partial with multiple messages" in new TestCase {

      val messages = List(
        Message(
          SenderInformation("senderName", DateTime.parse("2021-02-19T10:29:47.275Z")),
          Some(FirstReader("firstReadername", DateTime.parse("2021-03-01T10:29:47.275Z"))),
          "messageFirstBody"
        ),
        Message(
          SenderInformation("senderName", DateTime.parse("2021-04-19T10:29:47.275Z")),
          Some(FirstReader("firstReadername", DateTime.parse("2021-05-01T10:29:47.275Z"))),
          "messageSecondBody"
        )
      )

      val messagesContent = controller.messagePartial(messages).toString()
      messagesContent must include("this message on 19 Feb 2021 at 10:29 AM")
      messagesContent must include("on 1 Mar 2021 at 10:29 AM")
      messagesContent must include("You read")
      messagesContent must include("messageFirstBody")

      messagesContent must include("this message on 19 Apr 2021 at 10:29 AM")
      messagesContent must include("on 1 May 2021 at 10:29 AM")
      messagesContent must include("You read")
      messagesContent must include("messageSecondBody")

    }

    "return message partial will not show first read if it doesn't have first read info" in new TestCase {

      val messages = List(
        Message(
          SenderInformation("senderName", DateTime.parse("2021-02-19T10:29:47.275Z")),
          None,
          "messageBody"
        ))

      val messagesContent = controller.messagePartial(messages).toString()
      messagesContent must include("this message on 19 Feb 2021 at 10:29 AM")
      messagesContent must not include ("First read")
      messagesContent must include("You read")
      messagesContent must include("messageBody")
    }

    "return conversation partial" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      when(
        mockSecureMessageConnector.getConversation(any[String], any[String])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(
          Future.successful(Conversation(client, conversationId, "", Map.empty[String, String], "", "", List.empty)))

      when(mockConversation.apply(any[ConversationView])(any[Messages]))
        .thenReturn(new Html("MRN 20GB16046891253600 needs action"))

      val result =
        controller.display("some-service", "hmrc", "11111")(FakeRequest("GET", "/some-service/conversation/hmrc/11111"))
      status(result) mustBe Status.OK
      val pageContent = contentAsString(result)
      pageContent must include("MRN 20GB16046891253600 needs action")
    }

    "save reply" in new TestCase {

      val result = controller.saveReply("some-service", "111", "DA123")(
        FakeRequest("POST", "/some-service/conversation-message/111/DA123"))

      status(result) mustBe Status.CREATED
      val pageContent = contentAsString(result)
      pageContent must include("Saved reply successfull with client some-service client 111 and conversationId DA123")
    }

    "response" in new TestCase {

      val result = controller.response("some-service", "111", "DA123")(
        FakeRequest("GET", "/some-service/conversation-message/111/DA123/result"))

      status(result) mustBe Status.OK
      val pageContent = contentAsString(result)
      pageContent must include("some-service with client 111 with conversationId DA123")
    }
  }

  class TestCase {

    implicit val duration: Timeout = 5 seconds

    val client = "testClient"

    val conversationId = "conversationId"

    val messageContent = app.injector.instanceOf[messageContent]

    val mockConversation = mock[conversation]

    implicit val request: FakeRequest[_] = FakeRequest("POST", "/some-service/conversation-message/111/DA123")
    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

    val mockSecureMessageConnector: SecureMessageConnector = mock[SecureMessageConnector]

    val controller = new ConversationController(
      stubMessagesControllerComponents(),
      mockSecureMessageConnector,
      messageContent,
      mockConversation,
      mockAuthConnector)

  }

}
