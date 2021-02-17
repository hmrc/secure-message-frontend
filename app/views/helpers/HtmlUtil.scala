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

import models.{ ConversationHeader, FirstReader }
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.i18n.Messages
import play.twirl.api.{ Html, HtmlFormat }

object HtmlUtil {

  private val dtf = DateTimeFormat.forPattern("d MMMM yyyy")
  private val conversationDateTimeFormat = DateTimeFormat.forPattern("d MMM yyyy 'at' HH:mm aa")

  def getSenderName(conversationHeader: ConversationHeader)(implicit messages: Messages): String =
    conversationHeader.senderName match {
      case Some(name) => name
      case _          => messages("conversation.inbox.default.sender")
    }

  def getMessageDate(conversationHeader: ConversationHeader): String =
    dtf.print(conversationHeader.issueDate)

  def getConversationUrl(clientService: String, conversationHeader: ConversationHeader): String =
    s"$clientService/conversation/${conversationHeader.client}/${conversationHeader.conversationId}"

  def readableTime(dateTime: DateTime): String =
    conversationDateTimeFormat.print(dateTime)

  def sentMessageConversationText(time: String): String = s"this message on $time"
  def readMessageConversationText: String = s"this message on ${readableTime(DateTime.now)}"
  def firstReadMessageConversationText(firstReader: Option[FirstReader]): Option[String] =
    firstReader.map(r => s"on ${readableTime(r.read)}")

  def backToConversationsLink(callingService: String): Html =
    HtmlFormat.raw(s"""<a href=/$callingService/conversations class="govuk-back-link">Back</a>""")

}
