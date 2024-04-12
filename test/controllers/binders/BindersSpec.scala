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

package controllers.binders

import controllers.binders
import controllers.generic.models.{ CustomerEnrolment, Tag }
import org.scalatestplus.play.PlaySpec

class BindersSpec extends PlaySpec {

  "CustomerEnrolment request parameter binding and unbinding" should {

    "build a URL parameter for enrolment from its 3 part constituents" in {
      binders.queryStringBindableCustomerEnrolment
        .unbind("enrolment", CustomerEnrolment("key", "name", "value")) mustBe "key~name~value"
    }

    "parse a URL parameter for enrolment into its 3 part constituents" in {
      binders.queryStringBindableCustomerEnrolment
        .bind("enrolment", Map("enrolment" -> List("key~name~value"))) mustBe Some(
        Right(CustomerEnrolment("key", "name", "value"))
      )
    }

//    "fails to parse URL parameter for enrolment into its 3 part constituents" in {
//      binders.queryStringBindableCustomerEnrolment
//        .bind("enrolment", Map("enrolment" -> List("gdf34534534"))) mustBe Some(
//        Left("Unable to bind a CustomerEnrolment"))
//    }
  }

  "Tag request parameter binding and unbinding" should {

    "build a URL parameter for enrolment from its 3 part constituents" in {
      binders.queryStringBindableTag
        .unbind("tag", Tag("key", "value")) mustBe "key~value"
    }

    "parse a URL parameter for enrolment into its 3 part constituents" in {
      binders.queryStringBindableTag
        .bind("tag", Map("tag" -> List("key~value"))) mustBe Some(Right(Tag("key", "value")))
    }

//    "fails to parse URL parameter for tag into its 2 part constituents" in {
//      binders.queryStringBindableTag
//        .bind("tag", Map("tag" -> List("k456456sdfsdf"))) mustBe Some(Left("Unable to bind a Tag"))
//    }
  }
}
