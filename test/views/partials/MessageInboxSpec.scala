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

package views.partials

import helpers.TestData.{ FIVE, TEST_CLIENT, TEST_HEADING, TEST_ID, TEST_NAME, TEST_SERVICE_NAME, TEST_SUBJECT, TWO }
import models.{ Conversation, MessageHeader, MessageType }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{ Lang, Messages, MessagesApi }
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.stubMessages
import play.twirl.api.HtmlFormat
import views.html.partials.messageInbox
import views.viewmodels.MessageInbox

import java.time.Instant

class MessageInboxSpec extends PlaySpec with GuiceOneAppPerSuite {

  "view" should {
    "display the correct contents" in {

      val msgHeader = MessageHeader(
        messageType = MessageType.Conversation,
        id = TEST_ID,
        subject = TEST_SUBJECT,
        issueDate = Instant.now(),
        senderName = Some(TEST_NAME),
        unreadMessages = true,
        count = FIVE,
        conversationId = Some(TEST_ID),
        client = Some(TEST_CLIENT)
      )

      val msgInboxModel = MessageInbox(
        clientService = TEST_SERVICE_NAME,
        heading = TEST_HEADING,
        total = FIVE,
        unread = TWO,
        conversationHeaders = List(msgHeader)
      )

      implicit val messages: Messages = stubMessages()

      val view: HtmlFormat.Appendable = app.injector.instanceOf[messageInbox].apply(msgInboxModel)

      val viewBody: String = view.body

      assert(viewBody.contains("<h1 class=\"govuk-heading-xl\">test_heading</h1>"))
      assert(viewBody.contains("conversation.inbox.heading.message"))
      assert(viewBody.contains("conversation.inbox.heading.date"))
      assert(viewBody.contains("conversation.inbox.heading.from"))
      assert(viewBody.contains("conversation.inbox.heading.subject"))
      assert(viewBody.contains("conversation.inbox.subject.count"))
      assert(viewBody.contains("conversation.inbox.heading.status"))

      assert(
        viewBody.contains(
          "<span class=\"govuk-visually-hidden\">" +
            "2 conversation.inbox.heading.unread, 5 conversation.inbox.heading.total. conversation.inbox.heading.description" +
            "</span>"
        )
      )
    }

    "display the correct contents in Welsh" in {

      val msgHeader = MessageHeader(
        messageType = MessageType.Conversation,
        id = TEST_ID,
        subject = TEST_SUBJECT,
        issueDate = Instant.now(),
        senderName = Some(TEST_NAME),
        unreadMessages = true,
        count = FIVE,
        conversationId = Some(TEST_ID),
        client = Some(TEST_CLIENT)
      )

      val msgInboxModel = MessageInbox(
        clientService = TEST_SERVICE_NAME,
        heading = TEST_HEADING,
        total = FIVE,
        unread = TWO,
        conversationHeaders = List(msgHeader)
      )

      val application: Application = new GuiceApplicationBuilder()
        .configure(
          "microservice.metrics.enabled" -> false,
          "metrics.enabled"              -> false,
          "auditing.enabled"             -> false
        )
        .build()

      implicit val lang: Lang = Lang.apply("cy")
      implicit val messages: Messages =
        application.injector.instanceOf[MessagesApi].preferred(FakeRequest().withTransientLang(lang))

      val view: HtmlFormat.Appendable = application.injector.instanceOf[messageInbox].apply(msgInboxModel)

      val viewBody: String = view.body

      assert(viewBody.contains("<h1 class=\"govuk-heading-xl\">test_heading</h1>"))
      messages("conversation.inbox.heading.message") must be("Neges")
      messages("conversation.inbox.heading.date") must be("Dyddiad")
      messages("conversation.inbox.heading.from") must be("oddi wrth")
      messages("conversation.inbox.heading.subject") must be("Pwnc")
      messages("conversation.inbox.subject.count") must be("o negeseuon yn y sgwrs hon")
      messages("conversation.inbox.heading.status") must be("Statws")
    }
  }

}
