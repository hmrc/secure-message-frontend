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

package models

import java.time.{ Instant, LocalDate }
import play.api.libs.json.{ Format, Json, Reads, Writes }

final case class Letter(
  subject: String,
  content: String,
  firstReaderInformation: Option[FirstReaderInformation],
  senderInformation: Sender,
  readTime: Option[Instant] = None
)

final case class Sender(name: String, sent: LocalDate)

object Letter {
  private val dateTimeFormatString = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  private val dateFormatString = "yyyy-MM-dd"

  implicit val dateTimeFormat: Format[Instant] =
    Format(Reads.instantReads(dateTimeFormatString), Writes.DefaultInstantWrites)

  implicit val dateFormat: Format[LocalDate] =
    Format(Reads.localDateReads(dateFormatString), Writes.DefaultLocalDateWrites)

  implicit val senderFormat: Format[Sender] =
    Json.format[Sender]

  implicit val messageFormat: Format[Letter] =
    Json.format[Letter]
}
