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

package binders

import controllers.ParameterisedUrl
import org.scalatestplus.play.PlaySpec

class ParameterisedUrlSpec extends PlaySpec {

  "parsing delimited string" should {
    "return parameterised url when all parameters are included in the string" in {
      ParameterisedUrl.fromDelimitedString("/example/url::step1::/example/return/url") must be(
        ParameterisedUrl("/example/url", Map("step" -> Seq("step1"), "returnUrl" -> Seq("/example/return/url")))
      )
    }

    "return parameterised url when returnUrl is not included" in {
      ParameterisedUrl.fromDelimitedString("/example/url::step1") must be(
        ParameterisedUrl("/example/url", Map("step" -> Seq("step1")))
      )
    }

    "return parameterised url when returnUrl and step are not included" in {
      ParameterisedUrl.fromDelimitedString("/example/url") must be(ParameterisedUrl("/example/url"))
    }

    "return parameterised url when any other input is received" in {
      ParameterisedUrl.fromDelimitedString("/example/url::stuff::otherStuff::andMoreStuff") must be(
        ParameterisedUrl("/example/url")
      )
      ParameterisedUrl.fromDelimitedString("/example/url::::") must be(ParameterisedUrl("/example/url"))
    }
  }
}
