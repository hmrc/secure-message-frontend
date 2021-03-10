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
import controllers.generic.models.{ CustomerEnrolment, Tag }
import models.ConversationHeader
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import play.api.i18n.{ Lang, Messages, MessagesApi, MessagesImpl }
import play.api.test.Helpers.{ GET, contentAsString, status }
import play.api.test.{ FakeRequest, Helpers }
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import views.html.partials.conversationInbox
import views.viewmodels.ConversationInbox

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class ConversationInboxControllerSpec extends PlaySpec with MockitoSugar with MockAuthConnector {

  "Conversations Controller" must {
    "show latest messages" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      when(
        mockSecureMessageConnector.getConversationList(
          ArgumentMatchers.eq(Some(List("HMRC-CUS-ORG"))),
          ArgumentMatchers.eq(Some(List(CustomerEnrolment("HMRC-CUS-ORG", "EORIName", "GB7777777777")))),
          ArgumentMatchers.eq(Some(List(Tag("notificationType", "CDS Exports"))))
        )(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(
          Future(
            List(
              ConversationHeader(
                "cdcm",
                "D-80542-20201120",
                "DMS7324874993",
                new DateTime(),
                Some("CDS Exports Team"),
                unreadMessages = true,
                1))))
      when(mockConversationsInboxPartial.apply(any[ConversationInbox])(any[Messages])).thenReturn(new Html("test"))
      private val controller = new ConversationInboxController(
        mockAppConfig,
        Helpers.stubMessagesControllerComponents(),
        mockConversationsInboxPartial,
        mockSecureMessageConnector,
        mockAuthConnector)
      private val result = controller.display(
        "cds-frontend",
        Some(List("HMRC-CUS-ORG")),
        Some(List(CustomerEnrolment("HMRC-CUS-ORG", "EORIName", "GB7777777777"))),
        Some(List(Tag("notificationType", "CDS Exports")))
      )(FakeRequest(method = GET, path = "/messages"))
      status(result) mustBe OK
    }

    "return BAD REQUEST when query filter parameters are not valid" in new TestCase {
      mockAuthorise[Unit]()(Future.successful(()))
      when(
        mockSecureMessageConnector.getConversationList(
          ArgumentMatchers.eq(None),
          ArgumentMatchers.eq(None),
          ArgumentMatchers.eq(None)
        )(any[ExecutionContext], any[HeaderCarrier])).thenReturn(Future(List()))
      when(mockConversationsInboxPartial.apply(any[ConversationInbox])(any[Messages])).thenReturn(new Html("test"))
      private val controller = new ConversationInboxController(
        mockAppConfig,
        Helpers.stubMessagesControllerComponents(),
        mockConversationsInboxPartial,
        mockSecureMessageConnector,
        mockAuthConnector)
      private val result = controller.display("cds-frontend", None, None, None)(
        FakeRequest(method = GET, path = "/messages?x=3&abc=test&some_key=some_value"))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe "Invalid query parameter(s) found: [abc, some_key, x]"
    }
  }

  class TestCase {
    implicit val duration: Timeout = 5 seconds
    implicit val mockAppConfig: AppConfig = mock[AppConfig]
    val mockMessagesApi: MessagesApi = mock[MessagesApi]
    implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), mockMessagesApi)
    val mockConversationsInboxPartial: conversationInbox = mock[conversationInbox]
    val mockSecureMessageConnector: SecureMessageConnector = mock[SecureMessageConnector]

  }
}
