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

import models.ConversationView
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{ Lang, MessagesApi, MessagesImpl }
import play.api.test.FakeRequest
import play.twirl.api.{ Html }
import uk.gov.hmrc.govukfrontend.views.html.components.{ GovukBackLink, GovukPanel }
import views.html.Layout
import views.html.partials.conversation

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class conversationSpec extends PlaySpec {
  "conversation template" must {
    "have message content with subject and back link" in new TestClass {
      val conversationContent = new conversation(layout, new GovukPanel, new GovukBackLink)(
        ConversationView("subject", Seq(Html("message content one")), Html("back"))).toString

      conversationContent must include("back")
      conversationContent must include("subject")
      conversationContent must include("message content one")
    }
  }

  class TestClass {
    implicit val messages: MessagesImpl = MessagesImpl(Lang("en"), mock[MessagesApi])
    implicit val request = FakeRequest("GET", "/")
    val layout = mock[Layout]
  }
}
