/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import base.SpecBase
import config.AppConfig
import org.jsoup.Jsoup
import play.api.i18n.{ Messages, MessagesApi }
import play.api.test.FakeRequest
import play.twirl.api.Html
import views.html.Layout

class LayoutSpec extends SpecBase {

  "Layout" should {
    "correct contents" in {
      val application = applicationBuilder().configure("metrics.enabled" -> "false").build()

      val fakeRequest = FakeRequest("GET", "test_path").withHeaders("X-Session-ID" -> "someSessionId")
      val messages: Messages = application.injector.instanceOf[MessagesApi].preferred(fakeRequest)
      val appConfig = application.injector.instanceOf[AppConfig]

      val layout = application.injector.instanceOf[Layout]

      val title = Some("title")
      val headBlock = Some(Html("test_content"))
      val scriptBlock = Some(Html("test_content"))
      val content = Html("test_body")

      val layoutContents =
        Jsoup.parse(layout.apply(title, headBlock, scriptBlock)(content)(fakeRequest, messages, appConfig).body)

      layoutContents.title() mustBe "title"
    }
  }
}
