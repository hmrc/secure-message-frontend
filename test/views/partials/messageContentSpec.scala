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

package views.partials

import base.LanguageStubs
import org.joda.time.DateTime
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.mvc.RequestHeader
import views.html.Layout
import views.html.partials.messageContent
import views.viewmodels.MessageView

class messageContentSpec extends PlaySpec with LanguageStubs {

  "messsageContent template" must {

    "have all information including first read" in new TestClass {
      val messageContent = new messageContent(layout)(
        MessageView(
          Some("Mike"),
          DateTime.parse("2021-02-19T10:29:47.275Z"),
          Some(DateTime.parse("2021-02-19T10:29:47.275Z")),
          "message body"))(messagesEn, requestHeader).toString

      messageContent must include("Mike sent this on 19 February 2021 at")
      messageContent must include("First read on 19 February 2021 at")
      messageContent must include("First viewed on 19 February 2021 at")
      messageContent must include("message body")
    }

    "be handled without first read information" in new TestClass {
      val messageContent =
        new messageContent(layout)(
          MessageView(Some("Mike"), DateTime.parse("2021-02-19T10:29:47.275Z"), None, "message body"))(
          messagesEn,
          requestHeader).toString

      messageContent must include("Mike sent this on 19 February 2021 at")
      messageContent mustNot include("First read")
      messageContent mustNot include("First viewed")
      messageContent must include("message body")
    }

    "have all information including first read in Welsh" in new TestClass {
      val messageContent = new messageContent(layout)(
        MessageView(
          Some("Mike"),
          DateTime.parse("2021-02-19T10:29:47.275Z"),
          Some(DateTime.parse("2021-02-19T10:29:47.275Z")),
          "message body"))(messagesCy, requestHeader).toString

      messageContent must include("Mike wnaeth anfon y neges hon ar 19 Chwefror 2021 am")
      messageContent must include("Darllenwyd am y tro cyntaf ar 19 Chwefror 2021 am")
      messageContent must include("Gwelwyd am y tro cyntaf ar 19 Chwefror 2021 am")
      messageContent must include("message body")
    }

    "be handled without first read information in Welsh" in new TestClass {
      val messageContent =
        new messageContent(layout)(
          MessageView(Some("Mike"), DateTime.parse("2021-02-19T10:29:47.275Z"), None, "message body"))(
          messagesCy,
          requestHeader).toString

      messageContent must include("Mike wnaeth anfon y neges hon ar 19 Chwefror 2021 am")
      messageContent mustNot include("Darllenwyd am y tro cyntaf ar")
      messageContent mustNot include("Gwelwyd am y tro cyntaf ar")
      messageContent must include("message body")
    }
  }

  class TestClass {
    implicit val requestHeader: RequestHeader = mock[RequestHeader]
    val layout = mock[Layout]
  }
}
