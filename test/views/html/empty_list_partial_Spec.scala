/*
 * Copyright 2024 HM Revenue & Customs
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

package views.html

import config.AppConfig
import helpers.LanguageHelper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{ Lang, Messages }
import play.twirl.api.Html
import views.helpers.PortalUrlBuilder

class empty_list_partial_Spec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with LanguageHelper {

  implicit val messages: Messages = messagesInEnglish()
  implicit val lang: Lang = langEn
  val appConfig: AppConfig = mock[AppConfig]
  val testUrlBuilder = new PortalUrlBuilder(appConfig)

  "empty_list_partial" should {
    "generate empty messages inbox" in {
      val html = views.html.empty_list_partial(
        urlBuilder = testUrlBuilder,
        saUtr = Some("someSaUtr"),
        taxIdentifiersPartial = Html("")
      )

      html.body must (
        include("Messages") and
          include("Your inbox") and
          include("You’ve not been sent any inbox messages yet.")
      )
    }
  }
}
