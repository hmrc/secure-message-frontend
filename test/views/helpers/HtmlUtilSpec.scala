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

package views.helpers

import models.{ MessageHeader, MessageType }
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{ DefaultMessagesApi, Lang, MessagesImpl }
import views.helpers.HtmlUtil._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class HtmlUtilSpec extends PlaySpec {
  val messagesApi = new DefaultMessagesApi()
  implicit val messages = MessagesImpl(Lang("en"), messagesApi)

  "conversation readableTime function returns correct readable timestamp in English" in {
    readableTime(DateTime.parse("2021-02-19T10:29:47.275Z")) must be("19 February 2021 at 10:29AM")
  }

  "conversation readableTime function returns correct readable timestamp in Welsh" in {
    val messagesCy = MessagesImpl(Lang("cy"), messagesApi)
    readableTime(DateTime.parse("2021-02-19T10:29:47.275Z"))(messagesCy) must be("19 Chwefror 2021 am 10:29yb")
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
          DateTime.parse("2021-02-19T10:29:47.275Z"),
          None,
          false,
          1,
          Some(""),
          Some(""))) must be("19 February 2021")
    }

    "return just time if message creation is today" in {
      val dateTime = DateTime.parse(s"${DateTime.now().toLocalDate}T05:29:47.275Z")
      val messageDateOrTime =
        getMessageDate(MessageHeader(MessageType.Conversation, "", "", dateTime, None, false, 1, Some(""), Some("")))
      messageDateOrTime.takeRight(5) must be(":29AM")
    }
  }

  "Inbox creation date for message" must {
    "return correct date if date is not today" in {
      getMessageDate(MessageHeader(
        MessageType.Letter,
        "",
        "",
        DateTime.parse("2021-02-19"),
        None,
        false,
        1,
        Some(""),
        Some(""))) must be("19 February 2021")
    }

    "return today's Date even if date is today" in {
      val dateTime = DateTime.parse(s"${DateTime.now().toLocalDate}")
      val dtf = DateTimeFormat.forPattern("d MMMM yyyy")
      val messageDateOrTime =
        getMessageDate(
          MessageHeader(
            MessageType.Letter,
            "",
            "",
            DateTime.parse(dateTime.toString),
            None,
            false,
            1,
            Some(""),
            Some("")))
      messageDateOrTime must be(dtf.print(dateTime))
    }
  }

  "Inbox link url" must {
    val id = "60995694180000c223edb0b9"
    "return with client and id for a conversation" in {
      getMessageUrl(
        "someclient",
        MessageHeader(
          MessageType.Conversation,
          id,
          "subject",
          DateTime.now(),
          None,
          false,
          1,
          Some("111"),
          Some("CDCM"))) mustBe s"/someclient/conversation/CDCM/$id"
    }
    "with id for letter" in {
      getMessageUrl(
        "someclient",
        MessageHeader(MessageType.Letter, id, "subject", DateTime.now(), None, false, 1, None, None)) mustBe s"/someclient/messages/$id"
    }
  }

}
