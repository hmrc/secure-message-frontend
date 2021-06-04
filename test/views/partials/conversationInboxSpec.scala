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

import play.api.i18n.{ DefaultMessagesApi, Lang, Messages, MessagesApi, MessagesImpl }
import play.api.i18n.Messages.UrlMessageSource
import play.twirl.api.HtmlFormat
import views.html.partials.messageInbox
import views.viewmodels.MessageInbox

import scala.util.{ Success, Try }

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.EitherProjectionPartial"))
class conversationInboxSpec extends TemplateUnitSpec[MessageInbox] {

  "A conversation inbox template" must {
    "have all mandatory information" in {
      render(MessageInbox("cds-frontend", "test", 5, 1, List.empty)) match {
        case Success(component) => {
          val pageContent = component.body
          pageContent must include("""<h1 class="govuk-heading-xl">test</h1>""")
          pageContent must include("""<span class="govuk-visually-hidden">""")
          pageContent must include(
            """1 unread, 5 in total. Each message in the list includes its status (either unread or previously viewed), and its sender name, subject, and send time or date. If a message includes replies, then its subject says the number of messages in that conversation.""")
          pageContent must include(
            """<div aria-hidden="true" class="cols col-sender govuk-!-font-weight-bold">Subject</div>""")
          pageContent must include(
            """<div aria-hidden="true" class="cols col-date govuk-!-font-weight-bold mob-align-right">Date</div>""")
        }
        case _ => fail("There was a problem reading the test output")
      }
    }
  }

  /**
    * Calls the Twirl template with the given parameters and returns the resulting markup
    *
    * @param templateParams
    * @return [[Try[HtmlFormat.Appendable]]] containing the markup
    */
  override def render(templateParams: MessageInbox): Try[HtmlFormat.Appendable] = {
    val inbox = new messageInbox

    val messagesResourceEn: Map[String, String] =
      Messages
        .parse(UrlMessageSource(this.getClass.getClassLoader.getResource("messages")), "")
        .right
        .get

    val messagesResourceCy: Map[String, String] =
      Messages
        .parse(UrlMessageSource(this.getClass.getClassLoader.getResource("messages.cy")), "")
        .right
        .get

    val messagesApi: MessagesApi = new DefaultMessagesApi(
      messages = Map(
        "en" -> messagesResourceEn,
        "cy" -> messagesResourceCy
      )
    )

    val messagesEn = MessagesImpl(Lang("en"), messagesApi)
//    val messagesCy = MessagesImpl(Lang("cy"), messagesApi)
    Try(inbox(templateParams)(messagesEn))
  }

}
