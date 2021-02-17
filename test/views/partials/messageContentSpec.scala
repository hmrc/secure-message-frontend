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

package views.partials

import models.MessageView
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{ Lang, MessagesApi, MessagesImpl }
import play.api.test.FakeRequest
import views.html.Layout
import views.html.partials.messageContent

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class messageContentSpec extends PlaySpec {

  "messsageContent template" must {
    "have all information including first read" in new TestClass {
      val messageContent = new messageContent(layout)(
        MessageView("Mike", "sent text", Some("sample text for first read"), "read text", "message body")).toString

      messageContent must include("Mike")
      messageContent must include("sent text")
      messageContent must include("read text")
      messageContent must include("First read")
      messageContent must include("You read")
      messageContent must include("message body")
    }
    "be handled without first read information" in new TestClass {
      val messageContent =
        new messageContent(layout)(MessageView("Mike", "sent text", None, "read text", "message body")).toString

      messageContent must include("Mike")
      messageContent must include("sent text")
      messageContent must include("read text")
      messageContent must not include ("First read")
      messageContent must include("You read")
      messageContent must include("message body")
    }
  }

  class TestClass {
    implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), mock[MessagesApi])
    implicit val request = FakeRequest("GET", "/")
    val layout = mock[Layout]
  }
}
