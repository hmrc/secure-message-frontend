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

package model.utils

import play.api.libs.json.*

import java.time.format.DateTimeFormatter
import java.time.{ Instant, LocalDate }
import scala.util.{ Failure, Success, Try }

object DateFormats {
  implicit val instantReads: Reads[Instant] = new Reads[Instant] {

    def reads(json: JsValue): JsResult[Instant] = {

      val dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS[X]")

      json match {
        case JsString(value) =>
          Try {
            Instant.from(dtf.parse(value))
          } match {
            case Success(value)        => JsSuccess(value)
            case Failure(e: Throwable) => JsError(s"String: ${json.toString()} did not match the expected format $e")
          }
        case _ =>
          JsError(s"Expected a string, received: $json")
      }
    }
  }

  implicit val instantFormats: Format[Instant] =
    Format(instantReads, Writes.DefaultInstantWrites)

  // Format LocalDate, non-mongo
  implicit val localDateFormats: Format[LocalDate] =
    Format(Reads.DefaultLocalDateReads, Writes.DefaultLocalDateWrites)
}
