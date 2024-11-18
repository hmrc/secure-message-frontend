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

package views.helpers

import config.AppConfig
import org.scalatestplus.play.PlaySpec
import play.api.Configuration

import javax.inject.Inject

class PortalUrlBuilderSpec @Inject (configuration: Configuration) extends PlaySpec {

  trait Setup {
    class TestConfig(configuration: Configuration) extends AppConfig(configuration) {
      override val btaHost: String = ""
      override val btaBaseUrl: String = ""
      override val ptaHost: String = ""
      override val ptaBaseUrl: String = ""
      override def getPortalPath(pathKey: String): String = "http://someserver:8080/utr/<utr>"
    }

    val portalUrlBuilder = new PortalUrlBuilder(new TestConfig(configuration))
  }

  "PortalUrlBuilder " must {

    "return a resolved dynamic full URL with parameters year and saUtr resolved using a request and user object" in new Setup {
      val actualDestinationUrl = portalUrlBuilder.buildPortalUrl(Some("someUtr9898988"), "someDestinationPathKey")

      actualDestinationUrl must startWith("""http://someserver:8080/utr/someUtr""")
      actualDestinationUrl must not include """<utr>"""
    }
  }
}
