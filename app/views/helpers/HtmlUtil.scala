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

import models.ConversationHeader
import org.joda.time.format.DateTimeFormat
import play.api.i18n.Messages

object HtmlUtil {

  private val dtf = DateTimeFormat.forPattern("d MMMM yyyy")

  def getSenderName(conversationHeader: ConversationHeader)(implicit messages: Messages): String =
    conversationHeader.senderName match {
      case Some(name) => name
      case _          => messages("conversation.inbox.default.sender")
    }

  def getMessageDate(conversationHeader: ConversationHeader): String =
    dtf.print(conversationHeader.issueDate)

  def getConversationUrl(clientService: String, conversationHeader: ConversationHeader): String =
    s"$clientService/conversation/${conversationHeader.client}/${conversationHeader.conversationId}"

}
