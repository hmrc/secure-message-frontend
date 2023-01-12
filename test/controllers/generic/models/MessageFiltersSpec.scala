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

package controllers.generic.models

import org.scalatestplus.play.PlaySpec

class MessageFiltersSpec extends PlaySpec {

  "CustomerEnrolment request model" should {
    "parse a URL parameter for enrolment into its 3 part constituents" in {
      CustomerEnrolment.parse("HMRC-CUS-ORG~EoriNumber~GB1234567") mustEqual CustomerEnrolment(
        "HMRC-CUS-ORG",
        "EoriNumber",
        "GB1234567")
    }
  }

  "Tag request model" should {
    "parse a URL parameter for tag into its 2 part constituents" in {
      Tag.parse("notificationType~somevalue") mustEqual Tag("notificationType", "somevalue")
    }
  }
}
