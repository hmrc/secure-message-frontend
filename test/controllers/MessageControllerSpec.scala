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

import akka.stream.Materializer
import akka.stream.testkit.NoMaterializer
import akka.util.Timeout
import base.LanguageStubs
import config.AppConfig
import connectors.SecureMessageConnector
import forms.MessageFormProvider
import models.{ Conversation, CustomerMessage, FirstReaderInformation, Letter, Message, Sender, SenderInformation }
import org.joda.time.{ DateTime, LocalDate }
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{ Langs, Messages }
import play.api.mvc.Request
import play.api.test.Helpers.{ contentAsString, status }
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import views.helpers.HtmlUtil.encodeBase64String
import views.html.partials.{ conversationView, letterView, messageContent, messageReply, messageResult }
import views.viewmodels.ConversationView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ ExecutionContext, Future }

class MessageControllerSpec extends PlaySpec with LanguageStubs with GuiceOneAppPerSuite with MockAuthConnector {

  implicit val mat: Materializer = NoMaterializer
  implicit val hc: HeaderCarrier = HeaderCarrier()

  "Message Controller" must {

    "return message partial with 1 message" in new TestCase {
      val messages = List(
        Message(
          SenderInformation(Some("senderName"), DateTime.parse("2021-02-19T10:29:47.275Z"), self = false),
          Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-03-01T10:29:47.275Z"))),
          "TWVzc2FnZSBib2R5IQ=="
        ))

      private val messagesContent = controller.messagePartial(messages).toString()

      messagesContent must include("senderName sent this on 19 February 2021 at 10:29am")
      messagesContent must include("First read on 1 March 2021 at 10:29am")
      messagesContent must include("Message body!")
    }

    "return message partial with `You sent` instead of organisation name if sender name is missing" in new TestCase {
      val messages = List(
        Message(
          SenderInformation(None, DateTime.parse("2021-02-19T10:29:47.275Z"), self = false),
          Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-03-01T10:29:47.275Z"))),
          "TWVzc2FnZSBib2R5IQ=="
        ))

      private val messagesContent = controller.messagePartial(messages).toString()

      messagesContent must include("You sent this on 19 February 2021 at 10:29am")
      messagesContent must include("First read on 1 March 2021 at 10:29am")
      messagesContent must include("Message body!")
    }

    "return message partial with multiple messages" in new TestCase {

      val messages = List(
        // This message is before DST switch
        Message(
          SenderInformation(Some("senderName"), DateTime.parse("2021-02-19T10:29:47.275Z"), self = false),
          Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-03-01T10:29:47.275Z"))),
          "TWVzc2FnZSBib2R5IQ=="
        ),
        // This message is after DST
        Message(
          SenderInformation(Some("senderName"), DateTime.parse("2021-04-19T10:29:47.275Z"), self = false),
          Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-05-01T10:29:47.275Z"))),
          "TWVzc2FnZSBib2R5IQ=="
        )
      )

      private val messagesContent = controller.messagePartial(messages).toString()

      messagesContent must include("senderName sent this on 19 February 2021 at 10:29am")
      messagesContent must include("First read on 1 March 2021 at 10:29am")
      messagesContent must include("Message body!")

    }

    "return message partial will not show first read if it doesn't have first read info" in new TestCase {

      val messages = List(
        Message(
          SenderInformation(Some("senderName"), DateTime.parse("2021-02-19T10:29:47.275Z"), self = false),
          None,
          "TWVzc2FnZSBib2R5IQ=="
        ))

      private val messagesContent = controller.messagePartial(messages).toString()
      messagesContent must include("senderName sent this on 19 February 2021 at 10:29am")
      messagesContent must not include "First read on 19 February 2021 at 10:29am"
      messagesContent must include("Message body!")
    }

    "return conversation partial legacy" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      when(mockSecureMessageConnector.getConversationContent(any[String])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(Conversation(
          client,
          conversationId,
          "",
          None,
          "",
          "",
          List(Message(
            SenderInformation(Some("senderName"), DateTime.parse("2021-04-19T10:29:47.275Z"), self = false),
            Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-05-01T10:29:47.275Z"))),
            "TWVzc2FnZSBib2R5IQ=="
          ))
        )))

      when(mockConversationView.apply(any[ConversationView])(any[Messages], any[Request[_]]))
        .thenReturn(new Html("MRN 20GB16046891253600 needs action"))

      private val result =
        controller.display("some-service", "hmrc", "11111", showReplyForm = false)(
          FakeRequest("GET", "/some-service/conversation/hmrc/11111"))
      status(result) mustBe Status.OK
      private val pageContent = contentAsString(result)
      pageContent must include("MRN 20GB16046891253600 needs action")
    }

    "save a customer reply and return OK with a redirection URL" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      when(
        mockSecureMessageConnector
          .saveCustomerMessage(any[String], any[CustomerMessage])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(true))
      private val result = controller.saveReply("some-service", "111", "DA123")(
        FakeRequest("POST", "/some-service/conversation-message/111/DA123").withFormUrlEncodedBody(
          ("content", "c29tZSBjb250ZW50")))
      status(result) mustBe Status.OK
      private val pageContent = contentAsString(result)
      pageContent must include("/some-service/conversation/111/DA123/result")
    }

    "return BAD REQUEST when the payload is not valid" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))

      when(mockSecureMessageConnector.getConversationContent(any[String])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(Conversation(
          client,
          conversationId,
          "",
          None,
          "",
          "",
          List(Message(
            SenderInformation(Some("senderName"), DateTime.parse("2021-04-19T10:29:47.275Z"), self = false),
            Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-05-01T10:29:47.275Z"))),
            "TWVzc2FnZSBib2R5IQ=="
          ))
        )))

      when(mockConversationView.apply(any[ConversationView])(any[Messages], any[Request[_]]))
        .thenReturn(new Html("MRN 20GB16046891253600 needs action"))

      private val result = controller.saveReply("some-service", "111", "DA123")(
        FakeRequest("POST", "/some-service/conversation-message/111/DA123").withFormUrlEncodedBody(
          ("invalid", "c29tZSBjb250ZW50")))
      status(result) mustBe Status.BAD_REQUEST
    }

    "return BAD REQUEST when the payload is empty" in new TestCase {

      mockAuthorise[Unit]()(Future.successful(()))

      when(mockSecureMessageConnector.getConversationContent(any[String])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(Conversation(
          client,
          conversationId,
          "",
          None,
          "",
          "",
          List(Message(
            SenderInformation(Some("senderName"), DateTime.parse("2021-04-19T10:29:47.275Z"), self = false),
            Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-05-01T10:29:47.275Z"))),
            "TWVzc2FnZSBib2R5IQ=="
          ))
        )))

      when(mockConversationView.apply(any[ConversationView])(any[Messages], any[Request[_]]))
        .thenReturn(new Html("MRN 20GB16046891253600 needs action"))

      when(
        mockSecureMessageConnector
          .saveCustomerMessage(any[String], any[CustomerMessage])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(true))
      private val result = controller.saveReply("some-service", "111", "DA123")(
        FakeRequest("POST", "/some-service/conversation-message/111/DA123").withFormUrlEncodedBody(("content", "")))
      status(result) mustBe Status.BAD_REQUEST
    }

    "return BAD REQUEST when the payload exceeds 4000 chars" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      val textLength4001: String = List.fill(4001)("a").mkString
      when(mockSecureMessageConnector.getConversationContent(any[String])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(Conversation(
          client,
          conversationId,
          "",
          None,
          "",
          "",
          List(Message(
            SenderInformation(Some("senderName"), DateTime.parse("2021-04-19T10:29:47.275Z"), self = false),
            Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-05-01T10:29:47.275Z"))),
            "TWVzc2FnZSBib2R5IQ=="
          ))
        )))

      when(mockConversationView.apply(any[ConversationView])(any[Messages], any[Request[_]]))
        .thenReturn(new Html("MRN 20GB16046891253600 needs action"))

      when(
        mockSecureMessageConnector
          .saveCustomerMessage(any[String], any[CustomerMessage])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(true))
      private val result = controller.saveReply("some-service", "111", "DA123")(
        FakeRequest("POST", "/some-service/conversation-message/111/DA123").withFormUrlEncodedBody(
          ("content", textLength4001)))
      status(result) mustBe Status.BAD_REQUEST
    }

    "return BAD GATEWAY when the message can't be sent" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      when(
        mockSecureMessageConnector
          .saveCustomerMessage(any[String], any[CustomerMessage])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(false))
      private val result = controller.saveReply("some-service", "111", "DA123")(
        FakeRequest("POST", "/some-service/conversation-message/111/DA123").withFormUrlEncodedBody(
          ("content", "c29tZSBjb250ZW50")))
      status(result) mustBe Status.BAD_GATEWAY
      private val pageContent = contentAsString(result)
      pageContent must include("Failed to send message")
    }

    "return a result page HTML partial" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      when(mockMessageResult.apply(any[String])(any[Messages])).thenReturn(Html("<div>result</div>"))
      private val result = controller.result("some-service", "111", "DA123")(
        FakeRequest("GET", "/some-service/conversation-message/111/DA123/result"))
      status(result) mustBe Status.OK
      private val pageContent = contentAsString(result)
      pageContent must include("result")
    }

    "return BAD REQUEST when MesssageType not letter or conversation" in new TestCase {
      private val id = encodeBase64String("messageType/someId")

      private val result = controller.displayMessage("some-service", id, showReplyForm = false)(
        FakeRequest("GET", s"/some-service/messages/$id")
      )
      status(result) mustBe Status.BAD_REQUEST
    }

    "return a letter partial when MessageType letter" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      private val id = encodeBase64String("letter/someId")
      private val letter = Letter("MRN 123", "CDS message", None, Sender("HMRC", LocalDate.now()), None)
      when(mockSecureMessageConnector.getLetterContent(any[String])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(letter))
      when(mockletterView.apply(any[Letter])(any[Messages]))
        .thenReturn(new Html("MRN 20GB16046891253600 needs action"))
      private val result = controller.displayMessage("some-service", id, showReplyForm = false)(
        FakeRequest("GET", s"/some-service/messages/$id")
      )
      status(result) mustBe Status.OK
      private val pageContent = contentAsString(result)
      pageContent must include("MRN 20GB16046891253600 needs action")
    }

    "return a conversation partial when MessageType conversation" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      private val id = encodeBase64String("conversation/someId")
      when(mockSecureMessageConnector.getConversationContent(any[String])(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(Conversation(
          client,
          conversationId,
          "",
          None,
          "",
          "",
          List(Message(
            SenderInformation(Some("senderName"), DateTime.parse("2021-04-19T10:29:47.275Z"), self = false),
            Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-05-01T10:29:47.275Z"))),
            "TWVzc2FnZSBib2R5IQ=="
          ))
        )))

      when(mockConversationView.apply(any[ConversationView])(any[Messages], any[Request[_]]))
        .thenReturn(new Html("MRN 20GB16046891253600 needs action"))

      private val result = controller.displayMessage("some-service", id, showReplyForm = false)(
        FakeRequest("GET", s"/some-service/messages/$id")
      )

      status(result) mustBe Status.OK
      private val pageContent = contentAsString(result)
      pageContent must include("MRN 20GB16046891253600 needs action")
    }

    "Controller Id should be able to patter match on encoded message type and ID" in new TestCase {
      controller.Id("letter", "123") mustBe encodeBase64String("letter/123")
      controller.Id.unapply(encodeBase64String("letter/123")) mustBe Some(("letter", "123"))
      controller.Id.unapply("letter/1233") mustBe None
    }

    "saveReplyMessage" must {
      "redirect to result page when message is saved" in new TestCase {
        mockAuthorise[Unit]()(Future.successful(()))
        when(
          mockSecureMessageConnector
            .saveCustomerMessage(any[String], any[CustomerMessage])(any[ExecutionContext], any[HeaderCarrier]))
          .thenReturn(Future.successful(true))
        private val result = controller.saveReplyMessage("some-service", "L2NvbnZlcnNhdGlvbi8xMjMxNTQ2NDU2")(
          FakeRequest("POST", "/some-service/messages/:id").withFormUrlEncodedBody(("content", "c29tZSBjb250ZW50")))
        status(result) mustBe Status.OK
        private val pageContent = contentAsString(result)
        pageContent must include("/some-service/messages/L2NvbnZlcnNhdGlvbi8xMjMxNTQ2NDU2/result")
      }

      "return BadGateway when message failed to save" in new TestCase {
        mockAuthorise[Unit]()(Future.successful(()))
        when(
          mockSecureMessageConnector
            .saveCustomerMessage(any[String], any[CustomerMessage])(any[ExecutionContext], any[HeaderCarrier]))
          .thenReturn(Future.successful(false))
        private val result = controller.saveReplyMessage("some-service", "L2NvbnZlcnNhdGlvbi8xMjMxNTQ2NDU2")(
          FakeRequest("POST", "/some-service/messages/:id").withFormUrlEncodedBody(("content", "c29tZSBjb250ZW50")))
        status(result) mustBe Status.BAD_GATEWAY
      }

      "return BAD REQUEST when the payload exceeds 4000 chars" in new TestCase {
        mockAuthorise[Unit]()(Future.successful(()))
        val textLength4001: String = List.fill(4001)("a").mkString
        when(mockSecureMessageConnector.getConversationContent(any[String])(any[ExecutionContext], any[HeaderCarrier]))
          .thenReturn(Future.successful(Conversation(
            client,
            conversationId,
            "",
            None,
            "",
            "",
            List(Message(
              SenderInformation(Some("senderName"), DateTime.parse("2021-04-19T10:29:47.275Z"), self = false),
              Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-05-01T10:29:47.275Z"))),
              "TWVzc2FnZSBib2R5IQ=="
            ))
          )))

        when(mockConversationView.apply(any[ConversationView])(any[Messages], any[Request[_]]))
          .thenReturn(new Html("MRN 20GB16046891253600 needs action"))

        when(
          mockSecureMessageConnector
            .saveCustomerMessage(any[String], any[CustomerMessage])(any[ExecutionContext], any[HeaderCarrier]))
          .thenReturn(Future.successful(true))
        private val result = controller.saveReplyMessage("some-service", "L2NvbnZlcnNhdGlvbi8xMjMxNTQ2NDU2")(
          FakeRequest("POST", "/some-service/conversation-message/111/DA123").withFormUrlEncodedBody(
            ("content", textLength4001)))
        status(result) mustBe Status.BAD_REQUEST
      }

      "return BAD REQUEST when the payload is empty" in new TestCase {

        mockAuthorise[Unit]()(Future.successful(()))

        when(mockSecureMessageConnector.getConversationContent(any[String])(any[ExecutionContext], any[HeaderCarrier]))
          .thenReturn(Future.successful(Conversation(
            client,
            conversationId,
            "",
            None,
            "",
            "",
            List(Message(
              SenderInformation(Some("senderName"), DateTime.parse("2021-04-19T10:29:47.275Z"), self = false),
              Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-05-01T10:29:47.275Z"))),
              "TWVzc2FnZSBib2R5IQ=="
            ))
          )))

        when(mockConversationView.apply(any[ConversationView])(any[Messages], any[Request[_]]))
          .thenReturn(new Html("MRN 20GB16046891253600 needs action"))

        when(
          mockSecureMessageConnector
            .saveCustomerMessage(any[String], any[CustomerMessage])(any[ExecutionContext], any[HeaderCarrier]))
          .thenReturn(Future.successful(true))
        private val result = controller.saveReplyMessage("some-service", "L2NvbnZlcnNhdGlvbi8xMjMxNTQ2NDU2")(
          FakeRequest("POST", "/some-service/conversation-message/111/DA123").withFormUrlEncodedBody(("content", "")))
        status(result) mustBe Status.BAD_REQUEST
      }

      "return BAD REQUEST when the payload is not valid" in new TestCase {
        mockAuthorise[Unit]()(Future.successful(()))

        when(mockSecureMessageConnector.getConversationContent(any[String])(any[ExecutionContext], any[HeaderCarrier]))
          .thenReturn(Future.successful(Conversation(
            client,
            conversationId,
            "",
            None,
            "",
            "",
            List(Message(
              SenderInformation(Some("senderName"), DateTime.parse("2021-04-19T10:29:47.275Z"), self = false),
              Some(FirstReaderInformation(Some("firstReadername"), DateTime.parse("2021-05-01T10:29:47.275Z"))),
              "TWVzc2FnZSBib2R5IQ=="
            ))
          )))

        when(mockConversationView.apply(any[ConversationView])(any[Messages], any[Request[_]]))
          .thenReturn(new Html("MRN 20GB16046891253600 needs action"))

        private val result = controller.saveReplyMessage("some-service", "L2NvbnZlcnNhdGlvbi8xMjMxNTQ2NDU2")(
          FakeRequest("POST", "/some-service/conversation-message/111/DA123").withFormUrlEncodedBody(
            ("invalid", "c29tZSBjb250ZW50")))
        status(result) mustBe Status.BAD_REQUEST
      }

      "save a customer reply and return OK with a redirection URL" in new TestCase {
        mockAuthorise[Unit]()(Future.successful(()))
        when(
          mockSecureMessageConnector
            .saveCustomerMessage(any[String], any[CustomerMessage])(any[ExecutionContext], any[HeaderCarrier]))
          .thenReturn(Future.successful(true))
        private val result = controller.saveReplyMessage("some-service", "L2NvbnZlcnNhdGlvbi8xMjMxNTQ2NDU2")(
          FakeRequest("POST", "/some-service/conversation-message/111/DA123").withFormUrlEncodedBody(
            ("content", "c29tZSBjb250ZW50")))
        status(result) mustBe Status.OK
        private val pageContent = contentAsString(result)
        pageContent must include("/some-service/messages/L2NvbnZlcnNhdGlvbi8xMjMxNTQ2NDU2/result")
      }

    }

    "displayResult" must {
      "return a result page HTML partial" in new TestCase {
        mockAuthorise[Unit]()(Future.successful(()))
        when(mockMessageResult.apply(any[String])(any[Messages])).thenReturn(Html("<div>result</div>"))
        private val result =
          controller.displayResult("some-service")(FakeRequest("GET", "/:clientService/messages/:id/result"))
        status(result) mustBe Status.OK
        private val pageContent = contentAsString(result)
        pageContent must include("result")
      }
    }
  }

  class TestCase {

    implicit val duration: Timeout = 5 seconds

    val client = "testClient"

    val conversationId = "conversationId"

    implicit val langs: Langs = app.injector.instanceOf[Langs]
    implicit val request: FakeRequest[_] = FakeRequest("POST", "/some-service/conversation-message/111/DA123")

    val messageContent: messageContent = app.injector.instanceOf[messageContent]
    val mockMessageReply: messageReply = mock[messageReply]
    val mockMessageResult: messageResult = mock[messageResult]
    val mockConversationView: conversationView = mock[conversationView]
    val mockletterView: letterView = mock[letterView]

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

    val mockSecureMessageConnector: SecureMessageConnector = mock[SecureMessageConnector]

    val messageFormProvider: MessageFormProvider = new MessageFormProvider

    val controller = new MessageController(
      stubMessagesControllerComponents(messagesApi = messagesApi),
      mockSecureMessageConnector,
      messageContent,
      mockMessageReply,
      mockMessageResult,
      mockConversationView,
      mockletterView,
      mockAuthConnector,
      messageFormProvider
    )

  }

}
