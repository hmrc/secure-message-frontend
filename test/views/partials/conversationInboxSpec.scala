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

import base.LanguageStubs
import play.twirl.api.HtmlFormat
import views.html.partials.messageInbox
import views.viewmodels.MessageInbox

import scala.util.{ Success, Try }

class conversationInboxSpec extends TemplateUnitSpec[MessageInbox] with LanguageStubs {

  "A conversation inbox template" must {
    "have all mandatory information" in {
      render(MessageInbox("cds-frontend", "test", 5, 1, List.empty)) match {
        case Success(component) =>
          val pageContent = component.body
          pageContent must include("""<h1 class="govuk-heading-xl">test</h1>""")
          pageContent must include("""<span class="govuk-visually-hidden">""")
          pageContent must include(
            """1 unread, 5 in total. Each message in the list includes its status (either unread or previously viewed), and its sender name, subject, and send time or date. If a message includes replies, then its subject says the number of messages in that conversation."""
          )
          pageContent must include("""<th scope="col" class="govuk-table__header">Message</th>""")
          pageContent must include("""<th scope="col" class="govuk-table__header mob-align-right">Date</th>""")
        case _ => fail("There was a problem reading the test output")
      }
    }
  }

  /** Calls the Twirl template with the given parameters and returns the resulting markup
    *
    * @param templateParams
    * @return
    *   [[Try[HtmlFormat.Appendable]]] containing the markup
    */
  override def render(templateParams: MessageInbox): Try[HtmlFormat.Appendable] = {
    val inbox = new messageInbox
    Try(inbox(templateParams)(messagesEn))
  }

}
