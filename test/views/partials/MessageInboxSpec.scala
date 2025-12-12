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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
    "display the correct contents in English" in new Setup {
      implicit val lang: Lang = Lang.apply("en")
      implicit val messages: Messages =
        application.injector.instanceOf[MessagesApi].preferred(FakeRequest().withTransientLang(lang))

      val view: Document = Jsoup.parse(application.injector.instanceOf[messageInbox].apply(msgInboxModel).body)
      val viewAsText: String = view.text()

      view.getElementsByTag("h1").text() must be(TEST_HEADING)

      assert(viewAsText.contains("Message"))
      assert(viewAsText.contains("Date"))
      assert(viewAsText.contains("from"))
      assert(viewAsText.contains("Subject"))
      assert(viewAsText.contains("messages in this conversation"))
      assert(viewAsText.contains("Status"))

      val visuallyHiddenComponentText: String = view.getElementsByClass("govuk-visually-hidden").text()

      assert(visuallyHiddenComponentText.contains(messages("conversation.inbox.heading.unread")))
      assert(visuallyHiddenComponentText.contains(messages("conversation.inbox.heading.total")))
      assert(visuallyHiddenComponentText.contains(messages("conversation.inbox.heading.description")))
    }

    "display the correct contents in Welsh" in new Setup {
      implicit val lang: Lang = Lang.apply("cy")
      implicit val messages: Messages =
        application.injector.instanceOf[MessagesApi].preferred(FakeRequest().withTransientLang(lang))

      val view: Document = Jsoup.parse(application.injector.instanceOf[messageInbox].apply(msgInboxModel).body)
      val viewAsText: String = view.text()

      view.getElementsByTag("h1").text() must be(TEST_HEADING)
      assert(viewAsText.contains("Neges"))
      assert(viewAsText.contains("Dyddiad"))
      assert(viewAsText.contains("oddi wrth"))
      assert(viewAsText.contains("Pwnc"))
      assert(viewAsText.contains("o negeseuon yn y sgwrs hon"))
      assert(viewAsText.contains("Statws"))

      val visuallyHiddenComponentText: String = view.getElementsByClass("govuk-visually-hidden").text()

      assert(visuallyHiddenComponentText.contains(messages("conversation.inbox.heading.unread")))
      assert(visuallyHiddenComponentText.contains(messages("conversation.inbox.heading.total")))
      assert(visuallyHiddenComponentText.contains(messages("conversation.inbox.heading.description")))
    }
  }

  trait Setup {
    val msgHeader: MessageHeader = MessageHeader(
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

    val msgInboxModel: MessageInbox = MessageInbox(
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
  }

}
