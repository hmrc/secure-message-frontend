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

import com.typesafe.config.ConfigFactory
import config.AppConfig
import helpers.LanguageHelper
import model.{ Encoder, EncryptAndEncode, MessageListItem }
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.filters.crypto.ApplicationCrypto
import views.helpers.PortalUrlBuilder

import java.time.{ Instant, LocalDate }

class list_partial_Spec extends PlaySpec with GuiceOneAppPerSuite with MockitoSugar with LanguageHelper {

  implicit val messages: Messages = messagesInEnglish()
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = engRequest
  lazy val applicationCrypto = app.injector.instanceOf[ApplicationCrypto]
  val encryptAndEncode = new EncryptAndEncode(applicationCrypto) {
    override lazy val encoder: Encoder = new Encoder {
      override def encryptAndEncode(value: String) = s"encoded(encrypted($value))"
    }
  }

  "list_partial" should {

    val Unread = None
    val Read = Some(Instant.now)
    val appConfig = mock[AppConfig]
    val testUrlBuilder = new PortalUrlBuilder(appConfig)

    def callListPartial(maybeReadTime: Option[Instant]): Html =
      views.html.list_partial(
        ptaBaseUrl = "/somePtaBaseUrl",
        messageItems = Seq(
          MessageListItem(
            id = "",
            subject = "Leaving self assessment",
            validFrom = LocalDate.parse("2014-08-14"),
            taxpayerName = None,
            readTime = maybeReadTime,
            sentInError = false
          )
        ),
        urlBuilder = testUrlBuilder,
        saUtr = Some("someSaUtr"),
        taxIdentifiersPartial = Html(""),
        encryptAndEncode = encryptAndEncode
      )

    "generate all field for unread message" in {
      val html = callListPartial(maybeReadTime = Unread)
      html.body must (
        include("Unread") and
          include("Leaving self assessment") and
          include("14 August 2014")
      )
    }

    "generate all field for read message" in {
      val html = callListPartial(maybeReadTime = Read)

      html.body must not include "Unread"
    }
  }
}
