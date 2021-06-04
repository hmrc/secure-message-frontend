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

import cats.implicits.catsSyntaxEq
import com.ibm.icu.text.SimpleDateFormat
import com.ibm.icu.util.{ TimeZone, ULocale }
import models.{ MessageHeader, MessageType }
import org.apache.commons.codec.binary.Base64
import org.joda.time._
import play.api.i18n.Messages
import play.twirl.api.Html

import scala.xml.{ Utility, Xhtml }

@SuppressWarnings(Array("org.wartremover.warts.PlatformDefault"))
object HtmlUtil {

  private def dtf(implicit messages: Messages): SimpleDateFormat = createDateFormatForPattern("d MMMM yyyy")
  private def dtfHours(implicit messages: Messages): SimpleDateFormat = createDateFormatForPattern("h:mmaa")

  private def createDateFormatForPattern(pattern: String)(implicit messages: Messages): SimpleDateFormat = {
    val langCode = messages.lang.code
    val uk = TimeZone.getTimeZone("Europe/London")
    val validLang: Boolean = ULocale.getAvailableLocales.contains(new ULocale(langCode))
    val locale: ULocale = if (validLang) new ULocale(langCode) else ULocale.getDefault
    val sdf = new SimpleDateFormat(pattern, locale)
    sdf.setTimeZone(uk)
    sdf
  }

  def getSenderName(conversationHeader: MessageHeader)(implicit messages: Messages): String =
    conversationHeader.senderName match {
      case Some(name) => name
      case _          => messages("conversation.inbox.default.sender")
    }

  def getMessageDate(conversationHeader: MessageHeader)(implicit messages: Messages): String = {
    val isConversation = conversationHeader.messageType.entryName === MessageType.Conversation.entryName
    val isToday = conversationHeader.issueDate.toLocalDate.toString === DateTime.now.toLocalDate.toString
    if (isToday && isConversation) {
      dtfHours.format(conversationHeader.issueDate.toDate)
    } else {
      dtf.format(conversationHeader.issueDate.toDate)
    }
  }

  def getMessageUrl(clientService: String, messageHeader: MessageHeader): String =
    if (messageHeader.messageType.entryName === MessageType.Conversation.entryName) {
      s"/$clientService/conversation/CDCM/${messageHeader.id}"
    } else {
      s"/$clientService/messages/${messageHeader.id}"
    }

  def readableTime(dateTime: DateTime)(implicit messages: Messages): String = {
    val at = messages.lang.code match {
      case "cy" => "am"
      case _    => "at"
    }
    createDateFormatForPattern(s"d MMMM yyyy '$at' h:mmaa").format(dateTime.toDate)
  }

  def readableDate(date: LocalDate)(implicit messages: Messages): String =
    dtf.format(date.toDate)

  def decodeBase64String(input: String): String =
    new String(Base64.decodeBase64(input.getBytes("UTF-8")))

  def encodeBase64String(input: String): String =
    Base64.encodeBase64String(input.getBytes("UTF-8"))

  // scalastyle:off
  def getReplyIcon(replyFormUrl: String): Html =
    Html.apply(Xhtml.toXhtml(Utility.trim(<span>
      <a aria-hidden="true" tabindex="-1" style="text-decoration:none;" href={replyFormUrl}>
        <svg style="vertical-align:text-top;padding-right:5px;" width="21px" height="20px" viewBox="0 0 33 31" version="1.1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink">
          <title>Reply</title>
          <g id="Page-1" stroke="none" stroke-width="1" fill="none" fill-rule="evenodd">
            <g id="icon-reply" fill="#000000" fill-rule="nonzero">
              <path d="M20.0052977,9.00577935 C27.0039418,9.21272548 32.6139021,14.9512245 32.6139021,22 C32.6139021,25.5463753 31.1938581,28.7610816 28.8913669,31.1065217 C29.2442668,30.1082895 29.4380446,29.1123203 29.4380446,28.1436033 C29.4380446,21.8962314 25.9572992,21.1011463 20.323108,21 L15,21 L15,30 L-1.42108547e-14,15 L15,2.25597319e-13 L15,9 L20,9 L20.0052977,9.00577935 Z" id="Combined-Shape"></path>
            </g>
          </g>
        </svg>
      </a>
    </span>)))
  // scalastyle:on

}
