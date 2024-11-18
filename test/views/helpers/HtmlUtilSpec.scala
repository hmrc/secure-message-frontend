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

package views.helpers

import model.{ MessageListItem, TaxpayerName }
import models.{ MessageHeader, MessageType }

import java.time.{ Instant, LocalDate, ZoneOffset }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{ DefaultMessagesApi, Lang, Messages, MessagesApi, MessagesImpl }
import play.api.inject.Injector
import play.api.test.FakeRequest
import play.api.mvc.AnyContentAsEmpty
import views.helpers.HtmlUtil.*

import java.time.format.DateTimeFormatter

class HtmlUtilSpec extends PlaySpec with GuiceOneAppPerSuite {
  val messagesApi = new DefaultMessagesApi()
  implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), messagesApi)

  "conversation readableTime function returns correct readable timestamp in English" in {
    readableTime(Instant.parse("2021-02-19T10:29:47.275Z")) must be("19 February 2021 at 10:29am")
  }

  "conversation readableTime function returns correct readable timestamp in Welsh" in {
    val messagesCy = MessagesImpl(Lang("cy"), messagesApi)
    readableTime(Instant.parse("2021-02-19T10:29:47.275Z"))(messagesCy) must be("19 Chwefror 2021 am 10:29yb")
  }

  "decodeBase64String function should return decoded string" in {
    decodeBase64String("V2hhdCBhIGRheSE=") mustBe "What a day!"
  }

  "Inbox creation date for conversation" must {
    "return correct date if date is not today" in {
      getMessageDate(
        MessageHeader(
          MessageType.Conversation,
          "",
          "",
          Instant.parse("2021-02-19T10:29:47.275Z"),
          None,
          false,
          1,
          Some(""),
          Some("")
        )
      ) must be("19 February 2021")
    }

    "return just time if message creation is today" in {
      val instant = Instant.parse(s"${LocalDate.now()}T05:29:47.275Z")
      val times: Int = 5
      val messageDateOrTime =
        getMessageDate(MessageHeader(MessageType.Conversation, "", "", instant, None, false, 1, Some(""), Some("")))
      messageDateOrTime.takeRight(times) must be(":29am")
    }

    "return just time if message creation is today, in Welsh" in {
      val messagesCy = MessagesImpl(Lang("cy"), messagesApi)
      val times: Int = 5
      val instant = Instant.parse(s"${LocalDate.now()}T05:29:47.275Z")
      val messageDateOrTime =
        getMessageDate(MessageHeader(MessageType.Conversation, "", "", instant, None, false, 1, Some(""), Some("")))(
          messagesCy
        )
      messageDateOrTime.takeRight(times) must be(":29yb")
    }
  }

  "Inbox creation date for message" must {
    "return correct date if date is not today" in {
      getMessageDate(
        MessageHeader(
          MessageType.Letter,
          "",
          "",
          LocalDate.parse("2021-02-19").atStartOfDay().toInstant(ZoneOffset.UTC),
          None,
          false,
          1,
          Some(""),
          Some("")
        )
      ) must be("19 February 2021")
    }

    "return today's Date even if date is today" in {
      val instant = Instant.now()
      val dtf = DateTimeFormatter.ofPattern("d MMMM yyyy")
      val messageDateOrTime =
        getMessageDate(MessageHeader(MessageType.Letter, "", "", instant, None, false, 1, Some(""), Some("")))
      messageDateOrTime must be(dtf.format(LocalDate.now()))
    }
  }

  "Inbox link url" must {
    val id = "60995694180000c223edb0b9"
    "return with client and id for a conversation" in {
      getMessageUrl(
        "someclient",
        MessageHeader(MessageType.Conversation, id, "subject", Instant.now(), None, false, 1, Some("111"), Some("CDCM"))
      ) mustBe s"/someclient/conversation/CDCM/$id"
    }
    "with id for letter" in {
      getMessageUrl(
        "someclient",
        MessageHeader(MessageType.Letter, id, "subject", Instant.now(), None, false, 1, None, None)
      ) mustBe s"/someclient/messages/$id"
    }
  }

  "HtmlUtil.getSenderName" must {
    lazy val injector: Injector = app.injector
    lazy val messagesApi: MessagesApi = injector.instanceOf[MessagesApi]
    implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    implicit lazy val resourcesMessages: Messages = messagesApi.preferred(fakeRequest)

    "return the customer's name when the message is a 2-way secure message from the customer and the name is available" in {
      val messageListitem = new MessageListItem(
        id = "123",
        subject = "test",
        validFrom = LocalDate.now(),
        taxpayerName = Some(TaxpayerName(forename = Some("test"), surname = Some("user"))),
        readTime = None,
        sentInError = false,
        messageDesc = Some("2wsm-customer")
      )
      val senderName = HtmlUtil.getSenderName(messageListitem)
      senderName mustBe "Test User"
    }
    "return 'You' when the message is a 2-way secure message from the customer and the name is not available" in {
      val messageListitem = new MessageListItem(
        id = "123",
        subject = "test",
        validFrom = LocalDate.now(),
        taxpayerName = Some(TaxpayerName()),
        readTime = None,
        sentInError = false,
        messageDesc = Some("2wsm-customer")
      )
      val senderName = HtmlUtil.getSenderName(messageListitem)
      senderName mustBe "You"
    }
    "return the 'HMRC digital team' name when the message is a 2-way secure message from the advisor" in {
      val messageListitem = new MessageListItem(
        id = "123",
        subject = "test",
        validFrom = LocalDate.now(),
        taxpayerName = None,
        readTime = None,
        sentInError = false,
        messageDesc = Some("2wsm-advisor")
      )
      val senderName = HtmlUtil.getSenderName(messageListitem)
      senderName mustBe "HMRC digital team"
    }
    "return the 'HMRC' name when the message is from HMRC but not a 2-way secure message" in {
      val messageListitem = new MessageListItem(
        id = "123",
        subject = "test",
        validFrom = LocalDate.now(),
        taxpayerName = None,
        readTime = None,
        sentInError = false,
        messageDesc = Some("other")
      )
      val senderName = HtmlUtil.getSenderName(messageListitem)(resourcesMessages)
      senderName mustBe "HMRC"
    }
  }

}
