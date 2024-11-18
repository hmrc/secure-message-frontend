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

import org.scalatest.Inside.*
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{ JsSuccess, Json }

class RenderMessageMetadataSpec extends PlaySpec {

  "RenderMessageMetadata" must {
    "be deserialised as a ReadMessageMetadata in the presence of a readTime element" in {
      val result = Json.fromJson[RenderMessageMetadata](
        Json.parse(
          """ {
            | "id" : "57bac7e90b0000490000b7cf",
            |  "subject" : "Test subject",
            |  "body" : {
            |      "type" : "print-suppression-notification",
            |      "form" : "SA251",
            |      "suppressedAt" : "2016-08-22",
            |      "detailsId" : "C0123456781234568"
            |  },
            |  "contentParameters" : {
            |     "data" :    {
            |         "templateData1": "data1",
            |         "templateData2": "data2"
            |     },
            |     "templateId" : "testAlertTemplate"
            |  },
            |  "validFrom" : "2016-08-22",
            |  "readTime": "2014-05-02T17:17:45.618Z",
            |  "sentInError": false,
            |  "renderUrl" : {
            |     "service": "sa-message-renderer",
            |     "url":  "/message/url"
            |  }
            |}""".stripMargin
        )
      )

      inside(result) { case JsSuccess(RenderMessageMetadata.ReadMessageMetadata(rendererUrl), _) =>
        rendererUrl mustBe ServiceUrl("sa-message-renderer", "/message/url")
      }
    }

    "be deserialised as an UnreadMessageMetadata in the presence of a markAsReadUrl element" in {
      val result = Json.fromJson[RenderMessageMetadata](
        Json.parse(
          """{
            | "id" : "57bac7e90b0000490000b7cf",
            |  "subject" : "Test subject",
            |  "body" : {
            |      "type" : "print-suppression-notification",
            |      "form" : "SA251",
            |      "suppressedAt" : "2016-08-22",
            |      "detailsId" : "C0123456781234568"
            |  },
            |  "contentParameters" : {
            |     "data" :    {
            |         "templateData1": "data1",
            |         "templateData2": "data2"
            |     },
            |     "templateId" : "testAlertTemplate"
            |  },
            |  "validFrom" : "2016-08-22",
            |  "markAsReadUrl" : {
            |     "service" : "message",
            |     "url" : "/messages/57bac7e90b0000490000b7cf/read-time"
            |  },
            |  "renderUrl" : {
            |     "service": "sa-message-renderer",
            |     "url":  "/message/url"
            |  },
            |  "sentInError": false
            |}""".stripMargin
        )
      )

      inside(result) { case JsSuccess(RenderMessageMetadata.UnreadMessageMetadata(rendererUrl, setReadTimeUrl), _) =>
        rendererUrl mustBe ServiceUrl("sa-message-renderer", "/message/url")
        setReadTimeUrl mustBe ServiceUrl("message", "/messages/57bac7e90b0000490000b7cf/read-time")
      }
    }
  }
}
