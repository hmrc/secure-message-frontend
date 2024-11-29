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

package views.html

import helpers.LanguageHelper
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.Messages

class tax_identifiers_partial_Spec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with LanguageHelper {

  implicit val messages: Messages = messagesInEnglish()

  "tax_identifiers_partial" should {

    "include Utr if included" in {
      val identifiersToDisplay = Map("sautr" -> "123456789")
      val html = views.html.tax_identifiers_partial(identifiersToDisplay)

      html.body must (
        include("123456789") and
          include(Messages("tax.identifiers.sautr")) and
          not include Messages("tax.identifiers.nino")
      )
    }

    "include Nino if included" in {
      val identifiersToDisplay = Map("nino" -> "AA111111A")
      val html = views.html.tax_identifiers_partial(identifiersToDisplay)

      html.body must (
        include("AA111111A") and
          not include Messages("tax.identifiers.sautr") and
          include(Messages("tax.identifiers.nino"))
      )
    }

    "include both if included" in {
      val identifiersToDisplay = Map("nino" -> "AA111111A", "sautr" -> "123456789")
      val html = views.html.tax_identifiers_partial(identifiersToDisplay)

      html.body must (
        include("123456789") and
          include("AA111111A") and
          include(Messages("tax.identifiers.sautr")) and
          include(Messages("tax.identifiers.nino"))
      )
    }
  }

  "The tax identifiers name" should {

    "correctly show nino" in {
      Messages("tax.identifiers.nino") must be("National Insurance number")
    }

    "correctly show sautr" in {
      Messages("tax.identifiers.sautr") must be("Self Assessment UTR")
    }

    "correctly show ctutr" in {
      Messages("tax.identifiers.ctutr") must be("Company Tax")
    }
  }

}
