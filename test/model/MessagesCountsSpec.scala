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

package model

import com.codahale.metrics.SharedMetricRegistries
import org.scalatestplus.play.PlaySpec

class MessagesCountsSpec extends PlaySpec {
  "transformReadPreference" must {
    val counts = MessagesCounts(total = 3, unread = 1)

    "return a function to extract the total count when no read preference is provided" in {
      val none = MessagesCounts.transformReadPreference(None)(counts)
      val both = MessagesCounts.transformReadPreference(Some(ReadPreference.Both))(counts)
      none mustBe both
      none mustBe MessageCount(3)
    }
    "return a function to extract the count of unread messages when a `No` preference is provided" in {
      MessagesCounts.transformReadPreference(Some(ReadPreference.No))(counts) mustBe MessageCount(1)
    }
    "return a function to extract the count of read messages when a `Yes` preference is provided" in {
      MessagesCounts.transformReadPreference(Some(ReadPreference.Yes))(counts) mustBe MessageCount(2)
    }
    SharedMetricRegistries.clear

  }
}
