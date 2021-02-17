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

import models.FirstReader
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import views.helpers.HtmlUtil.{ backToConversationsLink, firstReadMessageConversationText,
  readMessageConversationText, readableTime, sentMessageConversationText }

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class HtmlUtilSpec extends PlaySpec {

  "sentMessageConversationText function returns correct sent message text" in {
    sentMessageConversationText("15 Feb 2021 at 11:49 AM") must be("this message on 15 Feb 2021 at 11:49 AM")
  }

  "readMessageConversationText function returns correct read message text" in {
    val testTime = DateTime.now
    readMessageConversationText must be(s"this message on ${readableTime(testTime)}")
  }

  "firstReadMessageConversationText function returns correct first read message text" in {
    val testTime = DateTime.now
    val firstReader = Some(FirstReader("Name", testTime))
    firstReadMessageConversationText(firstReader) must be(Some(s"on ${readableTime(testTime)}"))
  }

  "backToConversationsLink function should return correct link to take back to conversations" in {
    backToConversationsLink("stub").toString mustBe "<a href=/stub/conversations class=\"govuk-back-link\">Back</a>"
  }
}
